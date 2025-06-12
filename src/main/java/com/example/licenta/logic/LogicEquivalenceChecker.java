package com.example.licenta.logic;

import java.util.*;

public class LogicEquivalenceChecker {
    private final FormulaValidator validator = new FormulaValidator();

    public boolean checkAllTautologies(String formula1, String formula2) {
        return checkAssociativity(formula1, formula2)
                && checkCommutativity(formula1, formula2)
                && checkDistributivity(formula1, formula2)
                && checkAbsorption(formula1, formula2)
                && checkIdempotency(formula1, formula2)
                && checkDoubleNegation(formula1)
                && checkExcludedMiddle(formula1)
                && checkDeMorgan(formula1, formula2)
                && checkEquivalence(formula1, formula2)
                && checkImplication(formula1)
                && checkContraposition(formula1)
                && checkPremiseCombination(formula1, formula2)
                && checkPremisePermutation(formula1, formula2);
    }

    // a) Asociativitate
    private boolean checkAssociativity(String f1, String f2) {
        String[] vars = extractVariables(f1 + f2);
        return checkEquivalence(
                buildFormula(f1, "∧", "(" + buildFormula(f2, "∧", vars[2]) + ")"),
                buildFormula("(" + buildFormula(f1, "∧", vars[1]) + ")", "∧", vars[2])
        ) && checkEquivalence(
                buildFormula(f1, "∨", "(" + buildFormula(f2, "∨", vars[2]) + ")"),
                buildFormula("(" + buildFormula(f1, "∨", vars[1]) + ")", "∨", vars[2])
        );
    }

    // b) Comutativitate
    private boolean checkCommutativity(String f1, String f2) {
        return checkEquivalence(
                buildFormula(f1, "∧", f2),
                buildFormula(f2, "∧", f1)
        ) && checkEquivalence(
                buildFormula(f1, "∨", f2),
                buildFormula(f2, "∨", f1)
        );
    }

    // c) Distributivitate
    private boolean checkDistributivity(String f1, String f2) {
        String[] vars = extractVariables(f1 + f2);
        return checkEquivalence(
                buildFormula(f1, "∧", "(" + buildFormula(vars[1], "∨", vars[2]) + ")"),
                buildFormula("(" + buildFormula(f1, "∧", vars[1]) + ")", "∨", "(" + buildFormula(f1, "∧", vars[2]) + ")")
        ) && checkEquivalence(
                buildFormula(f1, "∨", "(" + buildFormula(vars[1], "∧", vars[2]) + ")"),
                buildFormula("(" + buildFormula(f1, "∨", vars[1]) + ")", "∧", "(" + buildFormula(f1, "∨", vars[2]) + ")")
        );
    }

    // d) Absorbtie
    private boolean checkAbsorption(String f1, String f2) {
        return checkEquivalence(
                buildFormula(f1, "∧", "(" + buildFormula(f1, "∨", f2) + ")"),
                f1
        ) && checkEquivalence(
                buildFormula(f1, "∨", "(" + buildFormula(f1, "∧", f2) + ")"),
                f1
        );
    }

    // e) Idempotenta
    private boolean checkIdempotency(String f1, String f2) {
        return checkEquivalence(
                buildFormula(f1, "∧", f1),
                f1
        ) && checkEquivalence(
                buildFormula(f1, "∨", f1),
                f1
        );
    }

    // f) Dubla negatie
    private boolean checkDoubleNegation(String f1) {
        return checkEquivalence(
                "¬(¬" + f1 + ")",
                f1
        );
    }

    // g) Tertul exclus si contradictie
    private boolean checkExcludedMiddle(String f1) {
        return checkEquivalence(
                buildFormula(f1, "∨", "¬" + f1),
                "1"
        ) && checkEquivalence(
                buildFormula(f1, "∧", "¬" + f1),
                "0"
        );
    }

    // h) De Morgan
    private boolean checkDeMorgan(String f1, String f2) {
        return checkEquivalence(
                "¬(" + buildFormula(f1, "∧", f2) + ")",
                buildFormula("¬" + f1, "∨", "¬" + f2)
        ) && checkEquivalence(
                "¬(" + buildFormula(f1, "∨", f2) + ")",
                buildFormula("¬" + f1, "∧", "¬" + f2)
        );
    }

    // j) Implicatie
    private boolean checkImplication(String f1) {
        return checkEquivalence(
                buildFormula(f1, "→", "B"),
                buildFormula("¬" + f1, "∨", "B")
        );
    }

    // k) Contrapozitie
    private boolean checkContraposition(String f1) {
        return checkEquivalence(
                buildFormula(f1, "→", "B"),
                buildFormula("¬B", "→", "¬" + f1)
        );
    }

    // l) Separarea premiselor
    private boolean checkPremiseCombination(String f1, String f2) {
        return checkEquivalence(
                buildFormula("(" + buildFormula(f1, "∧", f2) + ")", "→", "B"),
                buildFormula(f1, "→", "(" + buildFormula(f2, "→", "B") + ")")
        );
    }

    // m) Permutarea premiselor
    private boolean checkPremisePermutation(String f1, String f2) {
        return checkEquivalence(
                buildFormula(f1, "→", "(" + buildFormula(f2, "→", "B") + ")"),
                buildFormula(f2, "→", "(" + buildFormula(f1, "→", "B") + ")")
        );
    }

    private boolean checkEquivalence(String f1, String f2) {
        if (!validator.isFormula(f1) || !validator.isFormula(f2)) {
            return false;
        }

        Set<String> vars = new HashSet<>();
        vars.addAll(FormulaEvaluator.extractVariables(f1));
        vars.addAll(FormulaEvaluator.extractVariables(f2));

        List<String> varList = new ArrayList<>(vars);
        int rows = 1 << varList.size();

        for (int i = 0; i < rows; i++) {
            Map<String, Boolean> valuation = new HashMap<>();
            for (int j = 0; j < varList.size(); j++) {
                valuation.put(varList.get(j), (i & (1 << j)) != 0);
            }

            boolean val1 = FormulaEvaluator.evaluate(f1, valuation);
            boolean val2 = FormulaEvaluator.evaluate(f2, valuation);

            if (val1 != val2) {
                return false;
            }
        }
        return true;
    }


    private String[] extractVariables(String formula) {
        Set<String> vars = FormulaEvaluator.extractVariables(formula);
        return vars.toArray(new String[0]);
    }

    private String buildFormula(String left, String op, String right) {
        return "(" + left + " " + op + " " + right + ")";
    }
}