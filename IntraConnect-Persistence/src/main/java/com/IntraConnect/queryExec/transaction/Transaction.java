package com.IntraConnect.queryExec.transaction;


import com.IntraConnect.queryExec.QueryExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Transaction implements AutoCloseable {
	
	private static final Logger log = LoggerFactory.getLogger(Transaction.class);
	private final Connection connection;
	private final QueryExecutor executor;
	private final List<Statement> openStatements = Collections.synchronizedList(new ArrayList<>());
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
			
			return rs;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public int queryCount(String sql, Object... params) {
		try (ResultSet rs = executor.select(sql, connection, params);
			 Statement st = rs.getStatement()) {
			openStatements.add(st);
			if (rs.next()) return rs.getInt(1);
			return 0;
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
	
	public void insertBatch(String sql, List<Object[]> parameterList) {
		executor.executeBatch(sql, connection, parameterList);
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
		// Erst alle Statements schließen, dann Connection
		for (Statement st : openStatements) {
			try { if (st != null) st.close(); } catch (SQLException ignored) {}
		}
		openStatements.clear();
		
		try {
			if (!committed) connection.rollback();
		} catch (SQLException e) {
			log.error("Rollback fehlgeschlagen", e);
		} finally {
			try { connection.close(); } catch (SQLException ignored) {}
		}
	}
}
