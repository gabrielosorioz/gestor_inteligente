package com.gabrielosorio.gestor_inteligente.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class ConnectionFactory {

    private final HikariDataSource dataSource;
    private static ConnectionFactory instance;

    private ConnectionFactory(){
        this.dataSource = createH2dataSourceCP();
    }

    private static HikariDataSource createH2dataSourceCP(){
        var config = new HikariConfig();
        config.setJdbcUrl(Scheme.H2_DATABASE.getUrl());
        config.setUsername(Scheme.H2_DATABASE.getUsername());
        config.setDriverClassName(Scheme.H2_DATABASE.getDriver());
        config.setPassword(Scheme.H2_DATABASE.getPassword());
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

    public static ConnectionFactory getInstance(){
        synchronized (ConnectionFactory.class){
            if(Objects.isNull(instance)){
                instance = new ConnectionFactory();
            } else {
                System.out.println("Carregada conenctions");
            }
            return instance;
        }
    }

}
