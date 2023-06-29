package com.banalytics.box.jpa;

import java.util.ArrayList;
import java.util.List;

public class ExpressionNode {
    public final ExpressionNode left;
    public final ExpressionNode right;
    public Object value;

    public ExpressionNode(Object value) {
        this.left = null;
        this.right = null;
        this.value = value;
    }

    public ExpressionNode(ExpressionNode left, ExpressionNode right, Object value) {
        this.left = left;
        this.right = right;
        this.value = value;
    }

    public ExpressionNode(String propertyName, String operation, Object value) {
        this.left = new ExpressionNode(propertyName);
        this.right = new ExpressionNode(value);
        this.value = operation;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    @Override
    public String toString() {
        return
                (left == null ? "" : ("(" + left.toString() + ")"))
                        +
                        " " + value + " "
                        +
                        (right == null ? "" : ("(" + right.toString() + ")"));
    }

    public static ExpressionNode create(String propertyName, String operation, Object value) {
        return new ExpressionNode(propertyName, operation, value);
    }

    public ExpressionNode and(String propertyName, String operation, Object value) {
        return new ExpressionNode(
                create(propertyName, operation, value),
                this,
                "and"
        );
    }

    public ExpressionNode and(ExpressionNode node) {
        return new ExpressionNode(
                node,
                this,
                "and"
        );
    }

    public ExpressionNode or(String propertyName, String operation, Object value) {
        return new ExpressionNode(
                create(propertyName, operation, value),
                this,
                "or"
        );
    }

    public Object findParam(String paramName) {
        ExpressionNode node = ExpressionNodeUtils.findParam(this, paramName);
        return node == null ? null : node.right.value;
    }

    public List<ExpressionNode> findOperations(final String paramName, final String operation) {
        List<ExpressionNode> result = new ArrayList<>(5);
        ExpressionNodeUtils.findOperations(this, paramName, operation, result);
        return result;
    }
}
