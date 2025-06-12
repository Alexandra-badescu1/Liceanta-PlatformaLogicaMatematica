package com.example.licenta.normalForm;

import org.springframework.stereotype.Service;

@Service
public class NegationTransformer {

    public Node pushNegations(Node node) {
        if (node == null) return null;

        if (node.getType() == NodeType.VAR) {
            return new Node(NodeType.VAR, node.getValue());
        }

        if (node.getType() == NodeType.NOT) {
            Node child = node.getLeft();
            switch (child.getType()) {
                case NOT:
                    return pushNegations(child.getLeft());
                case AND:
                    return new Node(NodeType.OR,
                            pushNegations(new Node(NodeType.NOT, child.getLeft())),
                            pushNegations(new Node(NodeType.NOT, child.getRight())));
                case OR:
                    return new Node(NodeType.AND,
                            pushNegations(new Node(NodeType.NOT, child.getLeft())),
                            pushNegations(new Node(NodeType.NOT, child.getRight())));
                default:
                    return new Node(NodeType.NOT, pushNegations(child));
            }
        }

        return new Node(node.getType(), pushNegations(node.getLeft()), pushNegations(node.getRight()));
    }
}

