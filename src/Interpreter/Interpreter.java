package Interpreter;


import Lexer.Lexer;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Interpreter {
    private final static Map<String, Integer> GLOBALS = new HashMap<>();
    private static Scanner s;
    private final static Map<String, NodeType> STR_TO_NODES = new HashMap<>();
    private static Component parserFrame;

    protected static final Logger LOGGER = Logger.getLogger(Lexer.class.getName());

    static class Node {
        public NodeType nodeType;
        public Node left, right;
        public String value;

        Node(NodeType nodeType, Node left, Node right, String value) {
            this.nodeType = nodeType;
            this.left = left;
            this.right = right;
            this.value = value;
        }

        public static Node makeNode(NodeType nodetype, Node left, Node right) {
            return new Node(nodetype, left, right, "");
        }
        public static Node makeLeaf(NodeType nodetype, String value) {
            return new Node(nodetype, null, null, value);
        }
    }
    enum NodeType {
        ND_NONE(";"), ND_IDENTIFIER("IDENTIFIER"), ND_INTEGER("INTEGER"),
        ND_SEQUENCE("SEQUENCE"), ND_IF("IF"),
        ND_PRINT("PRINT"),
        ND_ASSIGN("ASSIGN"), ND_NEGATE("NEGATE"),
        ND_MULTIPLY("MULTIPLY"), ND_DIVIDE("DIVIDE"),
        ND_MOD("MOD"), ND_ADD("ADD"),
        ND_SUBTRACT("SUBTRACT"), ND_LESS("LESS"),
        ND_GREATER("GREATER");

        private final String name;

        NodeType(String name) {	this.name = name; }

        @Override
        public String toString() { return this.name; }
    }

    static int booleanToInteger(boolean b) {
        return b ? 1 : 0;
    }

    static int fetchVar(String name) {
        int result;
        if (GLOBALS.containsKey(name)) {
            result = GLOBALS.get(name);
        } else {
            GLOBALS.put(name, 0);
            result = 0;
        }
        return result;
    }

    private static final String path = "src\\Interpreter\\Interpreter.txt";
    private static final File file = new File(path);
    private static String outputString = "";

    public void printInterpret() throws FileNotFoundException {
        try (PrintWriter printWriter = new PrintWriter(file.getPath())) {
            printWriter.println(outputString.substring(0, outputString.length() - 1));
        }
        outputString = "";
    }
    static Integer interpret(Node n) {
        if (n == null) {
            return 0;
        }
        switch (n.nodeType) {
            case ND_INTEGER:
                return Integer.parseInt(n.value);
            case ND_IDENTIFIER:
                return fetchVar(n.value);
            case ND_ASSIGN:
                GLOBALS.put(n.left.value, interpret(n.right));
                return 0;
            case ND_ADD:
                return interpret(n.left) + interpret(n.right);
            case ND_SUBTRACT:
                return interpret(n.left) - interpret(n.right);
            case ND_MULTIPLY:
                return interpret(n.left) * interpret(n.right);
            case ND_DIVIDE:
                return interpret(n.left) / interpret(n.right);
            case ND_MOD:
                return interpret(n.left) % interpret(n.right);
            case ND_LESS:
                return booleanToInteger(interpret(n.left) < interpret(n.right));
            case ND_GREATER:
                return booleanToInteger(interpret(n.left) > interpret(n.right));
            case ND_NEGATE:
                return -interpret(n.left);
            case ND_IF:
                if (interpret(n.left) != 0) {
                    interpret(n.right.left);
                } else {
                    interpret(n.right.right);
                }
                return 0;
            case ND_SEQUENCE:
                interpret(n.left);
                interpret(n.right);
                return 0;
            case ND_PRINT:
                outputString += String.format("%d,", interpret(n.left));
                return 0;
            default:
                LOGGER.log(Level.WARNING, "Lexer: warning! '" + n.nodeType + "' found, expecting operator");
                JOptionPane.showMessageDialog(parserFrame, "Lexer: warning!'" + n.nodeType + "' found, expecting operator");
                Thread.currentThread().stop();
                return 0;
        }
    }
    static Node loadAst() {
        String command, value;
        String line;
        Node left, right;

        while (s.hasNext()) {
            line = s.nextLine();
            value = null;
            if (line.length() >= 16) {
                command = line.substring(0, 15).trim();
                value = line.substring(15).trim();
            } else {
                command = line.trim();
            }
            if (command.equals(";")) {
                return null;
            }
            if (!STR_TO_NODES.containsKey(command)) {
                LOGGER.log(Level.WARNING, "Lexer: command not found: '" + command + "'");
                JOptionPane.showMessageDialog(parserFrame, "Lexer: command not found: '" + command + "'");
                break;
            }
            if (value != null) {
                return Node.makeLeaf(STR_TO_NODES.get(command), value);
            }
            left = loadAst(); right = loadAst();
            return Node.makeNode(STR_TO_NODES.get(command), left, right);
        }
        return null;
    }


    public static Interpreter toInterpret() {
        Node n;
        STR_TO_NODES.put(";", NodeType.ND_NONE);
        STR_TO_NODES.put("SEQUENCE", NodeType.ND_SEQUENCE);
        STR_TO_NODES.put("IDENTIFIER", NodeType.ND_IDENTIFIER);
        STR_TO_NODES.put("INTEGER", NodeType.ND_INTEGER);
        STR_TO_NODES.put("IF", NodeType.ND_IF);
        STR_TO_NODES.put("PRINT", NodeType.ND_PRINT);
        STR_TO_NODES.put("ASSIGN", NodeType.ND_ASSIGN);
        STR_TO_NODES.put("NEGATE", NodeType.ND_NEGATE);
        STR_TO_NODES.put("MULTIPLY", NodeType.ND_MULTIPLY);
        STR_TO_NODES.put("DIVIDE", NodeType.ND_DIVIDE);
        STR_TO_NODES.put("MOD", NodeType.ND_MOD);
        STR_TO_NODES.put("ADD", NodeType.ND_ADD);
        STR_TO_NODES.put("SUBTRACT", NodeType.ND_SUBTRACT);
        STR_TO_NODES.put("LESS", NodeType.ND_LESS);
        STR_TO_NODES.put("GREATER", NodeType.ND_GREATER);
        try {
            s = new Scanner(new File("src\\Parser\\ParserOut.txt"));
            n = loadAst();
            interpret(n);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lexer: exception, " + e.getMessage());
            JOptionPane.showMessageDialog(parserFrame, "Lexer: exception: " + e.getMessage());
        }
        finally {
            s.close();
        }
        return new Interpreter();
    }
}
