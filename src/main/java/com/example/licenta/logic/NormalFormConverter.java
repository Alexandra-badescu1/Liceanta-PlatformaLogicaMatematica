package com.example.licenta.logic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * This class is responsible for converting logical formulas to their normal forms (CNF or DNF).
 * It includes methods for normalizing, eliminating implications, applying De Morgan's laws,
 * and simplifying the formulas.
 */

@Component
public class NormalFormConverter {
    @Autowired
    private FormulaValidator validator;
    private final LogicalDistributivity logic = new LogicalDistributivity();



    public String toNormalForm(String formula, boolean targetCNF) {
        if (!validator.isFormula(formula)) {
            throw new IllegalArgumentException("Invalid formula");
        }

        // 1. Normalize
        String normalized = normalizeFormula(formula);

        // 2. Eliminate implications and biconditionals
        normalized = eliminateImplications(normalized);

        // 3. Apply De Morgan's
        normalized = applyDeMorgans(normalized);

        // 4. Apply basic simplifications
        normalized = applyBasicSimplifications(normalized);

        // 5. Distribute based on target form
        normalized = targetCNF ? logic.distributeOrOverAnd(normalized)
                : logic.distributeAndOverOr(normalized);

        // 5.5 Clean up parentheses after distribution
        // 5.5 Flatten after distribution
        normalized = cleanParentheses(normalized);
        normalized = flattenAfterDistribution(normalized, targetCNF);

        // 6. Final simplification
        normalized = targetCNF ? simplifyCNF(normalized) : simplifyDNF(normalized);

        // 7. Standard symbols
        return convertToStandardSymbols(normalized);
    }

    private String cleanParentheses(String formula) {
        if (formula == null || formula.isEmpty()) {
            return formula;
        }

        formula = removeOuterParentheses(formula);

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < formula.length()) {
            char c = formula.charAt(i);
            if (c == '(') {
                int end = findMatchingParenthesis(formula, i);
                String inner = cleanParentheses(formula.substring(i + 1, end));

                // Only flatten if the entire content is the same operator
                if (canFlatten(inner)) {
                    result.append(inner);
                } else {
                    result.append('(').append(inner).append(')');
                }
                i = end + 1;
            } else {
                result.append(c);
                i++;
            }
        }

        return result.toString();
    }
    private String flattenAfterDistribution(String formula, boolean isCNF) {
        char op = isCNF ? '∨' : '∧';
        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < formula.length()) {
            char c = formula.charAt(i);
            if (c == '(') {
                int end = findMatchingParenthesis(formula, i);
                String inner = flattenAfterDistribution(formula.substring(i + 1, end), isCNF);

                if (canFlatten(inner) ){
                    // Safe to flatten
                    result.append(inner);
                } else {
                    result.append('(').append(inner).append(')');
                }
                i = end + 1;
            } else {
                result.append(c);
                i++;
            }
        }

        return result.toString();
    }
    private boolean canFlatten(String expr) {
        if (expr.isEmpty()) return false;

        // Check if all top-level operators are the same
        int topLevelPos = topLevelOperatorPosition(expr);
        if (topLevelPos < 0) return true; // no operators

        char op = expr.charAt(topLevelPos);
        int level = 0;
        for (char c : expr.toCharArray()) {
            if (c == '(') level++;
            else if (c == ')') level--;
            else if (level == 0 && (c == '∧' || c == '∨') && c != op) {
                return false; // different operator at top level
            }
        }
        return true;
    }

    private String removeOuterParentheses(String formula) {
        if (formula == null || formula.isEmpty()) {
            return formula;
        }

        formula = formula.trim();
        while (formula.startsWith("(") && formula.endsWith(")")) {
            // Check if the parentheses span the entire formula
            int matchingIndex = findMatchingParenthesis(formula, 0);
            if (matchingIndex != formula.length() - 1) {
                break; // Parentheses don't span the entire formula
            }

            // Check if removing them would be safe
            String inner = formula.substring(1, formula.length() - 1);
            if (!hasBalancedParentheses(inner)) {
                break;
            }

            // Only remove if there's a single top-level operator
            int topLevelPos = topLevelOperatorPosition(inner);
            if (topLevelPos < 0) {
                // No top-level operator - safe to remove
                formula = inner;
                continue;
            }

            // Check if all operators at top level are the same
            char firstOp = inner.charAt(topLevelPos);
            if (allTopLevelOperatorsSame(inner, firstOp)) {
                formula = inner;
            } else {
                break;
            }
        }
        return formula;
    }

    private boolean allTopLevelOperatorsSame(String expr, char op) {
        int level = 0;
        for (char c : expr.toCharArray()) {
            if (c == '(') level++;
            else if (c == ')') level--;
            else if (level == 0 && (c == '∧' || c == '∨') && c != op) {
                return false;
            }
        }
        return true;
    }


    private String applyBasicSimplifications(String formula) {
        String result = formula;
        String previous;
        do {
            previous = result;
            result = applyIdempotence(result);
            result = applyExcludedMiddleAndContradiction(result);
            result = applyAbsorption(result);
            result = removeDoubleNegations(result);
        } while (!result.equals(previous));
        return result;
    }
    private String removeDoubleNegations(String formula) {
        return formula.replaceAll("!!", "");
    }
    private String eliminateImplications(String formula) {
        LinkedList<Character> chars = new LinkedList<>();
        for (char c : formula.toCharArray()) {
            chars.add(c);
        }

        ListIterator<Character> it = chars.listIterator();
        while (it.hasNext()) {
            char c = it.next();
            if (c == '-') {
                // Check if it's part of -> or <->
                if (it.hasNext() && chars.get(it.nextIndex()) == '>') {
                    it.remove(); // remove '-'
                    char next = it.next(); // should be '>'
                    it.set('|'); // replace '>' with '|'

                    // Now handle the left operand
                    int currentIdx = it.previousIndex() - 1;
                    if (currentIdx >= 0) {
                        // Find the start of the left operand
                        int start = findOperandStart(chars, currentIdx);
                        // Insert negation at the start of left operand
                        chars.add(start, '!');
                        it = chars.listIterator(); // reset iterator after modification
                    }
                }
            } else if (c == '<') {
                // Handle biconditional <-> by converting to (A -> B) & (B -> A)
                if (it.hasNext() && chars.get(it.nextIndex()) == '-'
                        && it.hasNext() && chars.get(it.nextIndex()+1) == '>') {
                    it.remove(); // remove '<'
                    it.next(); // skip '-'
                    it.next(); // skip '>'
                    it.set('&'); // replace '>' with '&'

                    // Now we need to duplicate the operands with implications
                    int currentIdx = it.previousIndex();
                    int rightStart = currentIdx + 1;
                    int rightEnd = findOperandEnd(chars, rightStart);
                    String rightOperand = charsToString(chars, rightStart, rightEnd);

                    int leftEnd = currentIdx - 1;
                    int leftStart = findOperandStart(chars, leftEnd);
                    String leftOperand = charsToString(chars, leftStart, leftEnd);

                    // Replace the whole biconditional with (A -> B) & (B -> A)
                    String replacement = "(" + leftOperand + "->" + rightOperand + ")&(" + rightOperand + "->" + leftOperand + ")";

                    // Modify the list
                    for (int i = leftStart; i < rightEnd; i++) {
                        chars.remove(leftStart);
                    }
                    for (char rc : replacement.toCharArray()) {
                        chars.add(leftStart, rc);
                    }

                    it = chars.listIterator(); // reset iterator
                }
            }
        }

        return charsToString(chars, 0, chars.size());
    }

    private String charsToString(List<Character> chars, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end && i < chars.size(); i++) {
            sb.append(chars.get(i));
        }
        return sb.toString();
    }

    private int findOperandStart(List<Character> chars, int end) {
        if (end < 0) return 0;

        if (chars.get(end) == ')') {
            int balance = 1;
            for (int i = end - 1; i >= 0; i--) {
                if (chars.get(i) == ')') balance++;
                if (chars.get(i) == '(') balance--;
                if (balance == 0) return i;
            }
            return 0;
        }

        // For atomic propositions
        int start = end;
        while (start >= 0) {
            char c = chars.get(start);
            if (c == '&' || c == '|' || c == '(' ||
                    (start - 1 >= 0 && chars.get(start - 1) == '-' && chars.get(start) == '>') ||
                    (start - 2 >= 0 && chars.get(start - 2) == '<' && chars.get(start - 1) == '-' && chars.get(start) == '>')) {
                return start + 1;
            }
            start--;
        }
        return 0;
    }

    private int findOperandEnd(List<Character> chars, int start) {
        if (start >= chars.size()) return chars.size();

        if (chars.get(start) == '(') {
            int balance = 1;
            for (int i = start + 1; i < chars.size(); i++) {
                if (chars.get(i) == '(') balance++;
                if (chars.get(i) == ')') balance--;
                if (balance == 0) return i + 1;
            }
            return chars.size();
        }

        // For atomic propositions
        int end = start;
        while (end < chars.size()) {
            char c = chars.get(end);
            if (c == '&' || c == '|' || c == ')' ||
                    (end + 1 < chars.size() && chars.get(end) == '-' && chars.get(end + 1) == '>') ||
                    (end + 2 < chars.size() && chars.get(end) == '<' && chars.get(end + 1) == '-' && chars.get(end + 2) == '>')) {
                break;
            }
            end++;
        }
        return end;
    }

    // Rest of the methods remain the same as in your original code
    private String normalizeFormula(String formula) {
        return formula.replaceAll("\\s+", "")
                .replace("¬", "!")
                .replace("∧", "&")
                .replace("∨", "|")
                .replace("→", "->")
                .replace("↔", "<->");
    }

    private String convertToStandardSymbols(String formula) {
        if (formula == null || formula.isEmpty()) {
            return formula;
        }
        return formula.replace("!", "¬")
                .replace("&", "∧")
                .replace("|", "∨");
    }

    private String applyDeMorgans(String formula) {
        String result = formula;
        String previous;
        do {
            previous = result;
            result = applyDeMorgansStep(result);
        } while (!result.equals(previous));
        return result;
    }

    private String applyDeMorgansStep(String formula) {
        StringBuilder sb = new StringBuilder();
        int i = 0;

        while (i < formula.length()) {
            if (i + 1 < formula.length() && formula.startsWith("!(", i)) {
                int end = findMatchingParenthesis(formula, i + 1);
                String inner = formula.substring(i + 2, end);

                char topLevelOp = findTopLevelOperator(inner);
                if (topLevelOp == '&' || topLevelOp == '|') {
                    List<String> parts = splitTopLevel(inner, topLevelOp);
                    char newOp = (topLevelOp == '&') ? '|' : '&';

                    StringBuilder newExpr = new StringBuilder("(");
                    for (String part : parts) {
                        newExpr.append("!").append(part).append(newOp);
                    }
                    newExpr.setLength(newExpr.length() - 1); // Remove last operator
                    newExpr.append(")");

                    sb.append(newExpr);
                    i = end + 1;
                    continue;
                }
            }
            sb.append(formula.charAt(i));
            i++;
        }

        return sb.toString().replaceAll("!!","");
    }

    private String simplifyCNF(String formula) {
        formula = applyIdempotence(formula);
        formula = applyExcludedMiddleAndContradiction(formula);
        formula = applyAbsorption(formula);
        List<String> clauses = splitTopLevel(formula, '&');
        Set<String> uniqueClauses = new LinkedHashSet<>();

        for (String clause : clauses) {
            List<String> literals = splitTopLevel(clause, '|');
            Set<String> uniqueLiterals = new LinkedHashSet<>();
            Set<String> literalSet = new HashSet<>();
            boolean isTautology = false;

            for (String literal : literals) {
                literal = literal.trim();
                String negation = literal.startsWith("¬") ? literal.substring(1) : "¬" + literal;
                if (literalSet.contains(negation)) {
                    isTautology = true;
                    break;
                }
                literalSet.add(literal);
                uniqueLiterals.add(literal);
            }

            if (isTautology) {
                // In CNF, clause that is always true doesn't affect conjunction
                continue;
            }

            if (uniqueLiterals.size() == 1) {
                uniqueClauses.add(uniqueLiterals.iterator().next());
            } else {
                uniqueClauses.add("(" + String.join("|", uniqueLiterals) + ")");
            }
        }

        return uniqueClauses.isEmpty() ? "" :
                uniqueClauses.size() == 1 ? uniqueClauses.iterator().next() :
                        String.join("&", uniqueClauses);
    }

    private String simplifyDNF(String formula) {
        formula = applyIdempotence(formula);
        formula = applyExcludedMiddleAndContradiction(formula);
        formula = applyAbsorption(formula);
        // First, remove all outer parentheses that don't serve a purpose
        formula = removeOuterParentheses(formula);

        List<String> terms = splitTopLevel(formula, '|');
        Set<String> uniqueTerms = new LinkedHashSet<>();

        for (String term : terms) {
            term = removeOuterParentheses(term);
            List<String> literals = splitTopLevel(term, '&');
            Set<String> uniqueLiterals = new LinkedHashSet<>();
            Set<String> literalSet = new HashSet<>();
            boolean isContradictory = false;

            for (String literal : literals) {
                literal = literal.trim();
                String negation = literal.startsWith("¬") ? literal.substring(1) : "¬" + literal;
                if (literalSet.contains(negation)) {
                    isContradictory = true;
                    break;
                }
                literalSet.add(literal);
                uniqueLiterals.add(literal);
            }

            if (isContradictory) {
                continue;
            }

            // Build the term - only add parentheses if needed
            if (uniqueLiterals.size() == 1) {
                uniqueTerms.add(uniqueLiterals.iterator().next());
            } else {
                String termStr = String.join("∧", uniqueLiterals);
                // Only add parentheses if the term contains operators
                uniqueTerms.add(uniqueLiterals.size() > 1 ? "(" + termStr + ")" : termStr);
            }
        }

        if (uniqueTerms.isEmpty()) {
            return "";
        }

        // Join all terms with disjunction
        String result = String.join("∨", uniqueTerms);

        // Only add outer parentheses if:
        // 1. There are multiple terms, and
        // 2. The result contains conjunction operators that need grouping
        if (uniqueTerms.size() > 1 && result.contains("∧")) {
            return "(" + result + ")";
        }

        return result;
    }
    // New methods for logical simplifications
    private String applyIdempotence(String formula) {
        // A ∧ A ⇐⇒ A, A ∨ A ⇐⇒ A
        String result = formula;
        String previous;
        do {
            previous = result;
            result = result.replaceAll("\\(([^()]+)\\1\\)", "$1")  // (AA) => A
                    .replaceAll("([^∧∨])(∧|∨)\\1", "$1");    // A∧A => A, A∨A => A
        } while (!result.equals(previous));
        return result;
    }

    private String applyExcludedMiddleAndContradiction(String formula) {
        // A ∨ ¬A ⇐⇒ 1 (tautology), A ∧ ¬A ⇐⇒ 0 (contradiction)
        String result = formula;
        String previous;
        do {
            previous = result;

            // Match patterns like A∨¬A or ¬A∨A => replace with 1
            result = result.replaceAll("([A-Z])\\|¬\\1", "1");
            result = result.replaceAll("¬([A-Z])\\|\\1", "1");

            // Match patterns like A∧¬A or ¬A∧A => replace with 0
            result = result.replaceAll("([A-Z])∧¬\\1", "0");
            result = result.replaceAll("¬([A-Z])∧\\1", "0");

        } while (!result.equals(previous));

        return result;
    }

    private String applyAbsorption(String formula) {
        // A ∧ (A ∨ C) ⇐⇒ A, A ∨ (A ∧ C) ⇐⇒ A
        String result = formula;
        String previous;
        do {
            previous = result;
            // Pattern for A ∧ (A ∨ C)
            result = result.replaceAll("([^∧∨()]+)∧\\(\\1∨([^()]+)\\)", "$1")
                    .replaceAll("([^∧∨()]+)∧\\(([^()]+)∨\\1\\)", "$1");
            // Pattern for A ∨ (A ∧ C)
            result = result.replaceAll("([^∧∨()]+)∨\\(\\1∧([^()]+)\\)", "$1")
                    .replaceAll("([^∧∨()]+)∨\\(([^()]+)∧\\1\\)", "$1");
        } while (!result.equals(previous));
        return result;
    }



    private boolean hasBalancedParentheses(String expr) {
        int count = 0;
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '(') count++;
            if (c == ')') count--;
            if (count < 0) return false;
        }
        return count == 0;
    }

    private int topLevelOperatorPosition(String expr) {
        int depth = 0;
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '(') depth++;
            if (c == ')') depth--;
            if (depth == 0 && (c == '∧' || c == '∨')) {
                return i;
            }
        }
        return -1;
    }

    private List<String> splitTopLevel(String formula, char... operators) {
        if (formula.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Character> ops = new HashSet<>();
        for (char op : operators) {
            ops.add(op);
        }

        List<String> parts = new ArrayList<>();
        int depth = 0;
        int start = 0;

        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (c == '(') {
                depth++;
                continue;
            }
            if (c == ')') {
                depth--;
                continue;
            }

            if (depth == 0 && ops.contains(c)) {
                String part = formula.substring(start, i).trim();
                if (!part.isEmpty()) {
                    parts.add(part);
                }
                start = i + 1;
            }
        }

        String lastPart = formula.substring(start).trim();
        if (!lastPart.isEmpty()) {
            parts.add(lastPart);
        }

        if (parts.isEmpty() && !formula.trim().isEmpty()) {
            parts.add(formula.trim());
        }

        return parts;
    }

    private char findTopLevelOperator(String expr) {
        int depth = 0;
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '(') depth++;
            if (c == ')') depth--;
            if (depth == 0 && (c == '&' || c == '|')) {
                return c;
            }
        }
        return ' ';
    }

    private int findMatchingParenthesis(String expr, int start) {
        int count = 1;
        for (int i = start + 1; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '(') count++;
            if (c == ')') count--;
            if (count == 0) return i;
        }
        return expr.length();
}

}
