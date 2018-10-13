package com.dandigit.jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* lol /* */

import static com.dandigit.jlox.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    // We need to ignore newlines within parentheses, so we keep track
    // of how many are open.
    private int openParen = 0;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",     AND);
        keywords.put("class",   CLASS);
        keywords.put("else",    ELSE);
        keywords.put("false",   FALSE);
        keywords.put("for",     FOR);
        keywords.put("fun",     FUN);
        keywords.put("if",      IF);
        keywords.put("nil",     NIL);
        keywords.put("or",      OR);
        keywords.put("return",  RETURN);
        keywords.put("super",   SUPER);
        keywords.put("this",    THIS);
        keywords.put("true",    TRUE);
        keywords.put("var",     VAR);
        keywords.put("while",   WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We're at the beginning of the next lexeme
            start = current;
            scanToken();
        }

        // Finally, add the EOF token.
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // Single character tokens
            case '(': addToken(LEFT_PAREN); ++openParen; break;
            case ')': addToken(RIGHT_PAREN); --openParen; break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case ':': addToken(COLON); break;
            case '?': addToken(QUESTION); break;

            // 1 or 2 character tokens
            case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;

            // Slash: division or comment?
            case '/':
                // Single line comment
                if (match('/')) {
                    // Advance until the end of the line, we have found a comment
                    while (peek() != '\n' && !isAtEnd()) advance();
                // Multiline comment
                } else if (match('*')) {
                    blockComment();
                // Plain ol' slash
                } else {
                    addToken(SLASH);
                }
                break;

            case '"': string(); break;

            // Handle newline/eof
            case '\n':
                ++line;
                // Do we need an implicit semicolon here?
                Token lastToken = tokens.get(tokens.size() - 1);
                if (openParen == 0 &&
                        lastToken.type != SEMICOLON &&
                        lastToken.type != LEFT_BRACE &&
                        lastToken.type != RIGHT_BRACE)
                    addToken(SEMICOLON);

                break;

            // Useless characters, but ones we must ignore
            case ' ':
            case '\r':
            case '\t':
                break; // Ignore

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlphaOrUnderscore(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void blockComment() {
        while (peek() != '*' && !isAtEnd()) {
            if (peek() == '\n') ++line;
            if (peek() == '/') {
                if (peekNext() == '*') {
                    advance();
                    advance();
                    blockComment();
                }
            }
            advance();
        }

        // String without close quote
        if (isAtEnd()) {
            Lox.error(line, "Unterminated block comment");
        }

        advance();

        if (!match('/')) {
            advance();
            blockComment();
        }
    }

    private void identifier() {
        while (isAlphaNumericOrUnderscore(peek())) advance();

        // Grab the identifier's value
        String text = source.substring(start, current);

        // See if the value is a reserved word
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;

        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER,
                Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        // Keep looping until a matching " is found
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') ++line;
            advance();
        }

        // String without close quote
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // Eat the close quote
        advance();

        // Handle escape sequences
        String value = unescape(source.substring(start + 1, current - 1));

        addToken(STRING, value);
    }

    // Conditional advance(), only moves forward if next char is expected
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        ++current;
        return true;
    }

    // Like advance(), but it does not consume the character
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private char beforePrevious() {
        return source.charAt(current - 2);
    }

    private boolean isAlphaOrUnderscore(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumericOrUnderscore(char c) {
        return isAlphaOrUnderscore(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        ++current;
        return source.charAt(current -1);
    }

    private String unescape(String escaped) {
        String finalString = "";

        for (int i = 0; i < escaped.length(); ++i) {
            if (escaped.charAt(i) == '\\') {
                ++i;
                switch (escaped.charAt(i)) {
                    case 'n':
                        finalString += "\n";
                        break;

                    case '\\':
                        finalString += "\\";
                        break;

                    default:
                        Lox.error(line, "Unrecognised escape sequence '\\" + escaped.charAt(i) + "'.");
                }
            } else {
                finalString += escaped.charAt(i);
            }
        }

        return finalString;
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}