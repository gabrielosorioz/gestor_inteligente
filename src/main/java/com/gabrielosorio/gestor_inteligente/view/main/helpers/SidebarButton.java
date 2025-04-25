package com.gabrielosorio.gestor_inteligente.view.main.helpers;

public class SidebarButton {

    private final String label;
    private final String iconPath;
    private final String hoverIconPath;

    private final Runnable action;


    public SidebarButton(String label, String iconPath, String hoverIconPath, Runnable action) {
        this.label = label;
        this.iconPath = iconPath;
        this.hoverIconPath = hoverIconPath;
        this.action = action;
    }

    public String getLabel() {
        return label;
    }

    public String getIconPath() {
        return iconPath;
    }

    public Runnable getAction() {
        return action;
    }

    public String getHoverIconPath() {
        return hoverIconPath;
    }

}
