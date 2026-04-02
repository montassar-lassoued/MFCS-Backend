package com.IntraConnect;

import com.IntraConnect.helper.Console;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;

@Component
public class ModuleJarScanner {
	
	public void scanAndLog() {
		String classpath = System.getProperty("java.class.path");
		
		Arrays.stream(classpath.split(File.pathSeparator))
				.filter(p -> p.endsWith(".jar"))
				.map(File::new)
				.map(File::getName)
				.filter(n -> n.startsWith("IntraConnect-"))
				.forEach(n -> Console.info.println("Modul loaded: " + n));
	}
}
