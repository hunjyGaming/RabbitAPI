package de.hyperknox.listener;

import com.rabbitmq.client.Delivery;

public interface  RabbitListener {

    void onDeliverCallback(String consumerTag, Delivery message);

    void onCancelCallback(String consumerTag);

}
