package intf;

public interface PilotServices<T>{
    String getName();
    void configuration(T config);
    void validate();
    void run();
    void stop();
}
