package com.IntraConnect.services;

import com.IntraConnect.messageEngine.ControllerRegistry;
import com.IntraConnect.async.client.AsyncClientEngine;
import com.IntraConnect.async.server.AsyncServerEngine;
import com.IntraConnect.command.handlerReg.Register;
import com.IntraConnect.queryExec.transaction.Transaction;
import com.IntraConnect.controller.Controller;
import com.IntraConnect.helper.Console;
import com.IntraConnect.intf.PilotApplicationServices;
import com.IntraConnect.tcpController.TcpController;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import com.IntraConnect.xml.ConnectionConfig;
import com.IntraConnect.xml.ControllerConfig;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;


@Component
public class TCPService extends PilotApplicationServices {

    List<Controller> _controllers = new ArrayList<>();
	private final ControllerRegistry controllerRegistry;
	private final AsyncServerEngine serverEngine;
	private final AsyncClientEngine clientEngine;
	boolean enabled;
	private static final Logger log = LoggerFactory.getLogger(TCPService.class);
	
    public TCPService(Register register,
					  ControllerRegistry controllerRegistry,
					  AsyncServerEngine serverEngine,
					  AsyncClientEngine clientEngine) {
		super(register);
		
		this.controllerRegistry = controllerRegistry;
		this.serverEngine = serverEngine;
		this.clientEngine = clientEngine;
	}

    @Override
    public String getName() {
        return "TCP";
    }
	
	@Override
	public void configuration(Element module, ApplicationContext context) {
		if(module != null) {
			enabled = Boolean.parseBoolean(module.getAttributeValue("enabled"));
			if (!enabled){
				return;
			}
			
			List<Element> controllers = module.getChildren("Controller");
			for (Element controller : controllers) {
				
				String name = controller.getAttributeValue("name");
				boolean active = Boolean.parseBoolean(controller.getAttributeValue("active"));
				String prefix = controller.getAttributeValue("prefix");
				String suffix = controller.getAttributeValue("suffix");
				
				Element connection = controller.getChild("Connection");
				String host = connection.getChild("Host").getContent().getFirst().getValue();;
				int port = Integer.parseInt(connection.getChild("Port").getContent().getFirst().getValue());
				int timeout = Integer.parseInt(connection.getChild("Timeout").getContent().getFirst().getValue());
				
				ConnectionConfig connectionConfig = new ConnectionConfig(host, port, timeout);
				ControllerConfig controllerConfig = new ControllerConfig(name, active, prefix, suffix, connectionConfig);
				
				TcpController _controller = new TcpController(controllerConfig);
				_controllers.add(_controller);
			}
		}
	}
	
	@Override
	public void register() {
	
	}
	
	@Override
    public void validate() {
		if (!enabled){
			return;
		}
        for (Controller controller : _controllers) {
            if (controller.isActive()) {
                try (ServerSocket serverSocket = new ServerSocket(controller.getPort())) {
                    Console.info.println(controller.getName() + " -Port " + controller.getPort() + " ist verfügbar.");
                } catch (BindException e) {
                    throw new RuntimeException(controller.getName() + " -Port " + controller.getPort() + " ist bereits belegt.");
                } catch (IOException e) {
                    throw new RuntimeException(controller.getName() + " - Ein Fehler ist aufgetreten: " + e.getMessage());
                }
            }
        }
		//Controller registrieren
		controllerRegistry.register(_controllers);
        // Alles ok, wir können nun die Controller in die datenbank anlegen
        InitializeDatabaseTableController();
    }

    private void InitializeDatabaseTableController() {
        try(Transaction transaction = Transaction.create()){
			int count = transaction.queryCount("SELECT COUNT(*) FROM CONTROLLER");
			if(count > 0)
				return;
            for (Controller controller : _controllers) {
                String sql = "INSERT INTO CONTROLLER (NAME, DESCRIPTION, CONNECTED) " +
						"VALUES (?, ?, ?);";
				transaction.insert(sql, controller.getName(), "Host->"+ controller.getHost()+"/ PORT->"+controller.getPort(), false);
            }
            
            transaction.commit();

        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void run() {
		if (!enabled){
			return;
		}
        try {
            for (Controller controller : _controllers) {
                if (controller.isActive()) {
					
					serverEngine.registerController(controller);

                } else {
                    clientEngine.connect(controller);
                }
            }
			
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    @Override
    public void stop() {
		if (!enabled){
			return;
		}
    }
}
