package com.gabrielosorio.gestor_inteligente.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class QueryLoader {

    private Properties queries = new Properties();
    private final String dbPrefix;
    private final Logger log;

    public QueryLoader(DBScheme database) {
        this.dbPrefix = database.getPrefix() + ".";
        this.log = Logger.getLogger(getClass().getSimpleName()); // Apenas o nome da classe no log

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("queries.properties")) {
            if (input == null) {
                throw new FileNotFoundException("Queries file not found!");
            }
            queries.load(input);
            logInfo("Query file loaded successfully.");
        } catch (IOException e) {
            logError("Error loading query file", e);
            throw new RuntimeException(e);
        }
    }

    public String getQuery(String queryKey) {
        String query = queries.getProperty(dbPrefix + queryKey);
        logInfo("Retrieved query: " + dbPrefix + queryKey + " -> " + query);
        return query;
    }

    /**
     * Métodos auxiliares para log com nome da classe e método.
     */
    private void logInfo(String message) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        log.info(String.format("[%s#%s] %s", getClass().getSimpleName(), caller.getMethodName(), message));
    }

    private void logError(String message, Exception e) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        log.severe(String.format("[%s#%s] %s - %s", getClass().getSimpleName(), caller.getMethodName(), message, e.getMessage()));
    }
}
