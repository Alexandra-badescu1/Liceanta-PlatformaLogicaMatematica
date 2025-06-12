package com.example.licenta.normalForm;

import org.springframework.stereotype.Service;

@Service
public class ImplicationTransformer {

    public Node eliminate(Node node) {
        if (node == null) return null;

        switch (node.getType()) {
            case VAR:
                return new Node(NodeType.VAR, node.getValue());
            case IMPLIES:
                return new Node(NodeType.OR,
                        new Node(NodeType.NOT, eliminate(node.getLeft())),
                        eliminate(node.getRight()));
            case IFF:
                Node a = eliminate(node.getLeft());
                Node b = eliminate(node.getRight());
                Node left = new Node(NodeType.OR, new Node(NodeType.NOT, a), b);
                Node right = new Node(NodeType.OR, new Node(NodeType.NOT, b), a);
                return new Node(NodeType.AND, left, right);
            case NOT:
                return new Node(NodeType.NOT, eliminate(node.getLeft()));
            default:
                return new Node(node.getType(), eliminate(node.getLeft()), eliminate(node.getRight()));
        }
    }
}

