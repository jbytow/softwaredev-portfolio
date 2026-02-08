package com.portfolio.entity;

public enum Category {
    PERSONAL_PROJECT("Personal Project", "Projekt Osobisty"),
    PROFESSIONAL_PROJECT("Professional", "Profesjonalny");

    private final String labelEn;
    private final String labelPl;

    Category(String labelEn, String labelPl) {
        this.labelEn = labelEn;
        this.labelPl = labelPl;
    }

    public String getLabelEn() {
        return labelEn;
    }

    public String getLabelPl() {
        return labelPl;
    }

    public String getLabel(String locale) {
        return "pl".equalsIgnoreCase(locale) ? labelPl : labelEn;
    }
}
