package com.gabrielosorio.gestor_inteligente.view.table;


/**
 * Interface funcional para callback antes da atualização do modelo.
 *
 * @param <T> tipo do item da tabela
 * @param <V> tipo do valor a ser atualizado
 */
@FunctionalInterface
public interface BeforeModelUpdateCallback<T, V> {
    /**
     * Executado antes da atualização do modelo.
     *
     * @param item o item que será atualizado
     * @param columnId o ID da coluna a ser editada
     * @param oldValue o valor atual
     * @param newValue o novo valor a ser aplicado
     * @return true para permitir a atualização, false para cancelar
     */
    boolean beforeUpdate(T item, String columnId, V oldValue, V newValue);
}