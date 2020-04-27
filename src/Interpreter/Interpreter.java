package Interpreter;


import Lexer.Lexer;

import javax.swing.*;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import NodeType.NodeType;
import Node.Node;

public class Interpreter {
    private final static Map<String, Integer> GLOBALS = new HashMap<>();
    private static Scanner s;
    private final static Map<String, NodeType> STR_TO_NODES = new HashMap<>();

    protected static final Logger LOGGER = Logger.getLogger(Lexer.class.getName());

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

    private static String outputString = "";
    static Integer interpret(Node n) {
        if (n == null) {
            return 0;
        }
        switch (n.getNodeType()) {
            case ND_INTEGER:
                return Integer.parseInt(n.getValue());
            case ND_IDENTIFIER:
                return fetchVar(n.getValue());
            case ND_ASSIGN:
                GLOBALS.put(n.getLeft().getValue(), interpret(n.getRight()));
                return 0;
            case ND_ADD:
                return interpret(n.getLeft()) + interpret(n.getRight());
            case ND_SUBTRACT:
                return interpret(n.getLeft()) - interpret(n.getRight());
            case ND_MULTIPLY:
                return interpret(n.getLeft()) * interpret(n.getRight());
            case ND_DIVIDE:
                return interpret(n.getLeft()) / interpret(n.getRight());
            case ND_MOD:
                return interpret(n.getLeft()) % interpret(n.getRight());
            case ND_LESS:
                return booleanToInteger(interpret(n.getLeft()) < interpret(n.getRight()));
            case ND_GREATER:
                return booleanToInteger(interpret(n.getLeft()) > interpret(n.getRight()));
            case ND_NEGATE:
                return -interpret(n.getLeft());
            case ND_IF:
                if (interpret(n.getLeft()) != 0) {
                    interpret(n.getRight().getLeft());
                } else {
                    interpret(n.getRight().getRight());
                }
                return 0;
            case ND_SEQUENCE:
                interpret(n.getLeft());
                interpret(n.getRight());
                return 0;
            case ND_PRINT:
                outputString += String.format("%d,", interpret(n.getLeft()));
                return 0;
            default:
                return 0;
        }
    }

    public static Node loadAst() {
        String command, value;
        String line;
        Node left, right;

        if (s.hasNext()) {
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
                LOGGER.log(Level.WARNING, "Exception(Interpreter): command not found: '" + command + "'");
                JOptionPane.showMessageDialog(null, "Exception(Interpreter): command not found: '" + command + "'");
                return null;
            }
            if (value != null) {
                return Node.makeLeaf(STR_TO_NODES.get(command), value);
            }
            left = loadAst(); right = loadAst();
            return Node.makeNode(STR_TO_NODES.get(command), left, right);
        }
        return null;
    }

    public String printInterpret() {
        if(outputString.length() > 0) {
            String tempOutputString = outputString.substring(0, outputString.length() - 1);
            outputString = "";
            return tempOutputString;
        }
        return "";
    }

    public static Interpreter toInterpret(String parserSource) {
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
            s = new Scanner(parserSource);
            n = loadAst();
            interpret(n);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception(Interpreter): " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Exception(Interpreter): " + e.getMessage());
        }
        finally {
            s.close();
        }
        return new Interpreter();
    }
}
