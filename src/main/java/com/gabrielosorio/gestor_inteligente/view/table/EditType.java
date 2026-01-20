package com.gabrielosorio.gestor_inteligente.view.table;

/**
 * Tipos de edição disponíveis para colunas editáveis
 */
public enum EditType {
    /**
     * Campo numérico inteiro (quantidade, idade, etc)
     * Aceita apenas dígitos
     */
    INTEGER,

    /**
     * Campo monetário formatado (R$ 1.234,56)
     * Formatação automática enquanto digita
     */
    MONETARY
}