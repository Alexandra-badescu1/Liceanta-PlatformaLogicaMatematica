package com.example.licenta.normalForm;

public class TransformationStep {
    private String description;
    private String formula;

    public TransformationStep(String description, String formula) {
        this.description = description;
        this.formula = formula;
    }

    public String getDescription() {
        return description;
    }

    public String getFormula() {
        return formula;
    }
}
