package com.pilot;

import com.pilot.dataSource.DataSourceInitializer;
import com.pilot.listener.ConfigurationLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import xml.SystemConfig;

import java.sql.SQLException;


@SpringBootApplication
@ComponentScan
public class Starter {

    static void main(String[] args) throws SQLException {
        SystemConfig systemConfig = ConfigurationLoader.startingLoading();

         SpringApplication application = new SpringApplication(Starter.class);
         //application.setBannerMode(Banner.Mode.OFF);
        application.addInitializers(new SystemConfigInitializer(systemConfig));
         application.addInitializers(new DataSourceInitializer(systemConfig));
         application.run();
    }
}