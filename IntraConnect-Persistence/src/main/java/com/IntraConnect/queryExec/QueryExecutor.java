package com.IntraConnect.queryExec;

import com.IntraConnect.persistence.Persistence;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
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
	public ResultSet select(String sql, Connection connection) {
		return submit(() -> {
			PreparedStatement ps = connection.prepareStatement(sql);
			return ps.executeQuery(); // Statement bleibt offen
		});
	}
	
	public int update(String sql, Connection connection) {
		return submit(() -> {
			try (PreparedStatement ps = connection.prepareStatement(sql)) {
				return ps.executeUpdate();
			}
		});
	}
	
	public boolean exists(String sql, Connection connection) {
		String s = "SELECT EXISTS (" + sql + ")";
		return submit(() -> {
			String wrappedSql = "SELECT CASE WHEN EXISTS (" + sql + ") THEN 1 ELSE 0 END";
			try (PreparedStatement ps = connection.prepareStatement(wrappedSql)) {
				try (ResultSet rs = ps.executeQuery()) {
					rs.next();
					return rs.getBoolean(1);
				}
			}
		});
	}
	
	public void insert(String sql, Connection connection) {
		update(sql, connection);
	}
	
	public boolean delete(String sql, Connection connection) {
		return submit(() -> {
			try (PreparedStatement ps = connection.prepareStatement(sql)) {
				return ps.execute();
			}
		});
	}
}
