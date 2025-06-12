package com.example.licenta.logic;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;


public class FormulaValidator {
    private String input;
    private int index;
    private String normalizedFormula; // Store the normalized formula


    /**
     * Validates if the given string is a well-formed formula in propositional logic.
     * @param s The input string to validate
     * @return true if the string is a valid formula, false otherwise
     */
    public boolean isFormula(String s) {
        // Normalize the input formula and store it
        normalizedFormula = s.replaceAll("\\s+", "")
                .replace("¬", "!")
                .replace("∧", "&")
                .replace("∨", "|")
                .replace("→", ">")
                .replace("↔", "=");

        input = normalizedFormula;
        index = 0;
        boolean result = parseFormula();
        return result && index == input.length();
    }

    private boolean parseFormula() {
        if (!parseTerm()) {
            return false;
        }

        // After a term, we might have an operator and another formula
        while (index < input.length() && isOperator(input.charAt(index))) {
            index++;
            if (!parseTerm()) {
                return false;
            }
        }

        return true;
    }

    private boolean parseTerm() {
        if (index >= input.length()) {
            return false;
        }

        char ch = input.charAt(index);
        if (ch == '!') {
            index++;
            return parseTerm();
        }

        if (ch == '(') {
            index++;
            if (!parseFormula()) {
                return false;
            }
            if (index >= input.length() || input.charAt(index) != ')') {
                return false;
            }
            index++;
            return true;
        }

        if (Character.isLetter(ch)) {
            index++;
            return true;
        }

        return false;
    }

    private boolean isOperator(char ch) {
        return ch == '&' || ch == '|' || ch == '>' || ch == '=';
    }

    /**
     * Returns the normalized version of the formula (with standardized operators)
     * @return The normalized formula string
     */
    public String getNormalizedFormula() {
        return normalizedFormula;
    }

    /**
     * Returns the original formula with proper operator symbols restored
     * @return The pretty-printed formula
     */
    public String getPrettyFormula() {
        return normalizedFormula
                .replace("!", "¬")
                .replace("&", "∧")
                .replace("|", "∨")
                .replace(">", "→")
                .replace("=", "↔");
    }
}