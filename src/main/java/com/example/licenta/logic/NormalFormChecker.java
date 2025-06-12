package com.example.licenta.logic;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class NormalFormChecker {
    public static final int FNC = 1;
    public static final int FND = 2;
    public static final int NEITHER = 0;

    public int checkNormalForm(String formula) {
        String normalized = normalizeFormula(formula);

        try {
            if (isCNF(normalized)) return FNC;
            if (isDNF(normalized)) return FND;
        } catch (Exception e) {
            return NEITHER;
        }

        return NEITHER;
    }

    private String normalizeFormula(String formula) {
        return formula.replaceAll("\\s+", "")
                .replace("¬", "!")
                .replace("∧", "&")
                .replace("∨", "|");
    }

    public boolean isCNF(String formula) {
        formula = removeOuterParentheses(formula);
        if (isLiteralOrNegatedLiteral(formula)) return true;

        List<String> clauses = splitTopLevel(formula, '&');
        for (String clause : clauses) {
            if (!isDisjunctionOfLiterals(clause)) return false;
        }
        return true;
    }

    public boolean isDNF(String formula) {
        formula = removeOuterParentheses(formula);
        if (isLiteralOrNegatedLiteral(formula)) return true;

        List<String> terms = splitTopLevel(formula, '|');
        for (String term : terms) {
            if (!isConjunctionOfLiterals(term)) return false;
        }
        return true;
    }

    private boolean isDisjunctionOfLiterals(String clause) {
        clause = removeOuterParentheses(clause);
        if (isLiteralOrNegatedLiteral(clause)) return true;

        List<String> literals = splitTopLevel(clause, '|');
        for (String literal : literals) {
            if (!isLiteralOrNegatedLiteral(literal)) return false;
        }
        return true;
    }

    private boolean isConjunctionOfLiterals(String term) {
        term = removeOuterParentheses(term);
        if (isLiteralOrNegatedLiteral(term)) return true;

        List<String> literals = splitTopLevel(term, '&');
        for (String literal : literals) {
            if (!isLiteralOrNegatedLiteral(literal)) return false;
        }
        return true;
    }

    private boolean isLiteralOrNegatedLiteral(String expr) {
        expr = removeOuterParentheses(expr).trim().replaceAll("\\s+", "");
        if (expr.startsWith("!")) {
            return isSimpleLiteral(expr.substring(1));
        }
        return isSimpleLiteral(expr);
    }

    private boolean isSimpleLiteral(String expr) {
        return expr.matches("[a-zA-Z][a-zA-Z0-9_]*");
    }

    private String removeOuterParentheses(String expr) {
        expr = expr.trim();
        while (expr.startsWith("(") && expr.endsWith(")") && hasBalancedParentheses(expr)) {
            expr = expr.substring(1, expr.length() - 1).trim();
        }
        return expr;
    }

    private boolean hasBalancedParentheses(String expr) {
        int balance = 0;
        for (char c : expr.toCharArray()) {
            if (c == '(') balance++;
            else if (c == ')') balance--;
            if (balance < 0) return false;
        }
        return balance == 0;
    }

    private List<String> splitTopLevel(String expr, char delimiter) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        int start = 0;

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if (c == delimiter && depth == 0) {
                parts.add(expr.substring(start, i).trim());
                start = i + 1;
            }
        }

        if (start < expr.length()) {
            parts.add(expr.substring(start).trim());
        }

        return parts;
    }
}
