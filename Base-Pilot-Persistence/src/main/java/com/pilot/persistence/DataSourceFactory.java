package com.pilot.persistence;

import com.zaxxer.hikari.HikariDataSource;

public class DataSourceFactory {

    private static HikariDataSource hds = new HikariDataSource();

    static {
        hds.setJdbcUrl(getHost());
        hds.setUsername(getUsername());
        hds.setPassword(getPassword());
        hds.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    }

}
