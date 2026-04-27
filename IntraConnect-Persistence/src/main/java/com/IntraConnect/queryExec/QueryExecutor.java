package com.IntraConnect.queryExec;

import com.IntraConnect.persistence.Persistence;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

@Component
public class QueryExecutor {
	
	private final Executor executor;
	
	public QueryExecutor(@Qualifier("taskExecutor") Executor executor) {
		this.executor = executor;
	}
	
	private <T> T submit(Callable<T> task) {
		FutureTask<T> future = new FutureTask<>(task);
		executor.execute(future);
		
		try {
			return future.get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public ResultSet select(String sql, Connection connection, Object... params) {
		return submit(() -> {
			// KEIN try-with-resources für ps hier, sonst wird rs sofort geschlossen!
			PreparedStatement ps = connection.prepareStatement(sql);
			applyParameters(ps, params);
			return ps.executeQuery();
		});
	}
	
	public int update(String sql, Connection connection, Object... params) {
		return submit(() -> {
			try (PreparedStatement ps = connection.prepareStatement(sql)) {
				applyParameters(ps, params);
				return ps.executeUpdate();
			}
		});
	}
	
	public boolean exists(String sql, Connection connection, Object... params) {
		return submit(() -> {
			String wrappedSql = "SELECT CASE WHEN EXISTS (" + sql + ") THEN 1 ELSE 0 END";
			try (PreparedStatement ps = connection.prepareStatement(wrappedSql)) {
				applyParameters(ps, params);
				try (ResultSet rs = ps.executeQuery()) {
					rs.next();
					return rs.getBoolean(1);
				}
			}
		});
	}
	
	public void insert(String sql, Connection connection, Object... params) {
		update(sql, connection, params);
	}
	
	public boolean delete(String sql, Connection connection, Object... params) {
		return submit(() -> {
			try (PreparedStatement ps = connection.prepareStatement(sql)) {
				applyParameters(ps, params);
				return ps.execute();
			}
		});
	}
	
	public int[] executeBatch(String sql, Connection connection, List<Object[]> parameterList) {
		return submit(() -> {
			try (PreparedStatement ps = connection.prepareStatement(sql)) {
				for (Object[] params : parameterList) {
					applyParameters(ps, params);
					ps.addBatch();
				}
				return ps.executeBatch();
			}
		});
	}
	
	// Zentrale Logik für alle Typ-Probleme (Enums, Nulls, etc.)
	private void applyParameters(PreparedStatement ps, Object... params) throws SQLException {
		if (params == null) return;
		for (int i = 0; i < params.length; i++) {
			Object val = params[i];
			int index = i + 1;
			
			if (val == null) {
				ps.setNull(index, java.sql.Types.NULL);
			} else if (val instanceof Enum<?>) {
				ps.setString(index, ((Enum<?>) val).name());
			} else if (val instanceof java.time.temporal.TemporalAccessor) {
				ps.setObject(index, val.toString());
			} else {
				ps.setObject(index, val);
			}
		}
	}
}
