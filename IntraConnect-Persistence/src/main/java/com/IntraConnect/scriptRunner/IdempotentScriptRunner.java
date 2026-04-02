package com.IntraConnect.scriptRunner;

import com.IntraConnect.helper.Console;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Führt eine SQL-Datei aus.
 */
public class IdempotentScriptRunner {
	
	private static final Logger log = LoggerFactory.getLogger(IdempotentScriptRunner.class);
	private final DataSource dataSource;
	private static final Pattern CONSTRAINT_PATTERN =
			Pattern.compile("CONSTRAINT\\s+\\[?(\\w+)\\]?\\s+", Pattern.CASE_INSENSITIVE);
	
	public IdempotentScriptRunner(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	/**
	 * Führt eine SQL-Scripte aus.
	 */
	public void createSchemas() {
		
		try {
			// Alle V200__Persis_schema.sql Dateien im Klassenpfad finden
			Enumeration<URL> resources = Thread.currentThread()
					.getContextClassLoader()
					.getResources("database/V200__Persis_schema.sql");
			
			Vector<URL> urls = new Vector<>();
			while (resources.hasMoreElements()) {
				urls.add(resources.nextElement());
			}
			
			if (urls.isEmpty()) {
				Console.error.println("Keine V200__Persis_schema.sql Dateien gefunden!");
				return;
			}
			try (Connection conn = dataSource.getConnection();
				 Statement stmt = conn.createStatement()) {
				
				DatabaseMetaData metaData = conn.getMetaData();
//				if(!isDatabaseEmpty(metaData)){
//					return;
//				}
				// Jede Datei nacheinander ausführen
				for (java.net.URL url : urls) {
					//System.out.println("Schema ausführen: " + url);
					
					try (InputStream is = url.openStream();
						 Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
						
						scanner.useDelimiter(";"); // Trennung der Statements
						
						while (scanner.hasNext()) {
							String sql = scanner.next().trim();
							if (!sql.isEmpty()) {
								if (sql.startsWith("CREATE")) {
									String tableName = extractTableName(sql);
									if(tableName == null){
										// es ist keine Tabelle. CREATE INDEX?
										if(IndexExists(metaData, sql)){
											continue;
										}
									}
									else if ( tableExists(metaData, tableName)) {
										Console.info.println("Table " + tableName + " already exists, skipping...");
										continue;
									}
								} else if (isConstraintStatement(sql)) {
									String constraintName = extractConstraintName(sql);
									if (constraintName == null) {
										continue;
									}
									if (constraintExists(conn, constraintName)) {
										continue;
									}
								}
								stmt.execute(sql);
								System.out.println(sql);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public boolean isDatabaseEmpty( DatabaseMetaData metaData) throws SQLException {
		
		try (ResultSet rs = metaData.getTables(null, null, "%", new String[] {"TABLE"})) {
			// Wenn ResultSet leer → keine Tabellen
			return !rs.next();
		}
	}
	
	/**
	 * Prüft, ob ein SQL-Statement ein Constraint-Statement ist
	 */
	public static boolean isConstraintStatement(String sql) {
		if (sql == null) return false;
		return sql.toLowerCase().contains("constraint");
	}
	
	public static String extractConstraintName(String sql) {
		Matcher matcher = CONSTRAINT_PATTERN.matcher(sql);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null; // konnte nicht gefunden werden
	}
	
	/**
	 * Prüft, ob ein Constraint in der DB existiert
	 */
	public static boolean constraintExists(Connection conn, String constraintName) throws SQLException {
		if (constraintName == null) return false;
		
		String sql = "SELECT 1 FROM sys.foreign_keys WHERE name = ? " +
				"UNION ALL " +
				"SELECT 1 FROM sys.key_constraints WHERE name = ?";
		
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, constraintName);
			ps.setString(2, constraintName);
			
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	private boolean tableExists(DatabaseMetaData metaData, String tableName) {
		if (tableName.isBlank()) {
			log.error("ERROR {}", metaData);
		}
		try (ResultSet rs = metaData.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
			return rs.next();
		} catch (SQLException e) {
			log.error("ERROR {},{}", e.getMessage(), metaData);
			throw new RuntimeException();
		}
	}
	
	private String extractTableName(String sql) {
		// Regex für "CREATE TABLE <name>"
		Pattern pattern = Pattern.compile("(?i)CREATE\\s+TABLE\\s+([a-zA-Z0-9_]+)");
		Matcher matcher = pattern.matcher(sql);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
	
	private boolean IndexExists(DatabaseMetaData metaData, String sql) {
		
		Pattern pattern = Pattern.compile(
				"CREATE\\s+INDEX\\s+(\\w+)\\s+ON\\s+(\\w+)",
				Pattern.CASE_INSENSITIVE
		);
		
		Matcher matcher = pattern.matcher(sql);
		
		if (matcher.find()) {
			String indexName = matcher.group(1);
			String tableName = matcher.group(2);
			
			try (ResultSet rs = metaData.getIndexInfo(
					null,
					null,
					tableName,
					false,
					false)) {
				
				while (rs.next()) {
					String existingIndex = rs.getString("INDEX_NAME");
					if (indexName.equalsIgnoreCase(existingIndex)) {
						return true;
					}
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		return false;
	}
}

