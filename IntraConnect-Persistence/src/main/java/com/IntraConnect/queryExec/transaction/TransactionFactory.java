package com.IntraConnect.queryExec.transaction;

import com.IntraConnect.persistence.Persistence;
import com.IntraConnect.queryExec.QueryExecutor;
import com.IntraConnect.services.PersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TransactionFactory {
	
	private final PersistenceService persistenceService;
	private final QueryExecutor executor;
	
	public TransactionFactory(PersistenceService persistenceService,
							  QueryExecutor executor) {
		this.persistenceService = persistenceService;
		this.executor = executor;
	}
	
	public Transaction create() {
		Persistence main = persistenceService.getMainDatabaseOrThrow();
		
		try {
			return new Transaction(
					main.getDataSource(),
					executor
			);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Transaction create(String databaseName) {
		Persistence persistence =
				persistenceService.getDatabaseOrThrow(databaseName);

		try {
			return new Transaction(
					persistence.getDataSource(),
					executor
			);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}