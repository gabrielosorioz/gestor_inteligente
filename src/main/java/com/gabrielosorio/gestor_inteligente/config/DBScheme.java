package com.gabrielosorio.gestor_inteligente.config;

public enum DBScheme {

    H2("jdbc:h2:./src/main/java/com/gabrielosorio/gestor_inteligente/data/commerce_db","sa","12","org.h2.Driver","h2"),
    POSTGRESQL("jdbc:postgresql://localhost:5432/gestor_inteligente","postgres","postgres","org.postgresql.Driver","postgresql");

    private String url;
    private String username;
    private String password;
    private String driver;
    private String prefix;

    DBScheme(String url, String username, String password, String driver, String prefix) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDriver() {
        return driver;
    }

    public String getPrefix() {
        return prefix;
    }
}
