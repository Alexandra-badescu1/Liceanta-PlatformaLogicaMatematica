package com.example.licenta.controller;

import com.example.licenta.logic.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:5173/")
@RestController
@RequestMapping("/api/formula")
public class FormulaController {

    private final FormulaValidator validator;
    private final TruthTableGenerator tableGenerator;

    // Constructor injection is preferred over creating new instances
    public FormulaController() {
        this.validator = new FormulaValidator();
        this.tableGenerator = new TruthTableGenerator();
    }

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
        headers.add("result");
        return headers;
    }
}