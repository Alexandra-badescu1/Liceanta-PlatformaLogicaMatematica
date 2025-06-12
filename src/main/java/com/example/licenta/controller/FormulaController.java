package com.example.licenta.controller;

import com.example.licenta.logic.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@CrossOrigin(origins = "http://localhost:5173/")
@RestController
@RequestMapping("/api/formula")
public class FormulaController {

    @Autowired
    private FormulaValidator validator;
    @Autowired
    private TruthTableGenerator tableGenerator;




    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(@RequestBody Map<String, String> body) {
        try {
            String formula = body.get("formula");
            if (formula == null || formula.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Formula is required"));
            }

            boolean valid = validator.isFormula(formula);
            return ResponseEntity.ok(Map.of(
                    "valid", valid,
                    "normalized", validator.getNormalizedFormula()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Validation failed: " + e.getMessage()));
        }
    }

    @PostMapping("/truth-table")
    public ResponseEntity<Map<String, Object>> generateTruthTable(@RequestBody Map<String, String> body) {
        try {
            String formula = body.get("formula");
            if (formula == null || formula.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Formula is required"));
            }

            if (!validator.isFormula(formula)) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "error", "Invalid formula",
                                "suggestion", "Check your syntax and try again"
                        ));
            }

            Set<String> variables = FormulaEvaluator.extractVariables(formula);
            List<Map<String, String>> table = tableGenerator.generateTruthTable(variables, formula);

            return ResponseEntity.ok(Map.of(
                    "formula", formula,
                    "variables", new ArrayList<>(variables),
                    "table", table,
                    "headers", getTableHeaders(variables, formula)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", "Truth table generation failed",
                            "details", e.getMessage()
                    ));
        }
    }

    private List<String> getTableHeaders(Set<String> variables, String formula) {
        List<String> headers = new ArrayList<>(variables);

        // Folosește SubformulaExtractor pentru a extrage subformulele
        SubformulaExtractor extractor = new SubformulaExtractor();
        List<String> subformulas = extractor.extractSubformulas(formula);

        // Elimină duplicatele și variabilele deja prezente în antet
        Set<String> variableSet = new HashSet<>(variables);
        subformulas = subformulas.stream()
                .filter(f -> !variableSet.contains(f)) // elimină variabilele deja adăugate
                .distinct()
                .sorted(Comparator.comparingInt(String::length))
                .toList();

        headers.addAll(subformulas);

        // Adaugă formula principală la final (doar dacă nu e deja inclusă)
        if (!headers.contains(formula)) {
            headers.add(formula);
        }

        return headers;
    }

    @PostMapping("/classify")
    public ResponseEntity<Map<String, Object>> classifyFormula(@RequestBody Map<String, String> body) {
        try {
            String formula = body.get("formula");
            if (formula == null || formula.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Formula is required"));
            }

            if (!validator.isFormula(formula)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid formula"));
            }

            Set<String> variables = FormulaEvaluator.extractVariables(formula);
            List<Map<String, String>> table = tableGenerator.generateTruthTable(variables, formula);

            boolean allTrue = true;
            boolean allFalse = true;

            for (Map<String, String> row : table) {
                String result = row.get(formula);
                if (result.equals("0")) allTrue = false;
                if (result.equals("1")) allFalse = false;
            }

            String classification = allTrue ? "Tautologie" :
                    allFalse ? "Contradicție" :
                            "Nici tautologie, nici contradicție";

            return ResponseEntity.ok(Map.of(
                    "formula", formula,
                    "classification", classification
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Classification failed: " + e.getMessage()));
        }
    }
    @PostMapping("/subformulas")
    public ResponseEntity<Map<String, Object>> getSubformulas(@RequestBody Map<String, String> body) {
        try {
            String formula = body.get("formula");
            if (formula == null || formula.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Formula is required"));
            }

            if (!validator.isFormula(formula)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid formula"));
            }

            SubformulaExtractor extractor = new SubformulaExtractor();
            List<String> subformulas = extractor.extractSubformulas(formula)
                    .stream()
                    .distinct()
                    .sorted(Comparator.comparingInt(String::length))
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "formula", formula,
                    "subformulas", subformulas
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Extraction failed: " + e.getMessage()));
        }
    }

}