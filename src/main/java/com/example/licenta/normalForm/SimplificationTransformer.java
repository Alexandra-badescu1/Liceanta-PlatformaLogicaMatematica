package com.example.licenta.normalForm;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SimplificationTransformer {

    /**
     * Flatten the given node to collect all terms of a specific type (AND or OR).
     *
     * @param node The root node of the expression.
     * @param type The type of node to flatten (AND or OR).
     * @return A list of nodes representing the flattened terms.
     */
    private List<Node> flatten(Node node, NodeType type) {
        List<Node> terms = new ArrayList<>();
        collectTerms(node, type, terms);
        return terms;
    }

    private void collectTerms(Node node, NodeType type, List<Node> terms) {
        if (node == null) return;
        if (node.getType() == type) {
            collectTerms(node.getLeft(), type, terms);
            collectTerms(node.getRight(), type, terms);
        } else {
            terms.add(node);
        }
    }

//    private Node applyIdempotence(Node node) {
//        if (node == null) return null;
//
//        if (node.getType() == NodeType.AND || node.getType() == NodeType.OR) {
//            List<Node> terms = flatten(node, node.getType());
//
//            List<Node> uniqueTerms = new ArrayList<>();
//            for (Node term : terms) {
//                boolean alreadyExists = uniqueTerms.stream()
//                        .anyMatch(existing -> existing.getType() == term.getType() && existing.getValue().equals(term.getValue()));
//                if (!alreadyExists) {
//                    uniqueTerms.add(term);
//                }
//            }
//
//            if (uniqueTerms.isEmpty()) {
//                return node;
//            }
//
//            Node result = uniqueTerms.get(0);
//            for (int i = 1; i < uniqueTerms.size(); i++) {
//                result = new Node(node.getType(), result, uniqueTerms.get(i));
//            }
//            return result;
//        }
//
//        return node;
//    }
public Node applyIdempotence(Node node) {
    if (node == null) return null;

    if (node.getType() == NodeType.AND || node.getType() == NodeType.OR) {
        List<Node> terms = flatten(node, node.getType());

        List<Node> uniqueTerms = new ArrayList<>();
        for (Node term : terms) {
            boolean alreadyExists = uniqueTerms.stream()
                    .anyMatch(existing -> isSameLiteral(existing, term));
            if (!alreadyExists) {
                uniqueTerms.add(term);
            }
        }

        if (uniqueTerms.isEmpty()) {
            return node;
        }

        Node result = uniqueTerms.get(0);
        for (int i = 1; i < uniqueTerms.size(); i++) {
            result = new Node(node.getType(), result, uniqueTerms.get(i));
        }
        return result;
    }

    return node;
}

    private boolean isSameLiteral(Node a, Node b) {
        if (a == null || b == null) return false;

        if (a.getType() == NodeType.VAR && b.getType() == NodeType.VAR) {
            return a.getValue() != null && a.getValue().equals(b.getValue());
        }

        if (a.getType() == NodeType.NOT && b.getType() == NodeType.NOT) {
            return a.getLeft() != null && b.getLeft() != null &&
                    a.getLeft().getType() == NodeType.VAR && b.getLeft().getType() == NodeType.VAR &&
                    a.getLeft().getValue() != null &&
                    a.getLeft().getValue().equals(b.getLeft().getValue());
        }

        return false;
    }


    public Node applyContradiction(Node node) {
        if (node == null) return null;

        if (node.getType() == NodeType.AND || node.getType() == NodeType.OR) {
            List<Node> terms = flatten(node, node.getType());

            // Verificăm toate combinațiile
            for (int i = 0; i < terms.size(); i++) {
                for (int j = i + 1; j < terms.size(); j++) {
                    if (isNegationOf(terms.get(i), terms.get(j)) || isNegationOf(terms.get(j), terms.get(i))) {
                        if (node.getType() == NodeType.OR) {
                            return new Node(NodeType.VAR, "1"); // A ∨ ¬A ≡ 1
                        } else if (node.getType() == NodeType.AND) {
                            return new Node(NodeType.VAR, "0"); // A ∧ ¬A ≡ 0
                        }
                    }
                }
            }
        }
        return node;
    }


    public Node applyConstantSimplification(Node node) {
        if (node.getType() == NodeType.AND) {
            if (isConstant(node.getLeft(), "1")) return node.getRight();
            if (isConstant(node.getRight(), "1")) return node.getLeft();
            if (isConstant(node.getLeft(), "0") || isConstant(node.getRight(), "0")) return new Node(NodeType.VAR, "0");
        }

        if (node.getType() == NodeType.OR) {
            if (isConstant(node.getLeft(), "0")) return node.getRight();
            if (isConstant(node.getRight(), "0")) return node.getLeft();
            if (isConstant(node.getLeft(), "1") || isConstant(node.getRight(), "1")) return new Node(NodeType.VAR, "1");
        }
        return node;
    }
    private boolean isConstant(Node node, String constant) {
        return node.getType() == NodeType.VAR && constant.equals(node.getValue());
    }
    private boolean isNegationOf(Node a, Node b) {
        if (a == null || b == null) return false;

        if (a.getType() == NodeType.NOT &&
                a.getLeft() != null &&
                a.getLeft().getType() == NodeType.VAR &&
                b.getType() == NodeType.VAR) {

            return a.getLeft().getValue() != null && a.getLeft().getValue().equals(b.getValue());
        }

        return false;
    }
    public Node applyAbsorption(Node node) {
        if (node == null) return null;

        if (node.getType() == NodeType.OR || node.getType() == NodeType.AND) {
            List<Node> terms = flatten(node, node.getType());

            List<Node> toRemove = new ArrayList<>();

            for (Node term : terms) {
                for (Node other : terms) {
                    if (term == other) continue;

                    if (term.getType() == NodeType.VAR || isNegatedVar(term)) {
                        if (other.getType() == oppositeNodeType(node.getType())) {
                            if (containsVar(other, term)) {
                                toRemove.add(other); // other e absorbit
                            }
                        }
                    }
                }
            }
            terms.removeAll(toRemove);
            if (terms.isEmpty()) {
                return node;
            }
            Node result = terms.get(0);
            for (int i = 1; i < terms.size(); i++) {
                result = new Node(node.getType(), result, terms.get(i));
            }
            return result;
        }
        return node;
    }

    private boolean isNegatedVar(Node node) {
        return node.getType() == NodeType.NOT && node.getLeft() != null && node.getLeft().getType() == NodeType.VAR;
    }
    private boolean containsVar(Node node, Node var) {
        if (node == null) return false;

        if (node.getType() == NodeType.VAR && node.getValue().equals(var.getValue())) {
            return true;
        }

        if (node.getType() == NodeType.NOT && node.getLeft() != null) {
            return containsVar(node.getLeft(), var);
        }

        return containsVar(node.getLeft(), var) || containsVar(node.getRight(), var);
    }
    private NodeType oppositeNodeType(NodeType type) {
        switch (type) {
            case AND: return NodeType.OR;
            case OR: return NodeType.AND;
            default: return type; // pentru alte tipuri, nu schimb
        }
    }
}

