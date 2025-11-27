package intf;

import org.springframework.context.ApplicationContext;

public interface PilotServices<T>{
    String getName();
    void configuration(T config, ApplicationContext context);
    void validate();
    void run();
    void stop();
}
