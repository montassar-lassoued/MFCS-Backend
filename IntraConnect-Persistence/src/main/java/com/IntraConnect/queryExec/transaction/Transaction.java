package com.IntraConnect.queryExec.transaction;


import com.IntraConnect.queryExec.QueryExecutor;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Transaction implements AutoCloseable {
	
	private final Connection connection;
	private final QueryExecutor executor;
	private final List<Statement> openStatements = new ArrayList<>();
	private boolean committed = false;
	
	Transaction(DataSource dataSource,
				QueryExecutor executor) throws SQLException {
		
		this.connection = dataSource.getConnection();
		this.connection.setAutoCommit(false);
		this.executor = executor;
	}
	
	public static Transaction create() {
		return SpringContextHolder
				.getBean(TransactionFactory.class)
				.create();
	}
	
	public static Transaction create(String dbName) {
		return SpringContextHolder
				.getBean(TransactionFactory.class)
				.create(dbName);
	}
	
	public ResultSet select(String sql, Object... params) {
		ResultSet rs = executor.select(sql, connection, params);
		
		try {
			openStatements.add(rs.getStatement());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		return rs;
	}
	
	public int queryCount(String sql, Object... params) {
		ResultSet rs = executor.select(sql, connection, params);
		
		try {
			openStatements.add(rs.getStatement());
			
			return rs.getInt(1);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean exists(String sql, Object... params) {
		return executor.exists(sql, connection, params);
	}
	
	public int update(String sql, Object... params) {
		return executor.update(sql, connection, params);
	}
	
	public boolean delete(String sql, Object... params) {
		return executor.delete(sql, connection, params);
	}
	
	public void insert(String sql, Object... params) {
		executor.insert(sql, connection, params);
	}
	
	public void commit() {
		try {
			connection.commit();
			committed = true;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void close() throws Exception {
		try {
			if (!committed) {
				connection.rollback();
			}
		} finally {
			
			// Alle Statements schließen
			for (Statement st : openStatements) {
				try { st.close(); } catch (Exception ignored) {}
			}
			
			connection.close();
		}
	}
}
