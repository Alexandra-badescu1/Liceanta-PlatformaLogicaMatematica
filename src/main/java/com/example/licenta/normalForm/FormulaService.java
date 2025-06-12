package com.example.licenta.normalForm;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FormulaService {

    @Autowired
    private FormulaParser parser;

    @Autowired
    private FormulaTransformer transformer;

    public List<TransformationStep> transformFormula(String formula) {
        List<TransformationStep> steps = new ArrayList<>();

        Node ast = parser.parse(formula);
        if (ast == null) {
            steps.add(new TransformationStep("Error", "Invalid formula syntax"));
            return steps;
        }

        steps.add(new TransformationStep("Parsed Formula", transformer.printFormula(ast)));

        ast = transformer.eliminateImplications(ast);
        steps.add(new TransformationStep("Eliminate implications and biconditionals", transformer.printFormula(ast)));

        ast = transformer.pushNegations(ast);
        steps.add(new TransformationStep("Push negations (De Morgan and double negation)", transformer.printFormula(ast)));

        Node distributedAst = transformer.smartDistribute(ast);
        steps.add(new TransformationStep("Smart Distributivity (deepest first)", transformer.toFormulaString(distributedAst)));

        Node simplified = transformer.simplify(distributedAst);
        steps.add(new TransformationStep("Simplification (Idempotence, Absorption, Contradiction,Asociativity)", transformer.printFormula(simplified)));
        int normalForm = transformer.detectNormalForm(simplified);

        if (normalForm == 1) {
            steps.add(new TransformationStep("Normal Form Detection", "✅ Formula is in Conjunctive Normal Form (FNC)"));
        } else if (normalForm == 2) {
            steps.add(new TransformationStep("Normal Form Detection", "✅ Formula is in Disjunctive Normal Form (FND)"));
        } else {
            steps.add(new TransformationStep("Normal Form Detection", "❌ Formula is NOT in Conjunctive or Disjunctive Normal Form"));
        }

        return steps;
    }
}