package de.hyperknox;


import de.hyperknox.listener.ListenerType;

public class Main {
    public static API api;

    public static void main(String[] args) {
        api = new API("gust", "guest", "localhost", 5672);


        api.addListener("test-ex", "test", "test-key", new TestListener(), ListenerType.RECEIVE);
        api.addListener("test-ex", "test", "test-key", new RespondListener(), ListenerType.RESPOND);

        String msg = "This is a Test";
        api.sendData("test-ex", "test-key", msg.getBytes());
    }
}
