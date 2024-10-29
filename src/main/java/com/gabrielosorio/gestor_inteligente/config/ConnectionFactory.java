package com.gabrielosorio.gestor_inteligente.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Logger;

public class ConnectionFactory {

    private final HikariDataSource dataSource;
    private static ConnectionFactory instance;
    private static final Logger log = Logger.getLogger(Connection.class.getName());
    private static DBScheme dbScheme = null;

    private ConnectionFactory(DBScheme dbScheme){
        this.dbScheme = dbScheme;
        this.dataSource = getDataSourceCP(dbScheme);
    }

    private static HikariDataSource getDataSourceCP(DBScheme dbScheme){
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

    public static DBScheme getDBScheme() {
        return dbScheme;
    }

    public static ConnectionFactory getInstance(){
        synchronized (ConnectionFactory.class){
            if(Objects.isNull(instance)){
                instance = new ConnectionFactory(DBScheme.POSTGRESQL);
            } else {
                log.info("Connection already established.");
            }
            return instance;
        }
    }


}
