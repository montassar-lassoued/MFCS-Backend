package com.IntraConnect.services;

import com.IntraConnect.datagram.server.UdpServerEngine;
import com.IntraConnect.datagram.client.UdpClientEngine;
import com.IntraConnect.command.handlerReg.Register;
import com.IntraConnect.messageEngine.ControllerRegistry;
import com.IntraConnect.queryExec.transaction.Transaction;
import com.IntraConnect.controller.Controller;
import com.IntraConnect.helper.Console;
import com.IntraConnect.intf.PilotApplicationServices;
import com.IntraConnect.udpController.UdpController;
import org.jdom2.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import com.IntraConnect.xml.ConnectionConfig;
import com.IntraConnect.xml.ControllerConfig;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

@Component
public class UDPService extends PilotApplicationServices {
	
	List<Controller> _controllers = new ArrayList<>();
	private final UdpServerEngine udpServerEngine;
	private final UdpClientEngine clientEngine;
	private final ControllerRegistry controllerRegistry;
	boolean enabled;
	
	public UDPService(Register register, UdpServerEngine udpServerEngine, UdpClientEngine clientEngine, ControllerRegistry controllerRegistry) {
		super(register);
		
		this.udpServerEngine = udpServerEngine;
		this.clientEngine = clientEngine;
		this.controllerRegistry = controllerRegistry;
	}
	
	@Override
    public String getName() {
        return "UDP";
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
				
				UdpController _controller = new UdpController(controllerConfig);
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
				try (DatagramSocket socket = new DatagramSocket(controller.getPort())) {
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
		// in die DB speichern
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
					udpServerEngine.registerController(controller);
				} else {
					clientEngine.registerController(controller);
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
