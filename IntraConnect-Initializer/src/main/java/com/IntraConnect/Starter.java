package com.IntraConnect;

import com.IntraConnect.dataSource.DataSourceInitializer;
import com.IntraConnect.loader.ConfigurationLoader;
import com.IntraConnect.loader.SystemConfigInitializer;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import javax.sql.DataSource;
import java.sql.SQLException;


@SpringBootApplication
@ComponentScan
public class Starter {
	
	private static final Logger log = LoggerFactory.getLogger(Starter.class);
	
	static void main(String[] args) throws SQLException {
		
		log.info("IntraConnect Start()");
		Element systemConfig = ConfigurationLoader.startingLoading();
		
		SpringApplication application = new SpringApplication(Starter.class);
         //application.setBannerMode(Banner.Mode.OFF);
        application.addInitializers(new SystemConfigInitializer(systemConfig));
         application.addInitializers(new DataSourceInitializer(systemConfig));
         application.run();
    }
	
}