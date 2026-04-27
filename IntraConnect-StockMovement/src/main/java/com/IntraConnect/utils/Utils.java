package com.IntraConnect.utils;

import com.IntraConnect._enum.LuState;
import com.IntraConnect.path.nodes.Graph;
import com.IntraConnect.path.nodes.Node;
import com.IntraConnect.queryExec.transaction.Transaction;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Utils {
	
	private Utils(){
	
	}
	/**
	 * set LoadUnit Location
	 * @param luNumber : Load unit number
	 * @param location : location*/
	public static void setLuLocation(String luNumber, String location){
		updateLoadUnit(luNumber, location, null);
	}
	/**
	 * set LoadUnit Destination
	 * @param luNumber : Load unit number
	 * @param destination : destination*/
	public static void setLuDestination(String luNumber, String destination){
		updateLoadUnit(luNumber, null, destination);
	}
	/**
	 * set LoadUnit Location
	 * @param luNumber : Load unit number
	 * @param location : location*/
	public static void setLuLocationDestination(String luNumber, String location, String destination){
		updateLoadUnit(luNumber, location, destination);
	}
	
	private static void updateLoadUnit(String luNumber, String location, String destination) {
		try (Transaction tx = Transaction.create()) {
			
			long id = getLoadUnitId(tx, luNumber);
			
			if (location != null) {
				validateNode(location, "Location", luNumber);
			}
			if (destination != null) {
				validateNode(destination, "Destination", luNumber);
			}
			
			StringBuilder sql = new StringBuilder("UPDATE LoadUnit_Roadway SET ");
			
			if (location != null) {
				sql.append("LOCATION = ").append(location).append(", ");
			}
			if (destination != null) {
				sql.append("DESTINATION = ").append(destination).append(", ");
			}
			
			sql.append("nextLocation = '', STATE = ").append(LuState.NEW.name()).append(" WHERE ID = ").append(id);
			
			tx.update(sql.toString());
			tx.commit();
			
		} catch (Exception e) {
			throw new RuntimeException("Failed to update LoadUnit: " + luNumber, e);
		}
	}
	private static long getLoadUnitId(Transaction transaction, String luNumber){
		String sql= "SELECT ID FROM LOADUNIT WHERE NUMBER = '"+luNumber+"'";
		ResultSet rs = transaction.select(sql);
		try {
			if (!rs.next()) {
				throw new RuntimeException("LoadUnit not found: " + luNumber);
			}
			return rs.getLong("ID");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	private static void validateNode(String value, String type, String luNumber) {
		Node node = Graph.get().get(value);
		if (node == null) {
			throw new RuntimeException(
					"LoadUnit: " + luNumber + " -> Invalid " + type + ": " + value
			);
		}
	}
}
