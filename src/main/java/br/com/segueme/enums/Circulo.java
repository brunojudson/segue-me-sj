package br.com.segueme.enums;

public enum Circulo {
    ROSA("icon-rose"),
    AZUL("icon-blue"),
    VERDE("icon-green"),
    VERMELHO("icon-red"),
    AMARELO("icon-yellow");

    private final String cssIconClass;

    Circulo(String cssIconClass) {
        this.cssIconClass = cssIconClass;
    }

    public String getCssIconClass() {
        return cssIconClass;
    }
}