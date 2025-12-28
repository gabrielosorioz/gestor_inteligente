package com.gabrielosorio.gestor_inteligente.view.table;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para configurar colunas editáveis em TableViews.
 * Permite que o usuário edite valores diretamente na célula.
 *
 * Exemplo de uso:
 * <pre>
 * {@code
 * @TableColumnConfig(header = "Quantidade", order = 3)
 * @EditableColumn(
 *     editType = EditType.INTEGER,
 *     propertyUpdater = "setQuantity",
 *     defaultValue = "1"
 * )
 * private SimpleIntegerProperty quantity;
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface EditableColumn {

    /**
     * Tipo de edição da coluna
     */
    EditType editType();

    /**
     * Nome do método setter para atualizar o valor no objeto
     * Ex: "setQuantity" chamará item.setQuantity(newValue)
     */
    String propertyUpdater();

    /**
     * Valor padrão quando o campo está vazio
     */
    String defaultValue() default "";

    /**
     * Símbolo de moeda (apenas para EditType.MONETARY)
     */
    String currencySymbol() default "R$";

    /**
     * Largura preferencial do campo de edição
     */
    double fieldWidth() default 95.0;

}