package com.example.licenta.logic;

import java.util.*;

public class LogicalDistributivity {

    public String distributeOrOverAnd(String formula) {
        return distribute(formula, '|', '&');
    }

    public String distributeAndOverOr(String formula) {
        return distribute(formula, '&', '|');
    }

    private String distribute(String formula, char outerOp, char innerOp) {
        formula = removeOuterParentheses(formula.trim());

        List<String> terms = splitByMainOperator(formula, outerOp);
        if (terms.size() == 1) {
            if (formula.contains("(")) {
                int start = formula.indexOf('(');
                int end = findMatchingParenthesis(formula, start);
                String inner = formula.substring(start + 1, end);
                String before = formula.substring(0, start);
                String after = formula.substring(end + 1);
                String distributed = distribute(inner, outerOp, innerOp);
                return before + "(" + distributed + ")" + after;
            }
            return formula;
        }

        List<List<String>> distributedTerms = new ArrayList<>();
        for (String term : terms) {
            String cleaned = removeOuterParentheses(term.trim());
            if (findMainOperator(cleaned, innerOp) != -1) {
                distributedTerms.add(splitByMainOperator(distribute(cleaned, outerOp, innerOp), innerOp));
            } else {
                distributedTerms.add(Collections.singletonList(distribute(cleaned, outerOp, innerOp)));
            }
        }

        List<String> combinations = cartesianProduct(distributedTerms, outerOp);
        return joinByOperator(combinations, innerOp);
    }

    private List<String> cartesianProduct(List<List<String>> lists, char op) {
        List<String> result = new ArrayList<>();
        cartesianHelper(lists, result, 0, "", op);
        return result;
    }

    private void cartesianHelper(List<List<String>> lists, List<String> result, int depth, String current, char op) {
        if (depth == lists.size()) {
            result.add(current);
            return;
        }

        for (String s : lists.get(depth)) {
            String next = current.isEmpty() ? s : "(" + current + op + s + ")";
            cartesianHelper(lists, result, depth + 1, next, op);
        }
    }

    private List<String> splitByMainOperator(String formula, char op) {
        List<String> parts = new ArrayList<>();
        int level = 0, start = 0;
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (c == '(') level++;
            else if (c == ')') level--;
            else if (c == op && level == 0) {
                parts.add(formula.substring(start, i));
                start = i + 1;
            }
        }
        parts.add(formula.substring(start));
        return parts;
    }

    private int findMainOperator(String formula, char op) {
        int level = 0;
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (c == '(') level++;
            else if (c == ')') level--;
            else if (c == op && level == 0) return i;
        }
        return -1;
    }

    private int findMatchingParenthesis(String s, int start) {
        int count = 1;
        for (int i = start + 1; i < s.length(); i++) {
            if (s.charAt(i) == '(') count++;
            else if (s.charAt(i) == ')') count--;
            if (count == 0) return i;
        }
        return -1;
    }

    private String removeOuterParentheses(String formula) {
        while (formula.startsWith("(") && findMatchingParenthesis(formula, 0) == formula.length() - 1) {
            formula = formula.substring(1, formula.length() - 1).trim();
        }
        return formula;
    }

    private String joinByOperator(List<String> terms, char op) {
        if (terms.size() == 1) return terms.get(0);
        return String.join(String.valueOf(op), terms);
    }
}
