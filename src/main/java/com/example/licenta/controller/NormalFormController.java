package com.example.licenta.controller;

import com.example.licenta.logic.*;
import com.example.licenta.normalForm.FormulaService;
import com.example.licenta.normalForm.TransformationStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/normal-form")
public class NormalFormController {

    @Autowired
    private NormalFormChecker checker;
    @Autowired
    private FormulaValidator validator;

    @Autowired
    private FormulaService formulaService;

    @PostMapping("/transform")
    public List<TransformationStep> transformFormula(@RequestBody String formula) {
        return formulaService.transformFormula(formula);
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

}