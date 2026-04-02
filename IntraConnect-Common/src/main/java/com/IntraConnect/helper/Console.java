package com.IntraConnect.helper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Console {
	
	private  Console(){
	
	}
	
	static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	
	public static class info{
		
		private info() {
		}
		
		public static void println(String message){
			System.out.println(LocalDateTime.now().format(formatter)+"  [INFO]........ "+message);
		}
		public static void print(String message){
			System.out.println(" - "+message);
		}
	}
	
	public static class error{
		private error(){
		
		}
		public static void println(String message){
			System.out.println(LocalDateTime.now().format(formatter)+"  [ERROR]........ "+message);
		}
		public static void print(String message){
			System.out.println(" - "+message);
		}
	}
}
