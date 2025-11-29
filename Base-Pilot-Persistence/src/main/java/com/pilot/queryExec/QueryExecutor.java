package com.pilot.queryExec;

import com.pilot.callable.SelectCallable;
import com.pilot.callable.UpdateCallable;
import com.pilot.persistence.Persistence;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.concurrent.*;

public class QueryExecutor {

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final TransferQueue<Object> resultQueue = new LinkedTransferQueue<>();
    private static HashMap<String,Persistence> persistences = new HashMap<>();
    static DataSource dataSource ;

    private QueryExecutor(){

    }
    public static QueryExecutor Create(){
        Persistence persistence = persistences.entrySet().iterator().next().getValue();
        dataSource = persistence.getDataSource();
        return  new QueryExecutor();
    }
    public QueryExecutor CreateWithDB(String Database){
        Persistence persistence = persistences.get(Database);
        if(persistence != null){
            dataSource = persistence.getDataSource();
        }
        return  new QueryExecutor();
    }

    public Integer submitUpdate(String query) {
        Future<Integer> resultSetFuture = executor.submit(new UpdateCallable(query, dataSource));
        try {
            return  resultSetFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultSet submitSelect(String query) {
        Future<ResultSet> resultSetFuture = executor.submit(new SelectCallable(query, dataSource));

        try {
            return  resultSetFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public Future<ResultSet> findAll(String table) {
        String sql = "SELECT * FROM "+table;
        return executor.submit(new SelectCallable(sql, dataSource));
    }
    public Future<ResultSet> findById(String table, Long id) {
        String sql = "SELECT * FROM "+table+" WHERE ID ="+id;
        return executor.submit(new SelectCallable(sql, dataSource));
    }

    public TransferQueue<Object> getResultQueue() {
        return resultQueue;
    }

    public static void setPersistences(HashMap<String,Persistence> persistences) {
        QueryExecutor.persistences = persistences;
    }
}
