package com.pilot.queryExec;

import com.pilot.callable.SelectCallable;
import com.pilot.callable.UpdateCallable;
import com.pilot.persistence.Persistence;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.concurrent.*;

public class QueryExecutor {

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final TransferQueue<Object> resultQueue = new LinkedTransferQueue<>();
    private final HashMap<String,Persistence> persistences = new HashMap<>();
    private HikariDataSource dataSource = new HikariDataSource();

    private QueryExecutor(){

    }
    public QueryExecutor Create(){
        return  new QueryExecutor();
    }
    public QueryExecutor CreateWithDB(String Database){
        Persistence persistence = persistences.get(Database);
        if(persistence != null){
            dataSource = persistence.getDataSource();
        }
        return  new QueryExecutor();
    }

    public Future<Integer> submitUpdate(String query) {
        return executor.submit(new UpdateCallable(query, dataSource));
    }

    public Future<ResultSet> submitSelect(String query) {
        return executor.submit(new SelectCallable(query, dataSource));
    }

    public TransferQueue<Object> getResultQueue() {
        return resultQueue;
    }
}
