package com.breakinblocks.painterjs.math;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

public class MathExpression {
    private final Node root;

    public MathExpression(Node root) {
        this.root = root;
    }

    public double eval(Map<String, Double> vars) {
        return root.eval(vars);
    }

    public static MathExpression parse(String expression) {
        return new Parser(expression).parse();
    }

    interface Node {
        double eval(Map<String, Double> vars);
    }

    static class ConstantNode implements Node {
        double val;

        ConstantNode(double v) {
            val = v;
        }

        public double eval(Map<String, Double> vars) {
            return val;
        }
    }

    static class VariableNode implements Node {
        String name;

        VariableNode(String n) {
            name = n;
        }

        public double eval(Map<String, Double> vars) {
            return vars.getOrDefault(name, 0.0);
        }
    }

    static class BinaryNode implements Node {
        Node left, right;
        DoubleBinaryOperator op;

        BinaryNode(Node l, Node r, DoubleBinaryOperator o) {
            left = l;
            right = r;
            op = o;
        }

        public double eval(Map<String, Double> vars) {
            return op.applyAsDouble(left.eval(vars), right.eval(vars));
        }
    }

    static class UnaryNode implements Node {
        Node child;
        DoubleUnaryOperator op;

        UnaryNode(Node c, DoubleUnaryOperator o) {
            child = c;
            op = o;
        }

        public double eval(Map<String, Double> vars) {
            return op.applyAsDouble(child.eval(vars));
        }
    }

    static class FunctionNode implements Node {
        String func;
        List<Node> args;

        FunctionNode(String f, List<Node> a) {
            func = f;
            args = a;
        }

        public double eval(Map<String, Double> vars) {
            double[] v = new double[args.size()];
            for (int i = 0; i < v.length; i++)
                v[i] = args.get(i).eval(vars);

            switch (func) {
                case "sin":
                    return Math.sin(v[0]);
                case "cos":
                    return Math.cos(v[0]);
                case "tan":
                    return Math.tan(v[0]);
                case "asin":
                    return Math.asin(v[0]);
                case "acos":
                    return Math.acos(v[0]);
                case "atan":
                    return Math.atan(v[0]);
                case "atan2":
                    return Math.atan2(v[0], v[1]);
                case "sqrt":
                    return Math.sqrt(v[0]);
                case "abs":
                    return Math.abs(v[0]);
                case "min":
                    return Math.min(v[0], v[1]);
                case "max":
                    return Math.max(v[0], v[1]);
                case "pow":
                    return Math.pow(v[0], v[1]);
                case "floor":
                    return Math.floor(v[0]);
                case "ceil":
                    return Math.ceil(v[0]);
                case "rad":
                    return Math.toRadians(v[0]);
                case "deg":
                    return Math.toDegrees(v[0]);
                case "log":
                    return Math.log(v[0]);
                case "rgb":
                    return 0xFF000000 | ((int) (v[0] * 255) << 16) | ((int) (v[1] * 255) << 8) | (int) (v[2] * 255);
                case "hsv":
                    return hsvToRgb(v[0], v[1], v[2]);
                case "random":
                    return Math.random();
                default:
                    return 0;
            }
        }

        private static int hsvToRgb(double h, double s, double v) {
            double r = 0, g = 0, b = 0;
            int i = (int) (h * 6);
            double f = h * 6 - i;
            double p = v * (1 - s);
            double q = v * (1 - f * s);
            double t = v * (1 - (1 - f) * s);
            switch (i % 6) {
                case 0: r = v; g = t; b = p; break;
                case 1: r = q; g = v; b = p; break;
                case 2: r = p; g = v; b = t; break;
                case 3: r = p; g = q; b = v; break;
                case 4: r = t; g = p; b = v; break;
                case 5: r = v; g = p; b = q; break;
            }
            return 0xFF000000 | ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
        }
    }

    private static class Parser {
        String str;
        int pos = -1, ch;

        Parser(String s) {
            str = s;
            nextChar();
        }

        void nextChar() {
            ch = (++pos < str.length()) ? str.charAt(pos) : -1;
        }

        boolean eat(int charToEat) {
            while (ch == ' ')
                nextChar();
            if (ch == charToEat) {
                nextChar();
                return true;
            }
            return false;
        }

        MathExpression parse() {
            Node root = parseExpression();
            if (pos < str.length())
                throw new RuntimeException("Unexpected: " + (char) ch);
            return new MathExpression(root);
        }

        Node parseExpression() {
            Node x = parseTerm();
            for (; ; ) {
                if (eat('+'))
                    x = new BinaryNode(x, parseTerm(), (a, b) -> a + b); // addition
                else if (eat('-'))
                    x = new BinaryNode(x, parseTerm(), (a, b) -> a - b); // subtraction
                else
                    return x;
            }
        }

        Node parseTerm() {
            Node x = parseFactor();
            for (; ; ) {
                if (eat('*'))
                    x = new BinaryNode(x, parseFactor(), (a, b) -> a * b); // multiplication
                else if (eat('/'))
                    x = new BinaryNode(x, parseFactor(), (a, b) -> a / b); // division
                else if (eat('%'))
                    x = new BinaryNode(x, parseFactor(), (a, b) -> a % b); // modulus
                else
                    return x;
            }
        }

        Node parseFactor() {
            if (eat('+'))
                return parseFactor(); // unary plus
            if (eat('-'))
                return new UnaryNode(parseFactor(), a -> -a); // unary minus

            Node x;
            int startPos = pos;
            if (eat('(')) { // parentheses
                x = parseExpression();
                eat(')');
            } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                while ((ch >= '0' && ch <= '9') || ch == '.')
                    nextChar();
                x = new ConstantNode(Double.parseDouble(str.substring(startPos, pos)));
            } else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '$' || ch == '_') { // functions or
                // variables
                while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || ch == '$'
                        || ch == '_' || ch == '-')
                    nextChar();
                String func = str.substring(startPos, pos);
                if (eat('(')) { // function call
                    List<Node> args = new ArrayList<>();
                    if (!eat(')')) {
                        do {
                            args.add(parseExpression());
                        } while (eat(','));
                        eat(')');
                    }
                    x = new FunctionNode(func, args);
                } else { // variable or constant
                    if (func.equals("PI"))
                        x = new ConstantNode(Math.PI);
                    else if (func.equals("HALF_PI"))
                        x = new ConstantNode(Math.PI / 2);
                    else if (func.equals("TWO_PI"))
                        x = new ConstantNode(Math.PI * 2);
                    else if (func.equals("E"))
                        x = new ConstantNode(Math.E);
                    else if (func.equals("true"))
                        x = new ConstantNode(1);
                    else if (func.equals("false"))
                        x = new ConstantNode(0);
                    else
                        x = new VariableNode(func);
                }
            } else {
                throw new RuntimeException("Unexpected: " + (char) ch);
            }

            return x;
        }
    }
}
