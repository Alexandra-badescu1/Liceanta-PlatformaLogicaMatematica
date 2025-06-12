package com.example.licenta.normalForm;

import org.springframework.stereotype.Service;

@Service
public class DistributionTransformer {

    public Node distributeForFNC(Node node) {
        if (node == null) return null;

        if (node.getType() == NodeType.VAR) return new Node(NodeType.VAR, node.getValue());

        if (node.getType() == NodeType.OR) {
            Node left = node.getLeft();
            Node right = node.getRight();

            if (left.getType() == NodeType.AND) {
                return new Node(NodeType.AND,
                        distributeForFNC(new Node(NodeType.OR, left.getLeft(), right)),
                        distributeForFNC(new Node(NodeType.OR, left.getRight(), right)));
            } else if (right.getType() == NodeType.AND) {
                return new Node(NodeType.AND,
                        distributeForFNC(new Node(NodeType.OR, left, right.getLeft())),
                        distributeForFNC(new Node(NodeType.OR, left, right.getRight())));
            }
        }

        return new Node(node.getType(), distributeForFNC(node.getLeft()), distributeForFNC(node.getRight()));
    }

    public Node distributeForFND(Node node) {
        if (node == null) return null;

        if (node.getType() == NodeType.VAR) return new Node(NodeType.VAR, node.getValue());

        if (node.getType() == NodeType.AND) {
            Node left = node.getLeft();
            Node right = node.getRight();

            if (left.getType() == NodeType.OR) {
                return new Node(NodeType.OR,
                        distributeForFND(new Node(NodeType.AND, left.getLeft(), right)),
                        distributeForFND(new Node(NodeType.AND, left.getRight(), right)));
            } else if (right.getType() == NodeType.OR) {
                return new Node(NodeType.OR,
                        distributeForFND(new Node(NodeType.AND, left, right.getLeft())),
                        distributeForFND(new Node(NodeType.AND, left, right.getRight())));
            }
        }

        return new Node(node.getType(), distributeForFND(node.getLeft()), distributeForFND(node.getRight()));
    }

}

