package com.gabrielosorio.gestor_inteligente.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class QueryLoader {

    private Properties queries = new Properties();
    private final String dbPrefix;
    private Logger log = Logger.getLogger(getClass().getName());

    public QueryLoader(DBScheme database) {
        this.dbPrefix = database.getPrefix() + ".";
        try(InputStream input = getClass().getClassLoader().getResourceAsStream("queries.properties")) {
            if(input == null){
                throw new FileNotFoundException("Queries file not found!.");
            }
            queries.load(input);
        } catch (IOException e) {
            log.severe("Error loading query file " + e.getLocalizedMessage() + " " + e.getCause());
            throw new RuntimeException(e);
        }
    }

    public String getQuery(String queryKey){
        String query = queries.getProperty(dbPrefix + queryKey);
        log.info(dbPrefix + queryKey + " " + query);
        return query;
    }
}
