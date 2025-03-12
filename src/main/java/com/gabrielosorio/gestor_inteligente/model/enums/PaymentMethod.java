package com.gabrielosorio.gestor_inteligente.model.enums;

public enum PaymentMethod {

    PIX(1,"PIX"),
    DEBITO(2,"DÉBITO"),
    CREDIT0(3,"CRÉDITO"),
    DINHEIRO(4,"DINHEIRO");

    private final long id;
    private final String description;

    PaymentMethod(int id, String description){
        this.id = id;
        this.description = description;
    }

    public static PaymentMethod getMethodById(long id){

        for(PaymentMethod payMethod : PaymentMethod.values()){
            if(payMethod.getId() == id){
                return payMethod;
            }
        }
        throw new IllegalArgumentException("Invalid payment method ID: " + id);

    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}
