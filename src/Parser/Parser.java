package Parser;

import javax.swing.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import Token.Token;
import TokenType.TokenType;
import NodeType.NodeType;
import Node.Node;

public class Parser {
    private final List<Token> tokens;
    private Token token;
    private int position;

    protected static final Logger LOGGER = Logger.getLogger(Parser.class.getName());

    private static void error(int line, int pos, String msg) {
        if (line > 0 && pos > 0) {
            LOGGER.log(Level.WARNING,String.format("%s in line %d, pos %d\n", msg, line, pos));
            JOptionPane.showMessageDialog(null, String.format("Excepting(Parser): %s in line %d, pos %d\n", msg, line, pos));
        } else {
            LOGGER.log(Level.SEVERE,msg);
            JOptionPane.showMessageDialog(null, msg);
        }
        Thread.currentThread().stop();
    }

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.token = null;
        this.position = 0;
    }

    void nextToken() {
        this.token = this.tokens.get(this.position++);
    }

    Node expr(int p) {
        Node result = null, node;
        TokenType op;
        int q;

        if (this.token.getTokenType() == TokenType.LEFT_PAREN) {
            result = parenExpr();
        } else if (this.token.getTokenType() == TokenType.ADD || this.token.getTokenType() == TokenType.SUBTRACT) {
            op = (this.token.getTokenType() == TokenType.SUBTRACT) ? TokenType.NEGATE : TokenType.ADD;
            nextToken();
            node = expr(TokenType.NEGATE.getPrecedence());
            result = (op == TokenType.NEGATE) ? Node.makeNode(NodeType.ND_NEGATE, node) : node;
        } else if (this.token.getTokenType() == TokenType.IDENTIFIER) {
            result = Node.makeLeaf(NodeType.ND_IDENTIFIER, this.token.getValue());
            nextToken();
        } else if (this.token.getTokenType() == TokenType.INTEGER) {
            result = Node.makeLeaf(NodeType.ND_INTEGER, this.token.getValue());
            nextToken();
        } else {
            error(this.token.getLine(), this.token.getPos(), "Expecting(Parser) a primary, found: " + this.token.getTokenType());
        }

        while (this.token.getTokenType().isBinary() && this.token.getTokenType().getPrecedence() >= p) {
            op = this.token.getTokenType();
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
        if (this.token.getTokenType() == s) {
            nextToken();
            return;
        }
        error(this.token.getLine(), this.token.getPos(), msg + ": Expecting(Parser) '" + s + "', found: '" + this.token.getTokenType() + "'");
    }

    Node statement() {
        Node s, s2, t = null, e, v;

        if (this.token.getTokenType() == TokenType.IF) {
            nextToken();
            e = parenExpr();
            s = statement();
            s2 = null;
            if (this.token.getTokenType() == TokenType.ELSE) {
                nextToken();
                s2 = statement();
            }
            t = Node.makeNode(NodeType.ND_IF, e, Node.makeNode(NodeType.ND_IF, s, s2));
        } else if (this.token.getTokenType() == TokenType.PRINT) {
            nextToken();
            while (true) {
                e = Node.makeNode(NodeType.ND_PRINT, expr(0), null);
                t = Node.makeNode(NodeType.ND_SEQUENCE, t, e);
                if (this.token.getTokenType() != TokenType.COMMA) {
                    break;
                }
                nextToken();
            }
            expect("Print", TokenType.SEMICOLON);
        } else if (this.token.getTokenType() == TokenType.SEMICOLON) {
            nextToken();
        } else if (this.token.getTokenType() == TokenType.IDENTIFIER) {
            v = Node.makeLeaf(NodeType.ND_IDENTIFIER, this.token.getValue());
            nextToken();
            if(this.token.getTokenType() == TokenType.ADD) {
                expect("assign", TokenType.ADD);
                e = expr(0);
                t = Node.makeNode(NodeType.ND_ADD, v, e);
            }
            else if(this.token.getTokenType() == TokenType.SUBTRACT) {
                expect("assign", TokenType.SUBTRACT);
                e = expr(0);
                t = Node.makeNode(NodeType.ND_SUBTRACT, v, e);
            }
            else if(this.token.getTokenType() == TokenType.MULTIPLY) {
                expect("assign", TokenType.MULTIPLY);
                e = expr(0);
                t = Node.makeNode(NodeType.ND_MULTIPLY, v, e);
            }
            else if(this.token.getTokenType() == TokenType.DIVIDE) {
                expect("assign", TokenType.DIVIDE);
                e = expr(0);
                t = Node.makeNode(NodeType.ND_DIVIDE, v, e);
            }
            else if(this.token.getTokenType() == TokenType.MOD) {
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
        } else if (this.token.getTokenType() == TokenType.LEFT_BRACE) {
            nextToken();
            while (this.token.getTokenType() != TokenType.RIGHT_BRACE && this.token.getTokenType() != TokenType.END) {
                t = Node.makeNode(NodeType.ND_SEQUENCE, t, statement());
            }
            expect("LBrace", TokenType.RIGHT_BRACE);
        }
        else {
            error(this.token.getLine(), this.token.getPos(), "Expecting(Parser) start of statement, found: " + this.token.getTokenType());
        }
        return t;
    }

    public Node parse() {
        Node node = null;
        nextToken();
        while (this.token.getTokenType() != TokenType.END) {
            node = Node.makeNode(NodeType.ND_SEQUENCE, node, statement());
        }
        return node;
    }

    private static String outputString = "";

    public String printAST() {
        String tempOutputString = outputString;
        outputString = "";
        return tempOutputString;
    }

    public void AST(Node t) {
        if (t == null) {
            outputString += ";\n";
        } else {
            outputString += String.format("%-14s", t.getNodeType());
            if (t.getNodeType() == NodeType.ND_IDENTIFIER || t.getNodeType() == NodeType.ND_INTEGER) {
                outputString += " " + t.getValue() + "\n";
            } else {
                outputString += "\n";
                AST(t.getLeft());
                AST(t.getRight());
            }
        }
    }
}