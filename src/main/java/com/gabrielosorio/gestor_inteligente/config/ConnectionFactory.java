package com.gabrielosorio.gestor_inteligente.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ConnectionFactory {

    private final HikariDataSource dataSource;
    private static final Map<DBScheme, ConnectionFactory> instances = new ConcurrentHashMap<>();
    private final DBScheme dbScheme;

    private ConnectionFactory(DBScheme dbScheme){
        this.dbScheme = dbScheme;
        this.dataSource = createDataSource(dbScheme);
    }

    private static HikariDataSource createDataSource(DBScheme dbScheme){
        var config = new HikariConfig();
        config.setJdbcUrl(dbScheme.getUrl());
        config.setUsername(dbScheme.getUsername());
        config.setDriverClassName(dbScheme.getDriver());
        config.setPassword(dbScheme.getPassword());
        config.setMinimumIdle(10);
        config.setMaximumPoolSize(50);
        return new HikariDataSource(config);

    }

    public Connection getConnection(){
        try{
            return dataSource.getConnection();
        } catch (SQLException e){
            throw new RuntimeException("Failed to get a database connection",e.getCause());
        }
    }

    public DBScheme getDBScheme() {
        return dbScheme;
    }

    public static ConnectionFactory getInstance(DBScheme dbScheme) {
        return instances.computeIfAbsent(dbScheme, ConnectionFactory::new);
    }

}
