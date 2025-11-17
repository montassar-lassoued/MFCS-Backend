package com.pilot.callable;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public class UpdateCallable implements Callable<Integer> {

    /**
     *
     */
    private final String _query;
    private final HikariDataSource _dataSource;

    public UpdateCallable(String query, HikariDataSource dataSource){
        _query = query;
        _dataSource = dataSource;
    }

    @Override
    public Integer call() throws Exception {
        try {
            Connection cn = _dataSource.getConnection();
            PreparedStatement ps =cn.prepareStatement(_query);
            return ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
