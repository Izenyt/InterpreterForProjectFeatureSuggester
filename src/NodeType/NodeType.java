package NodeType;

public enum  NodeType {
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
