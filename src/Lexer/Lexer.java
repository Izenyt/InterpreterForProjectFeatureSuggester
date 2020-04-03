package Lexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private int line;
    private int position;
    private char chr;
    private int pos;
    private String str;

    public Lexer(String source) {
        this.line = 1;
        this.pos = 0;
        this.position = 0;
        this.str = source;
        this.chr = this.str.charAt(0);
    }

    protected static final Logger LOGGER = Logger.getLogger(Lexer.class.getName());
    private static final String PATH = "src\\Lexer\\LexerOut.txt";
    private static final File FILE = new File(PATH);

    private static final Map<String, TokenType> KEYWORDS = Map.of("if", TokenType.IF, "else", TokenType.ELSE, "print", TokenType.PRINT);

    private Pattern intPattern = Pattern.compile("[0-9]+");

    protected Matcher intMatcher;

    static class Token {
        public TokenType tokentype;
        public String value;
        public int line;
        public int pos;

        Token(TokenType token, String value, int line, int pos) {
            this.tokentype = token;
            this.value = value;
            this.line = line;
            this.pos = pos;
        }

        @Override
        public String toString() {
            String result = String.format("%4d  %4d %-15s", this.line, this.pos, this.tokentype);
            switch (this.tokentype) {
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

    private enum TokenType {
        END, MULTIPLY, DIVIDE, MOD, ADD, SUBTRACT,
        LESS, GREATER, ASSIGN, IF, ELSE, LEFT_PAREN, RIGHT_PAREN, PRINT,
        LEFT_BRACE, RIGHT_BRACE, SEMICOLON, COMMA, IDENTIFIER, INTEGER, EXPRESSION
    }

    private static void error(int line, int pos, String msg) {
        if (line > 0 && pos > 0) {
            LOGGER.log(Level.SEVERE, String.format("Lexer: %s in line %d, pos %d\n", msg, line, pos));
        } else {
            LOGGER.log(Level.SEVERE,("Lexer: " + msg));
        }
    }

    private void nextChar() {
        this.pos++;
        this.position++;
        if (this.position >= this.str.length()) {
            this.chr = '\u0000';
            return;
        }
        this.chr = this.str.charAt(this.position);
        if (this.chr == '\n') {
            this.line++;
            this.pos = 0;
        }
    }

    private Token idOrInt(int line, int pos) {
        boolean isNumber = true;
        StringBuilder text = new StringBuilder();

        while (Character.isAlphabetic(this.chr) || Character.isDigit(this.chr) || this.chr == '@') {
            text.append(this.chr);
            if (!Character.isDigit(this.chr)) {
                isNumber = false;
            }
            nextChar();
        }

        if (text.toString().equals("")) {
            error(line, pos, String.format("inaccessible character: (%d) %c", (int) this.chr, this.chr));
        }

        intMatcher = intPattern.matcher(text);
        if (intMatcher.matches()) {
            if (!isNumber) {
                error(line, pos, String.format("invalid number: %s", text.toString()));
            }
            return new Token(TokenType.INTEGER, text.toString(), line, pos);
        }
        
        if (KEYWORDS.containsKey(text.toString())) {
            return new Token(KEYWORDS.get(text.toString()), "", line, pos);
        }

        if (text.charAt(0) == '@') {
            return new Token(TokenType.IDENTIFIER, text.toString().replace("@", ""), line, pos);
        }

        return new Token(TokenType.EXPRESSION, text.toString(), line, pos);
    }

    public Token getToken() {
        int line;
        int pos;

        while (Character.isWhitespace(this.chr)) {
            nextChar();
        }

        line = this.line;
        pos = this.pos;

        switch (this.chr) {
            case '\u0000':
                return new Token(TokenType.END, "", this.line, this.pos);
            case '{':
                nextChar();
                return new Token(TokenType.LEFT_BRACE, "", line, pos);
            case '}':
                nextChar();
                return new Token(TokenType.RIGHT_BRACE, "", line, pos);
            case '(':
                nextChar();
                return new Token(TokenType.LEFT_PAREN, "", line, pos);
            case ')':
                nextChar();
                return new Token(TokenType.RIGHT_PAREN, "", line, pos);

            case '+':
                nextChar();
                return new Token(TokenType.ADD, "", line, pos);
            case '-':
                nextChar();
                return new Token(TokenType.SUBTRACT, "", line, pos);
            case '*':
                nextChar();
                return new Token(TokenType.MULTIPLY, "", line, pos);
            case '/':
                nextChar();
                return new Token(TokenType.DIVIDE, "", line, pos);
            case '%':
                nextChar();
                return new Token(TokenType.MOD, "", line, pos);

            case '<':
                nextChar();
                return new Token(TokenType.LESS, "", line, pos);
            case '>':
                nextChar();
                return new Token(TokenType.GREATER, "", line, pos);

            case '=':
                nextChar();
                return new Token(TokenType.ASSIGN, "", line, pos);
            case ';':
                nextChar();
                return new Token(TokenType.SEMICOLON, "", line, pos);
            case ',':
                nextChar();
                return new Token(TokenType.COMMA, "", line, pos);
            default:
                return idOrInt(line, pos);
        }
    }


    public void printTokens() throws FileNotFoundException {
        Token t;
        List<Token> tokens = new LinkedList<>();

        while ((t = getToken()).tokentype != TokenType.END) {
            tokens.add(t);
        }

        for (int i = 0; i < tokens.size() - 1; i++) {
            if((i == 0 && tokens.get(0).tokentype == TokenType.INTEGER)
            || (i == 0 && tokens.get(0).tokentype == TokenType.SUBTRACT)) {
                tokens.add(i, new Token(KEYWORDS.get("print"), "", 0, 0));
                i++;
            }
            if((tokens.get(i).tokentype == TokenType.SEMICOLON && tokens.get(i + 1).tokentype == TokenType.EXPRESSION)
            || (tokens.get(i).tokentype == TokenType.RIGHT_BRACE && tokens.get(i + 1).tokentype == TokenType.EXPRESSION)
            ||(tokens.get(i).tokentype == TokenType.LEFT_BRACE && tokens.get(i + 1).tokentype == TokenType.EXPRESSION)
            ||(tokens.get(i).tokentype == TokenType.SEMICOLON && tokens.get(i + 1).tokentype == TokenType.INTEGER)
            ||(tokens.get(i).tokentype == TokenType.RIGHT_BRACE && tokens.get(i + 1).tokentype == TokenType.INTEGER)
            ||(tokens.get(i).tokentype == TokenType.LEFT_BRACE && tokens.get(i + 1).tokentype == TokenType.INTEGER)
            ||(tokens.get(i).tokentype == TokenType.SEMICOLON && tokens.get(i + 1).tokentype == TokenType.SUBTRACT)
            ||(tokens.get(i).tokentype == TokenType.RIGHT_BRACE && tokens.get(i + 1).tokentype == TokenType.SUBTRACT)
            ||(tokens.get(i).tokentype == TokenType.LEFT_BRACE && tokens.get(i + 1).tokentype == TokenType.SUBTRACT))
            {
                tokens.add(i + 1, new Token(KEYWORDS.get("print"), "", 0, 0));
                i++;
            }
        }

        try (PrintWriter printWriter = new PrintWriter(FILE.getPath())) {
            for (Token token : tokens) {
                printWriter.println(token);
            }
            printWriter.println(new Token(TokenType.END, "", this.line, this.pos));
        }

    }

    public static Lexer toLex() throws FileNotFoundException {
        File f = new File("src\\Lexer\\LexerIn.txt");
        Scanner s = new Scanner(f);
        StringBuilder source = new StringBuilder();

        while (s.hasNext()) {
            source.append(s.nextLine()).append("\n");
        }

        return new Lexer(source.toString());
    }
}