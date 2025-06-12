package com.example.licenta.logic;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component
public class TruthTableGenerator {
    public List<Map<String, String>> generateTruthTable(Set<String> variables, String formula) {
        List<Map<String, String>> truthTable = new ArrayList<>();
        int numVars = variables.size();
        int numRows = (int) Math.pow(2, numVars);
        // Extract all subformulas including the main formula
        SubformulaExtractor extractor = new SubformulaExtractor();
        List<String> allFormulas = extractor.extractSubformulas(formula);
        allFormulas.add(formula); // Add the main formula
        // Sort formulas by complexity (simple to complex)
        allFormulas.sort((f1, f2) -> Integer.compare(f1.length(), f2.length()));
        List<String> varList = new ArrayList<>(variables);
        for (int i = 0; i < numRows; i++) {
            Map<String, Boolean> valuation = new HashMap<>();
            Map<String, String> row = new LinkedHashMap<>();
            // Assign truth values to variables
            for (int j = 0; j < numVars; j++) {
                boolean value = (i & (1 << (numVars - j - 1))) != 0;
                valuation.put(varList.get(j), value);
                row.put(varList.get(j), value ? "1" : "0");
            }
            // Evaluate all subformulas
            for (String subformula : allFormulas) {
                try {
                    boolean value = evaluate(subformula, new HashMap<>(valuation));
                    row.put(subformula, value ? "1" : "0");
                } catch (Exception e) {
                    row.put(subformula, "E"); // Mark evaluation errors
                }
            }
            truthTable.add(row);
        }
        return truthTable;
    }

    public boolean evaluate(String formula, Map<String, Boolean> valuation) {
        // Replace variables with their values
        String replaced = formula;
        for (Map.Entry<String, Boolean> entry : valuation.entrySet()) {
            replaced = replaced.replaceAll("\\b" + Pattern.quote(entry.getKey()) + "\\b",
                    entry.getValue() ? "1" : "0");
        }

        // Convert to Java logical operators
        replaced = replaced.replace("¬", "!")
                .replace("∧", "&&")
                .replace("∨", "||")
                .replace("→", "->")
                .replace("↔", "==");

        return evaluateExpression(replaced);
    }

    private boolean evaluateExpression(String expr) {
        expr = expr.replaceAll("\\s+", "");

        if (expr.equals("1")) return true;
        if (expr.equals("0")) return false;

        // Not operator
        if (expr.startsWith("!")) {
            return !evaluateExpression(expr.substring(1));
        }

        // Handle outer parentheses
        if (expr.startsWith("(") && expr.endsWith(")")) {
            int depth = 0;
            boolean isWrapped = true;
            for (int i = 0; i < expr.length(); i++) {
                if (expr.charAt(i) == '(') depth++;
                else if (expr.charAt(i) == ')') depth--;
                if (depth == 0 && i < expr.length() - 1) {
                    isWrapped = false;
                    break;
                }
            }
            if (isWrapped) {
                return evaluateExpression(expr.substring(1, expr.length() - 1));
            }
        }

        // Evaluate by precedence: ↔, →, ∨, ∧
        String[] operators = {"==", "->", "||", "&&"};

        for (String op : operators) {
            int depth = 0;
            for (int i = 0; i < expr.length() - 1; i++) {
                char c = expr.charAt(i);
                if (c == '(') depth++;
                else if (c == ')') depth--;
                else if (depth == 0 && expr.startsWith(op, i)) {
                    String left = expr.substring(0, i);
                    String right = expr.substring(i + op.length());
                    switch (op) {
                        case "==":
                            return evaluateExpression(left) == evaluateExpression(right);
                        case "->":
                            return !evaluateExpression(left) || evaluateExpression(right);
                        case "||":
                            return evaluateExpression(left) || evaluateExpression(right);
                        case "&&":
                            return evaluateExpression(left) && evaluateExpression(right);
                    }
                }
            }
        }

        throw new RuntimeException("Invalid expression: " + expr);
    }

}