package com.banalytics.box.filter;

public class FilterNode {
    public final FilterNode left;
    public final FilterNode right;
    public Object value;

    public FilterNode(Object value) {
        this.left = null;
        this.right = null;
        this.value = value;
    }

    public FilterNode(FilterNode left, FilterNode right, Object value) {
        this.left = left;
        this.right = right;
        this.value = value;
    }

    public FilterNode(String propertyName, String operation, Object value) {
        this.left = new FilterNode(propertyName);
        this.right = new FilterNode(value);
        this.value = operation;
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

    public boolean applyFilter(Object obj) {
        Object value = this.value;
        if (value instanceof String) {
            String operation = (String) value;
            FilterNode left = this.left;
            FilterNode right = this.right;

            return FilterOperators
                    .valueOf(operation)
                    .operation()
                    .apply(obj, left, right);
        }
        throw new RuntimeException("Unknown case: " + this.toString());
    }
}
