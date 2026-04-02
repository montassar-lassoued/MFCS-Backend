package com.IntraConnect.persistence;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class PersistenceDataSource {

    private final DataSource dataSource;

    public PersistenceDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
