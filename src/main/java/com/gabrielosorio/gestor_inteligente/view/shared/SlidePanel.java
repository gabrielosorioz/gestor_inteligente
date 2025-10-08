package com.gabrielosorio.gestor_inteligente.view.shared;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 * Sistema genérico para exibir painéis deslizantes em qualquer direção.
 * Calcula automaticamente posições e dimensões baseado no conteúdo.
 *
 * Exemplo de uso:
 * <pre>
 * SlidePanel slidePanel = new SlidePanel.Builder(mainContainer, contentPane)
 *     .direction(SlideDirection.RIGHT)
 *     .shadowOverlay(shadowPane)
 *     .animationDuration(Duration.millis(400))
 *     .onShow(() -> System.out.println("Painel exibido"))
 *     .build();
 *
 * slidePanel.show();
 * slidePanel.hide();
 * slidePanel.toggle();
 * </pre>
 */
public class SlidePanel {

    // Configurações
    private final AnchorPane container;
    private final Node content;
    private final SlideDirection direction;
    private final Duration animationDuration;
    private final Duration fadeDuration;
    private final Pane shadowOverlay;
    private final boolean autoCalculateSize;
    private final Double customWidth;
    private final Double customOffset;

    // Callbacks
    private final Consumer<Boolean> onToggle;
    private final Runnable onShow;
    private final Runnable onHide;

    // Estado
    private boolean isVisible = false;

    // Dimensões calculadas
    private double contentWidth;
    private double hiddenPosition;
    private double visiblePosition;

    private SlidePanel(Builder builder) {
        this.container = builder.container;
        this.content = builder.content;
        this.direction = builder.direction;
        this.animationDuration = builder.animationDuration;
        this.fadeDuration = builder.fadeDuration;
        this.shadowOverlay = builder.shadowOverlay;
        this.autoCalculateSize = builder.autoCalculateSize;
        this.customWidth = builder.customWidth;
        this.customOffset = builder.customOffset;
        this.onToggle = builder.onToggle;
        this.onShow = builder.onShow;
        this.onHide = builder.onHide;

        initialize();
    }

    /**
     * Inicializa o painel calculando dimensões e configurando o layout
     */
    private void initialize() {
        calculateDimensions();
        configureLayout();
        setupShadowInteraction();
    }

    /**
     * Calcula automaticamente as dimensões baseado no conteúdo e direção
     */
    private void calculateDimensions() {
        if (autoCalculateSize) {
            // Força o layout para obter dimensões reais
            content.applyCss();
            if (content instanceof Parent parent) {
                parent.layout();
            }

            contentWidth = customWidth != null ? customWidth :
                    (content.prefWidth(-1) > 0 ? content.prefWidth(-1) : 400);
        } else {
            contentWidth = customWidth != null ? customWidth : 400;
        }

        // Calcula posições baseado na direção
        switch (direction) {
            case RIGHT:
                hiddenPosition = contentWidth;
                visiblePosition = 0;
                break;
            case LEFT:
                hiddenPosition = -contentWidth;
                visiblePosition = 0;
                break;
            case TOP:
                hiddenPosition = -content.prefHeight(-1);
                visiblePosition = 0;
                break;
            case BOTTOM:
                hiddenPosition = content.prefHeight(-1);
                visiblePosition = 0;
                break;
        }
    }

    /**
     * Configura o layout do painel no container
     */
    private void configureLayout() {
        if (!container.getChildren().contains(content)) {
            container.getChildren().add(content);
        }

        // Configura âncoras baseado na direção
        switch (direction) {
            case RIGHT:
                double leftOffset = customOffset != null ? customOffset :
                        Math.max(0, container.getWidth() - contentWidth);
                AnchorPane.setRightAnchor(content, 0.0);
                AnchorPane.setLeftAnchor(content, leftOffset);
                AnchorPane.setTopAnchor(content, 39.0);
                AnchorPane.setBottomAnchor(content, 0.0);

                if (direction.isHorizontal()) {
                    content.setTranslateX(hiddenPosition);
                }
                break;

            case LEFT:
                AnchorPane.setLeftAnchor(content, 0.0);
                AnchorPane.setTopAnchor(content, 0.0);
                AnchorPane.setBottomAnchor(content, 0.0);
                content.setTranslateX(hiddenPosition);
                break;

            case TOP:
                AnchorPane.setTopAnchor(content, 0.0);
                AnchorPane.setLeftAnchor(content, 0.0);
                AnchorPane.setRightAnchor(content, 0.0);
                content.setTranslateY(hiddenPosition);
                break;

            case BOTTOM:
                AnchorPane.setBottomAnchor(content, 0.0);
                AnchorPane.setLeftAnchor(content, 0.0);
                AnchorPane.setRightAnchor(content, 0.0);
                content.setTranslateY(hiddenPosition);
                break;
        }

        // Garante que o conteúdo esteja oculto inicialmente
        content.setVisible(false);
    }

    /**
     * Configura a interação com o shadow overlay
     */
    private void setupShadowInteraction() {
        if (shadowOverlay != null) {
            shadowOverlay.setVisible(false);
            shadowOverlay.setOnMouseClicked(e -> hide());
        }
    }

    /**
     * Exibe o painel com animação
     */
    public void show() {
        if (isVisible) return;

        content.setVisible(true);
        animate(visiblePosition, 0.2);

        if (shadowOverlay != null) {
            shadowOverlay.setVisible(true);
        }

        isVisible = true;

        if (onShow != null) {
            onShow.run();
        }

        if (onToggle != null) {
            onToggle.accept(isVisible);
        }
    }

    /**
     * Oculta o painel com animação
     */
    public void hide() {
        if (!isVisible) return;

        animate(hiddenPosition, 0.0);
        isVisible = false;

        if (onHide != null) {
            onHide.run();
        }

        if (onToggle != null) {
            onToggle.accept(isVisible);
        }
    }

    /**
     * Alterna entre exibir e ocultar
     */
    public void toggle() {
        if (isVisible) {
            hide();
        } else {
            show();
        }
    }

    /**
     * Executa a animação de deslizar
     */
    private void animate(double targetPosition, double fadeToValue) {
        // Animação de translação
        TranslateTransition translateTransition = new TranslateTransition(animationDuration, content);

        if (direction.isHorizontal()) {
            translateTransition.setToX(targetPosition);
        } else {
            translateTransition.setToY(targetPosition);
        }

        translateTransition.setOnFinished(e -> {
            if (!isVisible) {
                content.setVisible(false);
            }
        });

        // Animação de fade do shadow
        if (shadowOverlay != null) {
            FadeTransition fadeTransition = new FadeTransition(fadeDuration, shadowOverlay);
            fadeTransition.setFromValue(shadowOverlay.getOpacity());
            fadeTransition.setToValue(fadeToValue);

            fadeTransition.setOnFinished(e -> {
                if (fadeToValue == 0.0) {
                    shadowOverlay.setVisible(false);
                }
            });

            fadeTransition.play();
        }

        translateTransition.play();
    }

    // Getters
    public boolean isVisible() {
        return isVisible;
    }

    public Node getContent() {
        return content;
    }

    /**
     * Builder para construção fluente do SlidePanel
     */
    public static class Builder {
        private final AnchorPane container;
        private final Node content;

        // Configurações com valores padrão
        private SlideDirection direction = SlideDirection.RIGHT;
        private Duration animationDuration = Duration.millis(400);
        private Duration fadeDuration = Duration.millis(200);
        private Pane shadowOverlay = null;
        private boolean autoCalculateSize = true;
        private Double customWidth = null;
        private Double customOffset = null;

        // Callbacks
        private Consumer<Boolean> onToggle = null;
        private Runnable onShow = null;
        private Runnable onHide = null;

        /**
         * Cria um novo builder
         * @param container Container principal onde o painel será exibido
         * @param content Conteúdo a ser exibido no painel
         */
        public Builder(AnchorPane container, Node content) {
            if (container == null || content == null) {
                throw new IllegalArgumentException("Container e content não podem ser null");
            }
            this.container = container;
            this.content = content;
        }

        public Builder direction(SlideDirection direction) {
            this.direction = direction;
            return this;
        }

        public Builder animationDuration(Duration duration) {
            this.animationDuration = duration;
            return this;
        }

        public Builder fadeDuration(Duration duration) {
            this.fadeDuration = duration;
            return this;
        }

        public Builder shadowOverlay(Pane shadow) {
            this.shadowOverlay = shadow;
            return this;
        }

        /**
         * Define se deve calcular o tamanho automaticamente
         */
        public Builder autoCalculateSize(boolean auto) {
            this.autoCalculateSize = auto;
            return this;
        }

        /**
         * Define uma largura customizada (sobrescreve cálculo automático)
         */
        public Builder width(double width) {
            this.customWidth = width;
            return this;
        }

        /**
         * Define o offset da borda (útil para deixar espaço lateral)
         */
        public Builder offset(double offset) {
            this.customOffset = offset;
            return this;
        }

        public Builder onToggle(Consumer<Boolean> callback) {
            this.onToggle = callback;
            return this;
        }

        public Builder onShow(Runnable callback) {
            this.onShow = callback;
            return this;
        }

        public Builder onHide(Runnable callback) {
            this.onHide = callback;
            return this;
        }

        public SlidePanel build() {
            return new SlidePanel(this);
        }
    }

    /**
     * Enum para definir a direção do slide
     */
    public enum SlideDirection {
        RIGHT,  // Desliza da direita para esquerda
        LEFT,   // Desliza da esquerda para direita
        TOP,    // Desliza de cima para baixo
        BOTTOM; // Desliza de baixo para cima

        public boolean isHorizontal() {
            return this == RIGHT || this == LEFT;
        }

        public boolean isVertical() {
            return this == TOP || this == BOTTOM;
        }
    }
}