package com.dandigit.jlox;

enum TokenType {
    // Single character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,
    COLON, QUESTION,

    // 1-2 character tokens
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords, nil and true/false
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL,
    OR, RETURN, SUPER, THIS, TRUE, VAR, WHILE, AS,

    // Types
    NUM, BOOL, STR,

    // Lonely
    EOF
}