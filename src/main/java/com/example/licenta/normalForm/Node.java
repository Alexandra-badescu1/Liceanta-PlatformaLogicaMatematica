package com.example.licenta.normalForm;

public class Node {
    private NodeType type;
    private String value; // doar pentru VAR
    private Node left;
    private Node right;

    public Node(NodeType type, Node left, Node right) {
        this.type = type;
        this.left = left;
        this.right = right;
    }

    public Node(NodeType type, Node child) {
        this.type = type;
        this.left = child;
    }

    // 👇 AICI adaugi constructorul pentru VAR:
    public Node(NodeType type, String value) {
        this.type = type;
        this.value = value;
    }

    // Getters
    public NodeType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }
}
