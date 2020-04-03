package Parser;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Parser {
    private static Component parserFrame;
    private List<Token> source;
    private Token token;
    private int position;

    protected static final Logger LOGGER = Logger.getLogger(Parser.class.getName());

    private static class Node {
        public NodeType nodeType;
        public Node left;
        public Node right;
        public String value;

        Node(NodeType nodeType, Node left, Node right, String value) {
            this.nodeType = nodeType;
            this.left = left;
            this.right = right;
            this.value = value;
        }

        public static Node makeNode(NodeType nodetype, Node left, Node right) { return new Node(nodetype, left, right, ""); }
        public static Node makeNode(NodeType nodetype, Node left) {
            return new Node(nodetype, left, null, "");
        }
        public static Node makeLeaf(NodeType nodetype, String value) { return new Node(nodetype, null, null, value); }
    }

    private static class Token {
        public TokenType tokentype;
        public String value;
        public int line;
        public int pos;

        Token(TokenType token, String value, int line, int pos) {
            this.tokentype = token; this.value = value; this.line = line; this.pos = pos;
        }
        @Override
        public String toString() {
            return String.format("%5d  %5d %-15s %s", this.line, this.pos, this.tokentype, this.value);
        }
    }

    private enum TokenType {
        END(false, false, -1, NodeType.ND_NONE),
        MULTIPLY(false, true, 13, NodeType.ND_MUL),
        DIVIDE(false, true, 13, NodeType.ND_DIV),
        MOD(false, true, 13, NodeType.ND_MOD),
        ADD(false, true, 12, NodeType.ND_ADD),
        SUBTRACT(false, true, 12, NodeType.ND_SUB),
        NEGATE(false, false, 14, NodeType.ND_NEGATE),
        LESS(false, true, 10, NodeType.ND_LESS),
        GREATER(false, true, 10, NodeType.ND_GREATER),
        ASSIGN(false, false, -1, NodeType.ND_ASSIGN),
        IF(false, false, -1, NodeType.ND_IF),
        ELSE(false, false, -1, NodeType.ND_NONE),
        PRINT(false, false, -1, NodeType.ND_NONE),
        LEFT_PAREN(false, false, -1, NodeType.ND_NONE),
        RIGHT_PAREN(false, false, -1, NodeType.ND_NONE),
        LEFT_BRACE(false, false, -1, NodeType.ND_NONE),
        RIGHT_BRACE(false, false, -1, NodeType.ND_NONE),
        SEMICOLON(false, false, -1, NodeType.ND_NONE),
        COMMA(false, false, -1, NodeType.ND_NONE),
        IDENTIFIER(false, false, -1, NodeType.ND_IDENTIFIER),
        INTEGER(false, false, -1, NodeType.ND_INTEGER);

        private final int precedence;
        private final boolean rightAssoc;
        private final boolean isBinary;
        private final NodeType nodeType;

        TokenType(boolean rightAssoc, boolean isBinary, int precedence, NodeType nodeType) {
            this.rightAssoc = rightAssoc;
            this.isBinary = isBinary;
            this.precedence = precedence;
            this.nodeType = nodeType;
        }

        boolean isRightAssoc() { return this.rightAssoc; }
        boolean isBinary() { return this.isBinary; }
        int getPrecedence() { return this.precedence; }
        NodeType getNodeType() { return this.nodeType; }
    }

    private enum NodeType {
        ND_NONE(""), ND_IDENTIFIER("IDENTIFIER"), ND_INTEGER("INTEGER"), ND_SEQUENCE("SEQUENCE"),
        ND_IF("IF"), ND_PRINT("PRINT"),
        ND_ASSIGN("ASSIGN"), ND_NEGATE("NEGATE"), ND_MUL("MULTIPLY"), ND_DIV("DIVIDE"), ND_MOD("MOD"), ND_ADD("ADD"),
        ND_SUB("SUBTRACT"), ND_LESS("LESS"),
        ND_GREATER("GREATER");

        private final String name;

        NodeType(String name) {
            this.name = name;
        }

        @Override
        public String toString() { return this.name; }
    }

    private static void error(int line, int pos, String msg) {
        if (line > 0 && pos > 0) {
            LOGGER.log(Level.WARNING,String.format("%s in line %d, pos %d\n", msg, line, pos));
            JOptionPane.showMessageDialog(parserFrame, String.format("Предупреждение: %s in line %d, pos %d\n", msg, line, pos));
        } else {
            LOGGER.log(Level.SEVERE,msg);
            JOptionPane.showMessageDialog(parserFrame, msg);
        }
        Thread.currentThread().stop();
    }

    private Parser(List<Token> source) {
        this.source = source;
        this.token = null;
        this.position = 0;
    }
    void nextToken() {
        this.token = this.source.get(this.position++);
    }
    Node expr(int p) {
        Node result = null, node;
        TokenType op;
        int q;

        if (this.token.tokentype == TokenType.LEFT_PAREN) {
            result = parenExpr();
        } else if (this.token.tokentype == TokenType.ADD || this.token.tokentype == TokenType.SUBTRACT) {
            op = (this.token.tokentype == TokenType.SUBTRACT) ? TokenType.NEGATE : TokenType.ADD;
            nextToken();
            node = expr(TokenType.NEGATE.getPrecedence());
            result = (op == TokenType.NEGATE) ? Node.makeNode(NodeType.ND_NEGATE, node) : node;
        } else if (this.token.tokentype == TokenType.IDENTIFIER) {
            result = Node.makeLeaf(NodeType.ND_IDENTIFIER, this.token.value);
            nextToken();
        } else if (this.token.tokentype == TokenType.INTEGER) {
            result = Node.makeLeaf(NodeType.ND_INTEGER, this.token.value);
            nextToken();
        } else {
            error(this.token.line, this.token.pos, "Expecting a primary, found: " + this.token.tokentype);
        }

        while (this.token.tokentype.isBinary() && this.token.tokentype.getPrecedence() >= p) {
            op = this.token.tokentype;
            nextToken();
            q = op.getPrecedence();
            if (!op.isRightAssoc()) {
                q++;
            }
            node = expr(q);
            result = Node.makeNode(op.getNodeType(), result, node);
        }
        return result;
    }
    Node parenExpr() {
        expect("parenExpr", TokenType.LEFT_PAREN);
        Node node = expr(0);
        expect("parenExpr", TokenType.RIGHT_PAREN);
        return node;
    }
    void expect(String msg, TokenType s) {
        if (this.token.tokentype == s) {
            nextToken();
            return;
        }
        error(this.token.line, this.token.pos, msg + ": Expecting '" + s + "', found: '" + this.token.tokentype + "'");
    }
    Node statement() {
        Node s, s2, t = null, e, v;
        if (this.token.tokentype == TokenType.IF) {
            nextToken();
            e = parenExpr();
            s = statement();
            s2 = null;
            if (this.token.tokentype == TokenType.ELSE) {
                nextToken();
                s2 = statement();
            }
            t = Node.makeNode(NodeType.ND_IF, e, Node.makeNode(NodeType.ND_IF, s, s2));
        } else if (this.token.tokentype == TokenType.PRINT) {
            nextToken();
            while (true) {
                e = Node.makeNode(NodeType.ND_PRINT, expr(0), null);
                t = Node.makeNode(NodeType.ND_SEQUENCE, t, e);
                if (this.token.tokentype != TokenType.COMMA) {
                    break;
                }
                nextToken();
            }
            expect("Print", TokenType.SEMICOLON);
        } else if (this.token.tokentype == TokenType.SEMICOLON) {
            nextToken();
        } else if (this.token.tokentype == TokenType.IDENTIFIER) {
            v = Node.makeLeaf(NodeType.ND_IDENTIFIER, this.token.value);
            nextToken();
            if(this.token.tokentype == TokenType.ADD) {
                expect("assign", TokenType.ADD);
                e = expr(0);
                t = Node.makeNode(NodeType.ND_ADD, v, e);
            }
            else if(this.token.tokentype == TokenType.SUBTRACT) {
                expect("assign", TokenType.SUBTRACT);
                e = expr(0);
                t = Node.makeNode(NodeType.ND_SUB, v, e);
            }
            else if(this.token.tokentype == TokenType.MULTIPLY) {
                expect("assign", TokenType.MULTIPLY);
                e = expr(0);
                t = Node.makeNode(NodeType.ND_MUL, v, e);
            }
            else if(this.token.tokentype == TokenType.DIVIDE) {
                expect("assign", TokenType.DIVIDE);
                e = expr(0);
                t = Node.makeNode(NodeType.ND_DIV, v, e);
            }
            else if(this.token.tokentype == TokenType.MOD) {
                expect("assign", TokenType.MOD);
                e = expr(0);
                t = Node.makeNode(NodeType.ND_MOD, v, e);
            }
            else {
                expect("assign", TokenType.ASSIGN);
                e = expr(0);
                t = Node.makeNode(NodeType.ND_ASSIGN, v, e);
            }
            expect("assign", TokenType.SEMICOLON);
        } else if (this.token.tokentype == TokenType.LEFT_BRACE) {
            nextToken();
            while (this.token.tokentype != TokenType.RIGHT_BRACE && this.token.tokentype != TokenType.END) {
                t = Node.makeNode(NodeType.ND_SEQUENCE, t, statement());
            }
            expect("LBrace", TokenType.RIGHT_BRACE);
        } else if (this.token.tokentype == TokenType.END) {
        } else {
            error(this.token.line, this.token.pos, "Expecting start of statement, found: " + this.token.tokentype);
        }
        return t;
    }

    public Node parse() {
        Node t = null;
        nextToken();
        while (this.token.tokentype != TokenType.END) {
            t = Node.makeNode(NodeType.ND_SEQUENCE, t, statement());
        }
        return t;
    }

    private static final String path = "src\\Parser\\ParserOut.txt";
    private static final File file = new File(path);
    private static String outputString = "";

    public void printAST() throws FileNotFoundException {
        try (PrintWriter printWriter = new PrintWriter(file.getPath())) {
            printWriter.println(outputString);
        }
        outputString = "";
    }

    private void AST(Node t) {
        if (t == null) {
            outputString += ";\n";
        } else {
            outputString += String.format("%-14s", t.nodeType);
            if (t.nodeType == NodeType.ND_IDENTIFIER || t.nodeType == NodeType.ND_INTEGER) {
                outputString += " " + t.value + "\n";
            } else {
                outputString += "\n";
                AST(t.left);
                AST(t.right);
            }
        }
    }

    public Parser() {
        StringBuilder value;
        String token;
        int line, pos;
        List<Token> list = new ArrayList<>();
        Map<String, TokenType> strToTokens = new HashMap<>();

        strToTokens.put("END", TokenType.END);
        strToTokens.put("MULTIPLY", TokenType.MULTIPLY);
        strToTokens.put("DIVIDE", TokenType.DIVIDE);
        strToTokens.put("MOD", TokenType.MOD);
        strToTokens.put("ADD", TokenType.ADD);
        strToTokens.put("SUBTRACT", TokenType.SUBTRACT);
        strToTokens.put("LESS", TokenType.LESS);
        strToTokens.put("GREATER", TokenType.GREATER);
        strToTokens.put("ASSIGN", TokenType.ASSIGN);
        strToTokens.put("IF", TokenType.IF);
        strToTokens.put("ELSE", TokenType.ELSE);
        strToTokens.put("PRINT", TokenType.PRINT);
        strToTokens.put("LEFT_PAREN", TokenType.LEFT_PAREN);
        strToTokens.put("RIGHT_PAREN", TokenType.RIGHT_PAREN);
        strToTokens.put("LEFT_BRACE", TokenType.LEFT_BRACE);
        strToTokens.put("RIGHT_BRACE", TokenType.RIGHT_BRACE);
        strToTokens.put("SEMICOLON", TokenType.SEMICOLON);
        strToTokens.put("COMMA", TokenType.COMMA);
        strToTokens.put("IDENTIFIER", TokenType.IDENTIFIER);
        strToTokens.put("INTEGER", TokenType.INTEGER);

        try(Scanner s = new Scanner(new File("src\\Lexer\\LexerOut.txt"))) {
            while (s.hasNext()) {
                String str = s.nextLine();
                StringTokenizer st = new StringTokenizer(str);
                line = Integer.parseInt(st.nextToken());
                pos = Integer.parseInt(st.nextToken());
                token = st.nextToken();
                value = new StringBuilder();

                while (st.hasMoreTokens()) {
                    value.append(st.nextToken()).append(" ");
                }

                if (strToTokens.containsKey(token)) {
                    list.add(new Token(strToTokens.get(token), value.toString(), line, pos));
                }
            }
            Parser p = new Parser(list);
            p.AST(p.parse());
        } catch (Exception e) {
            error(-1, -1, "Exception: " + e.getMessage());
        }
    }
}