package com.example.licenta.normalForm;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FlattenTransformer {

    public Node flatten(Node node) {
        if (node == null) return null;

        if (node.getType() == NodeType.VAR || node.getType() == NodeType.NOT) {
            return node;
        }

        List<Node> terms = new ArrayList<>();
        collectTerms(node, node.getType(), terms);

        if (terms.isEmpty()) {
            return node;
        }

        Node flat = terms.get(0);
        for (int i = 1; i < terms.size(); i++) {
            flat = new Node(node.getType(), flat, terms.get(i));
        }
        return flat;
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

}
