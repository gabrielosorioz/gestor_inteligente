package com.gabrielosorio.gestor_inteligente.model;

import java.util.Optional;

public class Supplier {

    private long id;
    private String name;
    private Optional<String> cnpj;
    private Optional<String> address;
    private Optional<String> cellPhone;
    private Optional<String> email;

    public Supplier(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Supplier(){}

    public Optional<String> getCnpj() {
        return cnpj;
    }

    public void setCnpj(Optional<String> cnpj) {
        this.cnpj = cnpj;
    }

    public Optional<String> getAddress() {
        return address;
    }

    public void setAddress(Optional<String> address) {
        this.address = address;
    }

    public Optional<String> getCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(Optional<String> cellPhone) {
        this.cellPhone = cellPhone;
    }

    public Optional<String> getEmail() {
        return email;
    }

    public void setEmail(Optional<String> email) {
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Supplier{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
