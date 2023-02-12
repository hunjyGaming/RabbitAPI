package de.hyperknox;

import com.rabbitmq.client.*;
import de.hyperknox.listener.ListenerType;
import de.hyperknox.listener.RabbitListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class API {

    private final String username;
    private final String password;
    private final String host;
    private final int port;
    private final ConnectionFactory connectionFactory;
    private final Connection connection;
    private final Channel channel;

    public API(String username, String password, String host, int port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.connectionFactory = new ConnectionFactory();
        try {
            this.connection = connectionFactory.newConnection(buildURL());
            this.channel = connection.createChannel();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildURL() {
        return "amqp://" + username + ":" + password + "@" + host + ":" + port + "/";
    }

    public void addListener(String exchange, String queue, String routingKey, RabbitListener listener, ListenerType respondListener) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        String finalQueue = (respondListener == ListenerType.RESPOND) ? queue + "-respond" : queue;
        String finalKey = (respondListener == ListenerType.RESPOND) ? routingKey + "-respond" : routingKey;

        System.out.println(finalQueue + " : " + finalKey);
        try {
            createExchange(exchange);
            channel.queueDeclare(finalQueue, true, false, false, null);
            createBinding(finalQueue, exchange, finalKey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        executorService.execute(() -> {
            DeliverCallback deliverCallback = listener::onDeliverCallback;

            CancelCallback cancelCallback = listener::onCancelCallback;
            try {
                channel.basicConsume(finalQueue, true, deliverCallback, cancelCallback);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void createBinding(String queueName, String exchangeName, String routingKey) {
        try {
            this.channel.queueBind(queueName, exchangeName, routingKey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendData(String exchangeName, String routingKey, byte[] bytes) {
        try {

            Map<String, Object> headerMap = new HashMap<>();
            headerMap.put("DataID", UUID.randomUUID().toString());
            AMQP.BasicProperties.Builder basicProperties = new AMQP.BasicProperties.Builder();
            basicProperties.headers(headerMap);
            this.channel.basicPublish(exchangeName, routingKey, basicProperties.build(), bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendResponse(String exchangeName, String routingKey, byte[] bytes) {
        try {
            this.channel.basicPublish(exchangeName, routingKey + "-respond", null, bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createExchange(String exchangeName) {
        try {
            this.channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public Connection getConnection() {
        return connection;
    }

    public Channel getChannel() {
        return channel;
    }
}
