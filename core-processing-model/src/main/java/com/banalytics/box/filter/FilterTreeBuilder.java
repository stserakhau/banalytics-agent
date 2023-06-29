package com.banalytics.box.filter;

import com.banalytics.box.filter.converter.EnumConverter;
import com.banalytics.box.filter.converter.InetAddressConverter;
import com.banalytics.box.filter.converter.LocalDateTimeConverter;
import com.banalytics.box.filter.converter.UUIDConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.ArrayConverter;
import org.springframework.beans.BeanUtils;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class FilterTreeBuilder {

    private static final Map<Character, Character> openCloseStringChars = new HashMap<>() {{
        put('\'', '\'');
        put('"', '"');
        put('[', ']');
    }};


    public static final Map<String, Integer> operators = FilterOperators.toMap();

    public static FilterNode parse(String expression, Class clazz) {
        List<String> splitted = splitByWord(expression);

        List<String> res = inPolishNotation(splitted);
        List<String> finalRes = concatValues(res);
        FilterNode root = buildTree(finalRes);

        applyFilterClassTypes(root, clazz);

        return root;
    }

    private static List<String> concatValues(List<String> res) {
        List<String> result = new ArrayList<>();
        String property = null;
        String value = null;
        for (String item : res) {
            if (operators.containsKey(item)) {
                if (property != null && value != null) {
                    result.add(property);
                    result.add(value);
                    property = null;
                    value = null;
                }
                result.add(item);
            } else if (property == null && value == null) {
                property = item;
            } else if (property != null && !operators.containsKey(item)) {
                value = value == null ? item : value + " " + item;
            }

        }
        return result;
    }


    private static List<String> splitByWord(String expression) {
        List<String> result = splitExpressionByDelimeter(expression);

        List<String> words = new ArrayList<>() {{
            add("(");
            add(")");
        }};
        words.addAll(operators.keySet());

        for (String word : words) {
            int wordLen = word.length();
            List<String> res = new ArrayList<>();
            for (String line : result) {
                if (openCloseStringChars.containsKey(line.charAt(0)) || words.contains(line)) {
                    res.add(line);
                    continue;
                }
                int wordStart = -1;
                do {
                    if ("()".contains(word)) {
                        wordStart = line.indexOf(word);
                    } else if (line.contains(word + '(')) {
                        wordStart = line.indexOf(word + '(');
                    } else if (line.contains(' ' + word)) {
                        wordStart = line.indexOf(' ' + word) + 1;
                    } else if (line.contains(')' + word)) {
                        wordStart = line.indexOf(')' + word) + 1;
                    }

                    if (wordStart == 0) {
                        res.add(word);
                    } else if (wordStart > 0) {
                        char prevChar = line.charAt(wordStart - 1);
                        if (" )".indexOf(prevChar) == -1 && !")".equals(word)) {
                            wordStart = -1;
                        } else {
                            res.add(line.substring(0, wordStart));
                            res.add(word);
                        }
                    }
                    if (wordStart > -1) {
                        line = line.substring(wordStart + wordLen);
                    }
                } while (wordStart != -1);

                if (line.length() > 0) {
                    res.add(line);
                }
            }
            result = res;
        }

        return result;
    }

    private static List<String> splitExpressionByDelimeter(String expression) {
        expression = expression.replaceAll("''", "'null'");
        List<String> result = new ArrayList<>();
        {
            char[] chars = expression.toCharArray();

            StringBuilder word = new StringBuilder();
            Character closeChar = null;
            boolean prevOpenedWord = false;
            boolean isOpenedWord = false;
            boolean wasClosed = false;
            for (char chr : chars) {
                if (!isOpenedWord && (closeChar == null || closeChar.equals(chr))) {
                    closeChar = openCloseStringChars.get(chr);
                }

                isOpenedWord = closeChar != null && chr != closeChar;

                if (prevOpenedWord && isOpenedWord != prevOpenedWord) {
                    wasClosed = true;
                    word.append(chr);
                }

                prevOpenedWord = isOpenedWord;
                if (!wasClosed && (isOpenedWord || chr != ' ')) {
                    word.append(chr);
                } else {
                    if (word.length() > 0) {
                        result.add(word.toString());
                        word = new StringBuilder();
                    }
                    closeChar = null;
                    wasClosed = false;
                }
            }
            if (word.length() > 0) {
                result.add(word.toString());
            }
        }
        return result;
    }

    private static FilterNode buildTree(List<String> res) {
        Stack<FilterNode> stack = new Stack<>();

        for (String word : res) {
            Integer priority = operators.get(word);
            if (priority == null) {
                FilterNode root = new FilterNode(word);
                stack.push(root);
            } else {
                FilterNode right = stack.pop();
                FilterNode left = stack.pop();

                FilterNode root = new FilterNode(left, right, word);
                stack.push(root);
            }

        }
        FilterNode root = stack.pop();

        if (!stack.isEmpty()) {
            throw new RuntimeException("Invalid expression");
        }

        return root;
    }

    private static List<String> inPolishNotation(List<String> expression) {
        List<String> result = new ArrayList<>();
        Stack<String> stack = new Stack<>();
        for (String line : expression) {
            switch (line) {
                case "(":
                    stack.push(line);
                    break;
                case ")":
                    while (!stack.empty() && !stack.peek().equals("(")) {
                        result.add(stack.pop());
                    }
                    stack.pop();
                    break;
                default:
                    Integer linePriority = operators.get(line);
                    Integer lastStackPriority = !stack.empty() ? operators.get(stack.peek()) : null;
                    if (linePriority != null && lastStackPriority != null && lastStackPriority < linePriority) {
                        while (!stack.empty() && !stack.peek().equals("(") && operators.get(stack.peek()) < linePriority) {
                            result.add(stack.pop());
                        }
                    }

                    if (linePriority != null) {
                        stack.push(line);
                    } else {
                        result.add(line);
                    }
                    break;
            }
        }

        while (!stack.empty()) {
            result.add(stack.pop());
        }
        return result;
    }

    public static <T> void applyFilterClassTypes(FilterNode root, Class<T> filterClass) {
        boolean isBottomOperator = root.left.left == null;
        if (isBottomOperator) {
            String operation = (String) root.value;
            String propertyPath = (String) root.left.value;
            String value = (String) root.right.value;
//            if ("custom".equals(operation)) {
//                return;
//            }

            Class propertyClass = getPropertyClass(propertyPath, filterClass);
            Object val;
            boolean isStringValue = (value.startsWith("'") && value.endsWith("'"))
                    || (value.startsWith("\"") && value.endsWith("\""));
            boolean isArrayValue = (value.startsWith("[") && value.endsWith("]"));

            if (isStringValue) {
                if (propertyClass.isEnum() && ConvertUtils.lookup(propertyClass) == null) {
                    ConvertUtils.register(new EnumConverter(), propertyClass);
                }
                if (Collection.class.isAssignableFrom(propertyClass)) {
                    val = value.replaceAll("\\'", "");
                } else if (Map.class.isAssignableFrom(propertyClass)) {
                    val = value.replaceAll("\\'", "");
                } else {
                    val = ConvertUtils.convert(value.replaceAll("\\'", ""), propertyClass);
                }
            } else if (isArrayValue) {
                boolean isPropertyArray = propertyClass.isArray();
                Class arrayClass = isPropertyArray ? propertyClass : java.lang.reflect.Array.newInstance(propertyClass, 0).getClass();
                if (propertyClass.isEnum() && ConvertUtils.lookup(arrayClass) == null) {
                    ConvertUtils.register(new ArrayConverter(arrayClass, new EnumConverter(), 10), arrayClass);
                }
                if (propertyClass == LocalDateTime.class && ConvertUtils.lookup(arrayClass) == null) {
                    ConvertUtils.register(new ArrayConverter(arrayClass, new LocalDateTimeConverter(), 10), arrayClass);
                }
                if (propertyClass == InetAddress.class && ConvertUtils.lookup(arrayClass) == null) {
                    ConvertUtils.register(new ArrayConverter(arrayClass, new InetAddressConverter(), 10), arrayClass);
                }
                val = ConvertUtils.convert(value, arrayClass);

                if ("in".equals(operation)) {
                    Object[] values = (Object[]) val;
                    val = Arrays.asList(values);
                } else {
                    val = java.lang.reflect.Array.get(val, 0);
                }
            } else {
                if ("null".equalsIgnoreCase(value)) {
                    val = null;
                } else {
                    val = value;
                }
            }

            root.right.value = val;
        } else {
            applyFilterClassTypes(root.left, filterClass);
            applyFilterClassTypes(root.right, filterClass);
        }
    }

    private static Class getPropertyClass(String propertyName, Class clazz) {
        String[] path = propertyName.split("\\.");
        for (String prop : path) {
            clazz = BeanUtils.findPropertyType(prop, new Class[]{clazz});
        }
        return clazz;
    }

    static {
        try {
            ConvertUtils.register(new InetAddressConverter(), InetAddress.class);
            ConvertUtils.register(new UUIDConverter(), UUID.class);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }
}
