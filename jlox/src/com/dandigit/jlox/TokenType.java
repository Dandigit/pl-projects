package com.dandigit.jlox;

enum TokenType {
    // Single character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, LEFT_SQUARE, RIGHT_SQUARE,
    COMMA, DOT, MINUS, PERCENTAGE, PLUS, SEMICOLON, SLASH, STAR,
    COLON, QUESTION,

    // 1-2 character tokens
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords, nil and true/false
    AND, CLASS, CONST, ELSE, FALSE, FUN, FOR, IF, IMPORT,
    NIL, OR, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    // Types
    NUM, BOOL, STR,

    // Lonely
    EOF
}