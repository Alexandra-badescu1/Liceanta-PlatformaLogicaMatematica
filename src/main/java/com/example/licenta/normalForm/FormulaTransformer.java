package com.example.licenta.normalForm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FormulaTransformer {

    @Autowired
    private ImplicationTransformer implicationTransformer;

    @Autowired
    private NegationTransformer negationTransformer;

    @Autowired
    private DistributionTransformer distributionTransformer;

    @Autowired
    private SimplificationTransformer simplificationTransformer;


    public Node eliminateImplications(Node node) {
        return implicationTransformer.eliminate(node);
    }

    public Node pushNegations(Node node) {
        return negationTransformer.pushNegations(node);
    }
    

    public Node simplify(Node node) {
        if (node == null) return null;
        if (node.getType() == NodeType.VAR) return new Node(NodeType.VAR, node.getValue());

        node = new Node(node.getType(), simplify(node.getLeft()), simplify(node.getRight()));
        node = simplificationTransformer.applyIdempotence(node);
        node = simplificationTransformer.applyContradiction(node);
        node = simplificationTransformer.applyConstantSimplification(node);
        node = simplificationTransformer.applyAbsorption(node);
        return node;
        //return simplificationTransformer.simplify(node);
    }
    public Node smartDistribute(Node node) {
        if (node == null) return null;

        if (node.getType() == NodeType.VAR || node.getType() == NodeType.NOT) {
            return node;
        }

        // Recursiv pe subnoduri
        Node left = smartDistribute(node.getLeft());
        Node right = smartDistribute(node.getRight());

        // Aplica distributivitatea
        if (node.getType() == NodeType.OR) {
            if (left.getType() == NodeType.AND) {
                Node l1 = smartDistribute(new Node(NodeType.OR, left.getLeft(), right));
                Node l2 = smartDistribute(new Node(NodeType.OR, left.getRight(), right));
                return new Node(NodeType.AND, l1, l2);
            }
            if (right.getType() == NodeType.AND) {
                Node r1 = smartDistribute(new Node(NodeType.OR, left, right.getLeft()));
                Node r2 = smartDistribute(new Node(NodeType.OR, left, right.getRight()));
                return new Node(NodeType.AND, r1, r2);
            }
        }

        // Nu e nevoie de distributivitate
        return new Node(node.getType(), left, right);
    }

    public String printFormula(Node node) {
        if (node == null) {
            return "";
        }

        String formula;
        switch (node.getType()) {
            case VAR:
                formula = node.getValue();
                break;
            case NOT:
                formula = "¬" + printFormula(node.getLeft());
                break;
            case AND:
                formula = "(" + printFormula(node.getLeft()) + " ∧ " + printFormula(node.getRight()) + ")";
                break;
            case OR:
                formula = "(" + printFormula(node.getLeft()) + " ∨ " + printFormula(node.getRight()) + ")";
                break;
            case IMPLIES:
                formula = "(" + printFormula(node.getLeft()) + " → " + printFormula(node.getRight()) + ")";
                break;
            case IFF:
                formula = "(" + printFormula(node.getLeft()) + " ↔ " + printFormula(node.getRight()) + ")";
                break;
            default:
                throw new IllegalArgumentException("Unknown node type: " + node.getType());
        }

        // Sterge primul si ultimul caracter DOAR daca sunt paranteze
//        if (formula.length() >= 2 && formula.startsWith("(") && formula.endsWith(")")) {
//            formula = formula.substring(1, formula.length() - 1);
//        }

        return formula;
    }


    /**
     * Transformă un nod AST într-un șir de caractere reprezentând formula logică.
     * @param node Nodul AST de la care se începe transformarea.
     * @return Formula logică sub formă de șir de caractere.
     */
    public String toFormulaString(Node node) {
        if (node == null) return "";
        return toFormulaString(node, getPriority(node.getType()));
    }

    private String toFormulaString(Node node, int parentPriority) {
        if (node.getType() == NodeType.VAR) {
            return node.getValue();
        }

        if (node.getType() == NodeType.NOT) {
            String childStr = toFormulaString(node.getLeft(), getPriority(NodeType.NOT));
            if (node.getLeft().getType() == NodeType.VAR) {
                return "¬" + childStr;
            } else {
                return "¬(" + childStr + ")";
            }
        }

        int nodePriority = getPriority(node.getType());
        String leftStr = toFormulaString(node.getLeft(), nodePriority);
        String rightStr = toFormulaString(node.getRight(), nodePriority);

        String operator = getOperatorSymbol(node.getType());

        String result = leftStr + " " + operator + " " + rightStr;

        boolean needParentheses = nodePriority < parentPriority;

        if (needParentheses) {
            return "(" + result + ")";
        } else {
            return result;
        }
    }

    private int getPriority(NodeType type) {
        switch (type) {
            case NOT: return 4;
            case AND: return 3;
            case OR: return 2;
            case IMPLIES: return 1;
            case IFF: return 0;
            default: return -1;
        }
    }

    private String getOperatorSymbol(NodeType type) {
        switch (type) {
            case AND: return "∧";
            case OR: return "∨";
            case IMPLIES: return "→";
            case IFF: return "↔";
            default: return "";
        }
    }

    private boolean isLiteral(Node node) {
        return node.getType() == NodeType.VAR ||
                (node.getType() == NodeType.NOT && node.getLeft().getType() == NodeType.VAR);
    }

    // Verifică dacă un subarbore este o disjuncție de literali
    private boolean isClauseOfLiterals(Node node) {
        if (node == null) return false;

        if (isLiteral(node)) {
            return true;
        }

        if (node.getType() == NodeType.OR) {
            return isClauseOfLiterals(node.getLeft()) && isClauseOfLiterals(node.getRight());
        }

        return false;
    }


    private boolean isFNC(Node node) {
        if (node == null) return false;

        if (isClauseOfLiterals(node)) {
            return true;
        }

        if (node.getType() == NodeType.AND) {
            return isFNC(node.getLeft()) && isFNC(node.getRight());
        }

        return false;
    }
    private boolean isConjunctionOfLiterals(Node node) {
        if (node == null) return false;

        if (isLiteral(node)) {
            return true;
        }

        if (node.getType() == NodeType.AND) {
            return isConjunctionOfLiterals(node.getLeft()) && isConjunctionOfLiterals(node.getRight());
        }

        return false;
    }

    private boolean isFND(Node node) {
        if (node == null) return false;

        if (isConjunctionOfLiterals(node)) {
            return true;
        }

        if (node.getType() == NodeType.OR) {
            return isFND(node.getLeft()) && isFND(node.getRight());
        }

        return false;
    }


    public int detectNormalForm(Node node) {
        if (isFNC(node)) {
            return 1; // FNC
        } else if (isFND(node)) {
            return 2; // FND
        } else {
            return 0; // None
        }
    }

}
