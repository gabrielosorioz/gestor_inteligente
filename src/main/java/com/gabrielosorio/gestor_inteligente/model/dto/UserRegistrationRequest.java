package com.gabrielosorio.gestor_inteligente.model.dto;

public class UserRegistrationRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String cellphone;
    private String cpf;
    private String password;
    private String confirmPassword;

    public UserRegistrationRequest() {}

    public UserRegistrationRequest(String firstName, String lastName, String email,
                                   String cellphone, String cpf, String password, String confirmPassword) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.cellphone = cellphone;
        this.cpf = cpf;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    // Getters e Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCellphone() { return cellphone; }
    public void setCellphone(String cellphone) { this.cellphone = cellphone; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}