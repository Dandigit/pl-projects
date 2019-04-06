#include <stdio.h>
#include <stdlib.h>

#include "../h/common.h"
#include "../h/compiler.h"
#include "../h/scanner.h"

#ifdef DEBUG_PRINT_CODE
#include "../h/debug.h"
#endif

Parser parser;

Chunk *compilingChunk;

static Chunk* currentChunk() {
    return compilingChunk;
}

static void errorAt(Token *token, const char *message) {
    if (parser.panicMode) return;
    parser.panicMode = true;

    fprintf(stderr, "[line %d] Error", token->line);

    if (token->type == TOKEN_EOF) {
        fprintf(stderr, " at end");
    } else if (token->type == TOKEN_ERROR) {
        // Nothing.
    } else {
        fprintf(stderr, " at '%.*s'", token->length, token->start);
    }

    fprintf(stderr, ": %s\n", message);
    parser.hadError = true;
}

static void error(const char *message) {
    errorAt(&parser.previous, message);
}

static void errorAtCurrent(const char *message) {
    errorAt(&parser.current, message);
}

static void advance() {
    parser.previous = parser.current;

    while (true) {
        parser.current = scanToken();
        if (parser.current.type != TOKEN_ERROR) break;

        errorAtCurrent(parser.current.start);
    }
}

static void consume(TokenType type, const char *message) {
    if (parser.current.type == type) {
        advance();
        return;
    }

    errorAtCurrent(message);
}

static bool check(TokenType type) {
    return parser.current.type == type;
}

static bool match(TokenType type) {
    if (!check(type)) return false;
    advance();
    return true;
}

static void emitByte(uint8_t byte) {
    writeChunk(currentChunk(), byte, parser.previous.line);
}

static void emitBytes(uint8_t byte1, uint8_t byte2) {
    emitByte(byte1);
    emitByte(byte2);
}

static void emitLong(int index) {
    emitByte((uint8_t)(index & 0xff));
    emitByte((uint8_t)((index >> 8) & 0xff));
    emitByte((uint8_t)((index >> 16) & 0xff));
}

static void emitConstant(Value value) {
    writeConstant(currentChunk(), value, parser.previous.line);
}

static void emitReturn() {
    emitByte(OP_RETURN);
}

static void endCompiler() {
    emitReturn();
#ifdef DEBUG_PRINT_CODE
    if (!parser.hadError) {
        disassembleChunk(currentChunk(), "code");
    }
#endif
}

static void expression();
static ParseRule* getRule(TokenType type);
static void parsePrecedence(Precedence precedence);

static int identifierConstant(Token* name) {
    return addConstant(compilingChunk,
                       OBJ_VAL(copyString(name->start, name->length)));
}

static void binary(bool canAssign) {
    // Remember the operator.
    TokenType operatorType = parser.previous.type;

    // Compile the right operand.
    ParseRule *rule = getRule(operatorType);
    parsePrecedence((Precedence)(rule->precedence + 1));

    switch (operatorType) {
        case TOKEN_BANG_EQUAL: emitBytes(OP_EQUAL, OP_NOT); break;
        case TOKEN_EQUAL_EQUAL: emitByte(OP_EQUAL); break;
        case TOKEN_GREATER: emitByte(OP_GREATER); break;
        case TOKEN_GREATER_EQUAL: emitBytes(OP_LESS, OP_NOT); break;
        case TOKEN_LESS: emitByte(OP_LESS); break;
        case TOKEN_LESS_EQUAL: emitBytes(OP_GREATER, OP_NOT); break;
        case TOKEN_PLUS: emitByte(OP_ADD); break;
        case TOKEN_MINUS: emitByte(OP_SUBTRACT); break;
        case TOKEN_STAR: emitByte(OP_MULTIPLY); break;
        case TOKEN_SLASH: emitByte(OP_DIVIDE); break;
        default: return; // Unreachable.
    }
}

static void literal(bool canAssign) {
    switch (parser.previous.type) {
        case TOKEN_FALSE: emitByte(OP_FALSE); break;
        case TOKEN_NIL: emitByte(OP_NIL); break;
        case TOKEN_TRUE: emitByte(OP_TRUE); break;
        default:
            return; // Unreachable.
    }
}

static void grouping(bool canAssign) {
    expression();
    consume(TOKEN_RIGHT_PAREN, "Expect ')' after expression.");
}

static void number(bool canAssign) {
    double value = strtod(parser.previous.start, NULL);
    emitConstant(NUMBER_VAL(value));
}

static void string(bool canAssign) {
    emitConstant(OBJ_VAL(copyString(parser.previous.start + 1,
            parser.previous.length - 2)));
}

static void namedVariable(Token name, bool canAssign) {
    int nameIndex = identifierConstant(&name);

    if (canAssign && match(TOKEN_EQUAL)) {
        // Parse an assignment.
        expression();

        if (nameIndex < 256) {
            emitBytes(OP_SET_GLOBAL, (uint8_t)nameIndex);
        } else {
            emitByte(OP_SET_GLOBAL_LONG);
            emitLong(nameIndex);
        }
    } else {
        if (nameIndex < 256) {
            emitBytes(OP_GET_GLOBAL, (uint8_t)nameIndex);
        } else {
            emitByte(OP_GET_GLOBAL_LONG);
            emitLong(nameIndex);
        }
    }
}

static void variable(bool canAssign) {
    namedVariable(parser.previous, canAssign);
}

static void unary(bool canAssign) {
    TokenType operatorType = parser.previous.type;

    // Compile the operand.
    parsePrecedence(PREC_UNARY);

    // Emit the operator instruction.
    switch (operatorType) {
        case TOKEN_BANG: emitByte(OP_NOT); break;
        case TOKEN_MINUS: emitByte(OP_NEGATE); break;
        default: return; // Unreachable.
    }
}

static void ternary() {
    parsePrecedence(PREC_CONDITIONAL);
    consume(TOKEN_COLON, "Expected ':' after then branch of conditional expression.");
    parsePrecedence(PREC_ASSIGNMENT);
    emitByte(OP_CONDITIONAL);
}

ParseRule rules[] = {
        { grouping, NULL,    PREC_CALL },        // TOKEN_LEFT_PAREN
        { NULL,     NULL,    PREC_NONE },        // TOKEN_RIGHT_PAREN
        { NULL,     NULL,    PREC_NONE },        // TOKEN_LEFT_BRACE
        { NULL,     NULL,    PREC_NONE },        // TOKEN_RIGHT_BRACE
        { NULL,     NULL,    PREC_NONE },        // TOKEN_COMMA
        { NULL,     NULL,    PREC_NONE },        // TOKEN_COLON
        { NULL,     NULL,    PREC_CALL },        // TOKEN_DOT
        { unary,    binary,  PREC_TERM },        // TOKEN_MINUS
        { NULL,     binary,  PREC_TERM },        // TOKEN_PLUS
        { NULL,     ternary, PREC_CONDITIONAL }, // TOKEN_QUESTION
        { NULL,     NULL,    PREC_NONE },        // TOKEN_SEMICOLON
        { NULL,     binary,  PREC_FACTOR },      // TOKEN_SLASH
        { NULL,     binary,  PREC_FACTOR },      // TOKEN_STAR
        { unary,    NULL,    PREC_NONE },        // TOKEN_BANG
        { NULL,     binary,  PREC_EQUALITY },    // TOKEN_BANG_EQUAL
        { NULL,     NULL,    PREC_NONE },        // TOKEN_EQUAL
        { NULL,     binary,  PREC_EQUALITY },    // TOKEN_EQUAL_EQUAL
        { NULL,     binary,  PREC_COMPARISON },  // TOKEN_GREATER
        { NULL,     binary,  PREC_COMPARISON },  // TOKEN_GREATER_EQUAL
        { NULL,     binary,  PREC_COMPARISON },  // TOKEN_LESS
        { NULL,     binary,  PREC_COMPARISON },  // TOKEN_LESS_EQUAL
        { variable, NULL,    PREC_NONE },        // TOKEN_IDENTIFIER
        { string,   NULL,    PREC_NONE },        // TOKEN_STRING
        { number,   NULL,    PREC_NONE },        // TOKEN_NUMBER
        { NULL,     NULL,    PREC_AND },         // TOKEN_AND
        { NULL,     NULL,    PREC_NONE },        // TOKEN_CLASS
        { NULL,     NULL,    PREC_NONE },        // TOKEN_ELSE
        { literal,  NULL,    PREC_NONE },        // TOKEN_FALSE
        { NULL,     NULL,    PREC_NONE },        // TOKEN_FUN
        { NULL,     NULL,    PREC_NONE },        // TOKEN_FOR
        { NULL,     NULL,    PREC_NONE },        // TOKEN_IF
        { literal,  NULL,    PREC_NONE },        // TOKEN_NIL
        { NULL,     NULL,    PREC_OR },          // TOKEN_OR
        { NULL,     NULL,    PREC_NONE },        // TOKEN_PRINT
        { NULL,     NULL,    PREC_NONE },        // TOKEN_RETURN
        { NULL,     NULL,    PREC_NONE },        // TOKEN_SUPER
        { NULL,     NULL,    PREC_NONE },        // TOKEN_THIS
        { literal,  NULL,    PREC_NONE },        // TOKEN_TRUE
        { NULL,     NULL,    PREC_NONE },        // TOKEN_VAR
        { NULL,     NULL,    PREC_NONE },        // TOKEN_WHILE
        { NULL,     NULL,    PREC_NONE },        // TOKEN_ERROR
        { NULL,     NULL,    PREC_NONE },        // TOKEN_EOF
};

static void parsePrecedence(Precedence precedence) {
    advance();
    ParseFn prefixRule = getRule(parser.previous.type)->prefix;
    if (prefixRule == NULL) {
        error("Expected expression.");
        return;
    }

    bool canAssign = precedence <= PREC_ASSIGNMENT;
    prefixRule(canAssign);

    while (precedence <= getRule(parser.current.type)->precedence) {
        advance();
        ParseFn infixRule = getRule(parser.previous.type)->infix;
        infixRule(canAssign);
    }

    // Don't silently ignore a rogue '='.
    if (canAssign && match(TOKEN_EQUAL)) {
        error("Invalid assignment target.");
        expression();
    }
}

static int parseVariable(const char* errorMessage) {
    consume(TOKEN_IDENTIFIER, errorMessage);
    return identifierConstant(&parser.previous);
}

static void defineVariable(int global) {
    if (global < 256) {
        emitBytes(OP_DEFINE_GLOBAL, global);
    } else {
        emitByte(OP_DEFINE_GLOBAL_LONG);
        emitLong(global);
    }
}

static ParseRule* getRule(TokenType type) {
    return &rules[type];
}

static void expression() {
    parsePrecedence(PREC_ASSIGNMENT);
}

static void declaration();
static void statement();

static void varDeclaration() {
    int global = parseVariable("Expected variable name.");

    // Compile the initializer.
    if (match(TOKEN_EQUAL)) {
        expression();
    } else {
        emitByte(OP_NIL);
    }

    consume(TOKEN_SEMICOLON, "Expected ';' after variable declaration.");

    defineVariable(global);
}

static void expressionStatement() {
    expression();
    emitByte(OP_POP);
    consume(TOKEN_SEMICOLON, "Expected ';' after expression.");
}


static void printStatement() {
    expression();
    emitByte(OP_PRINT);
    consume(TOKEN_SEMICOLON, "Expected ';' after value.");
}

static void synchronise() {
    parser.panicMode = false;

    // Look for a statement boundary
    while (parser.current.type != TOKEN_EOF) {
        if (parser.previous.type == TOKEN_SEMICOLON) return;

        switch (parser.current.type) {
            case TOKEN_CLASS:
            case TOKEN_FUN:
            case TOKEN_VAR:
            case TOKEN_FOR:
            case TOKEN_IF:
            case TOKEN_WHILE:
            case TOKEN_PRINT:
            case TOKEN_RETURN:
                return;

            default:
                // Do nothing.
                ;
        }

        advance();
    }
}

static void declaration() {
    if (match(TOKEN_VAR)) {
        varDeclaration();
    } else {
        statement();
    }

    if (parser.panicMode) synchronise();
}

static void statement() {
    if (match(TOKEN_PRINT)) {
        printStatement();
    } else {
        expressionStatement();
    }
}

bool compile(const char *source, Chunk *chunk) {
    initScanner(source);

    compilingChunk = chunk;
    parser.hadError = false;
    parser.panicMode = false;

    advance();

    while (!match(TOKEN_EOF)) {
        declaration();
    }

    endCompiler();

    // compile() returns false if there was an error.
    return !parser.hadError;
}