package TokenType;

import NodeType.NodeType;

public enum TokenType {
    MULTIPLY(false, true, 13, NodeType.ND_MULTIPLY),
    DIVIDE(false, true, 13, NodeType.ND_DIVIDE),
    MOD(false, true, 13, NodeType.ND_MOD),
    ADD(false, true, 12, NodeType.ND_ADD),
    SUBTRACT(false, true, 12, NodeType.ND_SUBTRACT),
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
    INTEGER(false, false, -1, NodeType.ND_INTEGER),
    END(false, false, -1, NodeType.ND_NONE),
    EXPRESSION(false, false, -1, NodeType.ND_IDENTIFIER);


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

    public boolean isRightAssoc() { return this.rightAssoc; }
    public boolean isBinary() { return this.isBinary; }
    public int getPrecedence() { return this.precedence; }
    public NodeType getNodeType() { return this.nodeType; }
}
