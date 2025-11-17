package intf;

public interface PilotServices<T> extends Runnable{
    String getName();
    void configuration(T config);
    void validate();
    //void run();
    void stop();
}
