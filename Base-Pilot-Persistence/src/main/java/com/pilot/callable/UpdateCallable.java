package com.pilot.callable;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public class UpdateCallable implements Callable<Integer> {

    /**
     *
     */
    private final String _query;
    private final DataSource _dataSource;

    public UpdateCallable(String query, DataSource dataSource){
        _query = query;
        _dataSource = dataSource;
    }

    @Override
    public Integer call() throws Exception {
        try {
            Connection cn = _dataSource.getConnection();
            PreparedStatement ps =cn.prepareStatement(_query);
            int result = ps.executeUpdate();
            cn.commit();
            return result;

        }  catch (Exception e) {
            // Rückgängig machen bei Fehler
            try (Connection conn = _dataSource.getConnection()) {
                conn.rollback();
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            throw e;
        }
    }
}
