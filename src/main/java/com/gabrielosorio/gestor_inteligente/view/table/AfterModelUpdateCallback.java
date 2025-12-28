package com.gabrielosorio.gestor_inteligente.view.table;

/**
 * Interface funcional para callback após atualização do modelo.
 *
 * @param <T> tipo do item da tabela
 * @param <V> tipo do valor editado
 */
@FunctionalInterface
public interface AfterModelUpdateCallback<T, V> {
    /**
     * Executado após a atualização do modelo.
     *
     * @param item o item que foi atualizado
     * @param columnId o ID da coluna editada
     * @param newValue o novo valor após atualização
     */
    void onUpdate(T item, String columnId, V newValue);
}
