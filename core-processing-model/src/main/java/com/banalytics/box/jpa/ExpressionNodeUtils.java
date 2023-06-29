package com.banalytics.box.jpa;


import java.util.List;

public class ExpressionNodeUtils {
    public static ExpressionNode and(ExpressionNode left, ExpressionNode right) {
        if(right==null && left==null) {
            return null;
        }
        if(right==null) {
            return left;
        }
        return new ExpressionNode(left, right, "and");
    }
    public static ExpressionNode or(ExpressionNode left, ExpressionNode right) {
        if(right==null && left==null) {
            return null;
        }
        if(right==null) {
            return left;
        }
        return new ExpressionNode(left, right, "or");
    }

    static ExpressionNode findParam(ExpressionNode node, String paramName) {
        ExpressionNode result;
        if (!node.left.isLeaf()) {
            result = findParam(node.left, paramName);
            if (result != null) {
                return result;
            }
        }
        if (!node.right.isLeaf()) {
            result = findParam(node.right, paramName);
            if (result != null) {
                return result;
            }
        }
        if (paramName.equals(node.left.value)) {
            return node;
        }

        return null;
    }

    static void findOperations(ExpressionNode node, final String paramName, final String operation, List<ExpressionNode> resultAccumulator) {
        if (!node.left.isLeaf()) {
            findOperations(node.left, paramName, operation, resultAccumulator);
        }
        if (!node.right.isLeaf()) {
            findOperations(node.right, paramName, operation, resultAccumulator);
        }
        if (paramName.equals(node.left.value) && operation.equals(node.value)) {
            resultAccumulator.add(node);
        }
    }
}

