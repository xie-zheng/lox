package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jlox.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean match(char expected) {
        char c = peek();
        if (c != expected) {
            return false;
        }

        current += 1;
        return true;
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    private char peekNext() {
        current += 1;
        if (isAtEnd()) {
            return '\0';
        }
        current -= 1;

        return source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private char advance() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '\0':
                break;
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;
            // operators
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    // comment
                    while (!isAtEnd() && peek() != '\n') {
                        advance();
                    }
                } else if (match('*')) {
                    // multiline comment
                    mulcomment();
                } else {
                    // division
                    addToken(SLASH);
                }
                break;
            // whitespaces
            // ----------------
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line += 1;
                break;
            case '"':
                string();
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                    break;
                }
        }
    }

    private void string() {
        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\n') {
                line += 1;
            }
            advance();
        }

        if (isAtEnd()) {
            // string not ending
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek())) advance();
        }

//        // fix identifier parsing error
//        // 这里不是很严谨，如果数字后面接的是';'呢？
//        // 还有其他可能性吗？
//        if (!match(' ') && !match(')') && !match(';')) {
//            Lox.error(line, "Number can't be the start of an identifier");
//            while (isAlphaNumeric(peek())) {
//                advance();
//            }
//            return;
//        }
        addToken(NUMBER,
                Double.parseDouble(source.substring(start, current)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String identifier = source.substring(start, current);
        // keywords
        if (keywords.containsKey(identifier)) {
            addToken(keywords.get(identifier));
        } else {
            addToken(IDENTIFIER);
        }
    }

    private void mulcomment() {
        int parentheses = 1;

        while (!isAtEnd() && parentheses > 0) {
            char c = advance();

            if (c == '/') {
                // another parentheses
                if (match('*')) {
                    parentheses += 1;
                }
            } else if (c == '*') {
                    // right parentheses
                    if (match('/')) {
                        parentheses -= 1;
                    }
            } else if (c == '\n') {
                line += 1;
            }

        }
    }
}