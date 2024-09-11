package com.gabrielosorio.gestor_inteligente.config;

public enum Scheme {

    H2_DATABASE("jdbc:h2:./src/main/java/com/gabrielosorio/gestor_inteligente/data/commerce_db","sa","12","org.h2.Driver");

    private String url;
    private String username;
    private String password;
    private String driver;

    Scheme(String url, String username, String password, String driver) {
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
}
