package com.example.licenta.logic;

import java.util.*;
import java.util.regex.*;

public class FormulaEvaluator {

    public static boolean evaluate(String formula, Map<String, Boolean> valuation) {
        String replaced = formula;
        for (Map.Entry<String, Boolean> entry : valuation.entrySet()) {
            replaced = replaced.replaceAll("\\b" + Pattern.quote(entry.getKey()) + "\\b", entry.getValue() ? "1" : "0");
        }

        return eval(replaced);
    }

    public static boolean eval(String expr) {
        // Înlocuiește simbolurile logice cu echivalentele lor Java
        expr = expr.replaceAll("¬", "!");
        expr = expr.replaceAll("∨", "|");
        expr = expr.replaceAll("∧", "&");

        // Elimină spațiile
        expr = expr.replaceAll("\\s+", "");

        // Verifică pentru valorile 1 și 0
        if (expr.equals("1")) return true;
        if (expr.equals("0")) return false;

        // Cazul pentru negare
        if (expr.startsWith("!")) {
            return !eval(expr.substring(1));
        }

        // Cazul pentru paranteze și operatori logici
        if (expr.startsWith("(") && expr.endsWith(")")) {
            int level = 0;
            for (int i = 1; i < expr.length() - 1; i++) {
                char c = expr.charAt(i);
                if (c == '(') level++;
                if (c == ')') level--;
                if (level == 0) {
                    // Evaluare operatori logici
                    if (expr.startsWith("->", i)) {
                        return !eval(expr.substring(1, i)) || eval(expr.substring(i + 2, expr.length() - 1));
                    }
                    if (expr.startsWith("<->", i)) {
                        return eval(expr.substring(1, i)) == eval(expr.substring(i + 3, expr.length() - 1));
                    }
                    if (expr.charAt(i) == '&') {
                        return eval(expr.substring(1, i)) && eval(expr.substring(i + 1, expr.length() - 1));
                    }
                    if (expr.charAt(i) == '|') {
                        return eval(expr.substring(1, i)) || eval(expr.substring(i + 1, expr.length() - 1));
                    }
                }
            }
        }

        throw new RuntimeException("Expresie invalidă: " + expr);
    }
    /**
     * Extracts variables from a logical formula.
     *
     * @param formula The logical formula as a string.
     * @return A set of variable names found in the formula.
     */

    public static Set<String> extractVariables(String formula) {
        Set<String> vars = new TreeSet<>();
        Matcher matcher = Pattern.compile("\\b[a-zA-Z][a-zA-Z0-9]*\\b").matcher(formula);
        while (matcher.find()) {
            vars.add(matcher.group());
        }
        return vars;
    }
}
