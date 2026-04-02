package com.IntraConnect.utils;

import com.IntraConnect.handler.LoadUnitCreateHandler;
import com.IntraConnect.command.trigger.Trigger;
import com.IntraConnect.queryExec.transaction.Transaction;
import com.IntraConnect.record.LoadUnitCreate;

import java.time.LocalDateTime;


public class LoadUnit {
	
	private LoadUnit(){
	
	}
	/**
	 * create LoadUnit
	 * @param number : Load unit number*/
	public static void create(String number){
		createLoadUnit(number, "", null);
	}
	/**
	 * create LoadUnit
	 * @param number : Load unit number
	 * @param description : description*/
	public static void create(String number, String description){
		createLoadUnit(number, description, null);
	}
	/**
	 * create LoadUnit
	 * @param number : Load unit number
	 * @param articleID : description*/
	public static void create(String number, int articleID){
		createLoadUnit(number, "", articleID);
	}
	/**
	 * create LoadUnit
	 * @param number : Load unit number
	 * @param description : description
	 * @param articleID : description*/
	public static void create(String number, String description, int articleID){
		createLoadUnit(number, description, articleID);
	}
	/**
	 * delete LoadUnit
	 * @param number : Load unit number*/
	public static void delete(String number){
		try(Transaction transaction = Transaction.create()){
			String sql = "DELETE FROM LOADUNIT WHERE NUMBER = '"+number+"'";
			
			transaction.delete(sql);
			transaction.commit();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void createLoadUnit(String number, String description, Integer articleID){
		try (Transaction transaction = Transaction.create()){
			String sql_l = "INSERT INTO LOADUNIT (NUMBER, DESCRIPTION, CREATED)" +
					" VALUES ('"+number+"','"+description+"', '"+ LocalDateTime.now() +"')";
			
			transaction.insert(sql_l);
			
			if(articleID != null) {
				String sql_a = "INSERT INTO LOADUNIT_ARTICLE (LOADUNIT_ID, ARTICLE_ID) " +
						"VALUES (SELECT ID FROM LOADUNIT WHERE NUMBER = '" + number + "', " + articleID + ")";
				
				transaction.insert(sql_a);
			}
			
			transaction.commit();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
