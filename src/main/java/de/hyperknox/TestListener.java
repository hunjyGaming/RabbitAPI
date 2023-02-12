package de.hyperknox;

import com.rabbitmq.client.Delivery;
import de.hyperknox.listener.RabbitListener;

import java.util.Arrays;

public class TestListener implements RabbitListener {

    @Override
    public void onDeliverCallback(String consumerTag, Delivery message) {
        System.out.println("######### Listener #########");
        System.out.println("");
        System.out.println("Reciving: ");
        System.out.println("");
        System.out.println(consumerTag);
        System.out.println(new String(message.getBody()));
        System.out.println(message.getProperties().getHeaders().get("DataID"));
        System.out.println("");
        System.out.println("Respond:");
        String respond = "Okay!";
        System.out.println(respond);
        Main.api.sendResponse("test-ex", "test-key", respond.getBytes());
        System.out.println("");
        System.out.println("######### Listener #########");
    }


    @Override
    public void onCancelCallback(String consumerTag) {
        System.out.println(consumerTag);
    }
}
