package com.pilot.callable;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public class SelectCallable implements Callable<ResultSet> {

    /**
     *
     */
    private final String _query;
    private final DataSource _dataSource;

    public SelectCallable(String query, DataSource dataSource){
        _query = query;
        _dataSource = dataSource;
    }

    @Override
    public ResultSet call() throws Exception {
        try {
            Connection cn = _dataSource.getConnection();
            PreparedStatement ps =cn.prepareStatement(_query);
            ResultSet resultSet = ps.executeQuery();
            cn.commit();

            return resultSet;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
