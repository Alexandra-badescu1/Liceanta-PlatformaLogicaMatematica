package com.example.licenta.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class SubformulaExtractor {
    public List<String> extractSubformulas(String formula) {
        List<String> subformulas = new ArrayList<>();
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < formula.length(); i++) {
            char ch = formula.charAt(i);

            if (ch == '(') {
                stack.push(i);
            }
            else if (ch == ')') {
                if (!stack.isEmpty()) {
                    int start = stack.pop();
                    String sub = formula.substring(start, i + 1);
                    subformulas.add(sub);
                }
            }
        }
        // Add atomic formulas (single variables)
        for (int i = 0; i < formula.length(); i++) {
            char ch = formula.charAt(i);
            if (Character.isLetter(ch) && (i == 0 || !Character.isLetter(formula.charAt(i - 1)))) {
                int j = i;
                while (j < formula.length() && Character.isLetterOrDigit(formula.charAt(j))) {
                    j++;
                }
                String var = formula.substring(i, j);
                if (!subformulas.contains(var)) {
                    subformulas.add(var);
                }
            }
        }

        return subformulas;
    }
}