package com.example.licenta.normalForm;

import org.springframework.stereotype.Service;

@Service
public class FormulaParser {

    private String input;
    private int pos;

    public Node parse(String input) {
        this.input = input.replaceAll("\\s+", ""); // curăță spațiile
        this.pos = 0;
        return parseExpression();
    }

    private Node parseExpression() {
        Node left = parseTerm();
        while (peek() == '→' || peek() == '↔') {
            char op = consume();
            Node right = parseTerm();
            if (op == '→') {
                left = new Node(NodeType.IMPLIES, left, right);
            } else if (op == '↔') {
                left = new Node(NodeType.IFF, left, right);
            }
        }
        return left;
    }

    private Node parseTerm() {
        Node left = parseFactor();
        while (peek() == '∨') {
            consume();
            Node right = parseFactor();
            left = new Node(NodeType.OR, left, right);
        }
        return left;
    }

    private Node parseFactor() {
        Node left = parsePrimary();
        while (peek() == '∧') {
            consume();
            Node right = parsePrimary();
            left = new Node(NodeType.AND, left, right);
        }
        return left;
    }

    private Node parsePrimary() {
        char c = peek();
        if (c == '¬') {
            consume();
            Node child = parsePrimary();
            return new Node(NodeType.NOT, child);
        } else if (c == '(') {
            consume();
            Node expr = parseExpression();
            expect(')');
            return expr;
        } else if (Character.isLetter(c)) {

            StringBuilder sb = new StringBuilder();
            while (pos < input.length() && (Character.isLetterOrDigit(peek()))) {
                sb.append(consume());
            }System.out.println("Parsing variable: " + sb.toString());

            return new Node(NodeType.VAR, sb.toString());
        } else {
            throw new RuntimeException("Unexpected character: " + c);
        }
    }

    private char peek() {
        if (pos >= input.length()) return '\0';
        return input.charAt(pos);
    }

    private char consume() {
        return input.charAt(pos++);
    }

    private void expect(char expected) {
        if (consume() != expected) {
            throw new RuntimeException("Expected: " + expected);
        }
    }
}

