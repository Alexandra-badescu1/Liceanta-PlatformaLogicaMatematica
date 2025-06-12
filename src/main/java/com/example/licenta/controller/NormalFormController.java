package com.example.licenta.controller;

import com.example.licenta.logic.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/normal-form")
public class NormalFormController {

    private final NormalFormChecker checker;
    private final NormalFormConverter converter;
    private final FormulaValidator validator;

    public NormalFormController() {
        this.checker = new NormalFormChecker();
        this.converter = new NormalFormConverter();
        this.validator = new FormulaValidator();
    }


    @PostMapping("/check")
    public Map<String, Object> checkNormalForm(@RequestBody Map<String, String> body) {
        String formula = body.get("formula");
        if (!validator.isFormula(formula)) {
            return Map.of("error", "Formula invalidă");
        }

        int formType = checker.checkNormalForm(formula);
        String typeName = switch (formType) {
            case NormalFormChecker.FNC -> "Forma Normală Conjunctivă (FNC)";
            case NormalFormChecker.FND -> "Forma Normală Disjunctivă (FND)";
            default -> "Nici FNC, nici FND";
        };

        return Map.of(
                "formula", formula,
                "type", formType,
                "typeName", typeName
        );
    }
    @PostMapping("/convert")
    public ResponseEntity<Map<String, Object>> convertFormula(@RequestBody Map<String, String> body) {
        try {
            String formula = body.get("formula");
            if (formula == null || formula.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Formula is required"));
            }

            if (!validator.isFormula(formula)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid formula"));
            }

            // Convert to CNF with proper parentheses
            String cnf = converter.toNormalForm(formula, true);
            cnf = ensureProperParentheses(cnf, true);
            boolean isCNF = checker.isCNF(normalizeForCheck(cnf));

            // Convert to DNF with proper parentheses
            String dnf = converter.toNormalForm(formula, false);
            dnf = ensureProperParentheses(dnf, false);
            boolean isDNF = checker.isDNF(normalizeForCheck(dnf));

            Map<String, Object> response = new HashMap<>();
            response.put("original", formula);
            response.put("cnf", cnf);
            response.put("dnf", dnf);
            response.put("isCNF", isCNF);
            response.put("isDNF", isDNF);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Conversion failed: " + e.getMessage()));
        }
    }

    // Helper method to normalize formula for the checker
    private String normalizeForCheck(String formula) {
        return formula.replace("∧", "&").replace("∨", "|");
    }

    // Helper method to ensure proper parentheses in the output
    private String ensureProperParentheses(String formula, boolean isCNF) {
        // Split by the main connective (∧ for CNF, ∨ for DNF)
        String[] parts = formula.split(isCNF ? "∧" : "∨");

        // If there's only one part, return it as is
        if (parts.length <= 1) {
            return formula;
        }

        // Add parentheses around each part if they don't already have them
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (!part.startsWith("(") || !part.endsWith(")")) {
                result.append("(").append(part).append(")");
            } else {
                result.append(part);
            }
            if (i < parts.length - 1) {
                result.append(isCNF ? "∧" : "∨");
            }
        }

        return result.toString();
    }
}