package Token;

import TokenType.TokenType;

public class Token {
    private final TokenType tokenType;
    private final String value;
    private final int line;
    private final int pos;

    public TokenType getTokenType() {
        return tokenType;
    }

    public String getValue() {
        return value;
    }

    public int getLine() {
        return line;
    }

    public int getPos() {
        return pos;
    }

    public Token(TokenType tokenType, String value, int line, int pos) {
        this.tokenType = tokenType;
        this.value = value;
        this.line = line;
        this.pos = pos;
    }

    @Override
    public String toString() {
        String result = String.format("%4d  %4d %-15s", this.line, this.pos, this.tokenType);
        switch (tokenType) {
            case INTEGER:
            case IDENTIFIER:
                result += String.format(" %s", value);
                break;
            case EXPRESSION:
                result = String.format("%4d  %4d %-15s", this.line, this.pos, TokenType.IDENTIFIER);
                result += String.format(" %s", value);
                break;
        }
        return result;
    }
}
