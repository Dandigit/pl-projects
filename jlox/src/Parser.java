package com.dandigit.jlox;

import java.util.ArrayList;
import java.util.List;

import static com.dandigit.jlox.TokenType.*;

class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    /* DECLARATIONS: need to be global or in a block. */
    private Stmt declaration() {
        try {
            // Look for declaration keywords
            if (match(VAR)) return varDeclaration();

            // Otherwise, look for a statement
            return statement();
        } catch (ParseError error) {
            synchronise();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expected a variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expected ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    /* STATEMENTS: don't need to be global or in a block. */
    private Stmt statement() {
        if (match(PRINT)) return printStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }

    // RULE: printStmt → "print" expression ";";
    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expected ';' after value.");
        return new Stmt.Print(value);
    }

    // RULE: exprStmt → expression ";";
    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expected ';' after expression.");
        return new Stmt.Expression(expr);
    }

    // RULE: "{" declaration* "}";
    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expected '}' after block.");
        return statements;
    }

    /* expression() is simply an alias  *
     * for the lowest precedence level. */

    // RULE: expression → comma;
    private Expr expression() {
        return comma();
    }

    /* comma() has the lowest precedence  *
     * of any rule, and has left to right *
     * associativity.                     */

    // RULE: comma → equality ( "," equality )*;
    private Expr comma() {
        // Grab either the left operand of an comma expression
        // or an expression with higher precedence than an comma expression
        Expr expr = assignment();

        // Is a comma token matched?
        while (match(COMMA)) {
            // Grab the token matched for the operator
            Token operator = previous();
            // Get the right operand
            Expr right = assignment();
            // Combine the operator and operands into a new AST node
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr assignment() {
        // Get the first expression
        Expr expr = ternary();

        // Is there an assignment operator?
        if (match(EQUAL)) {
            // We're parsing an assignment expression
            Token equals = previous();
            Expr value = assignment(); // Right recursion

            // Is the first expression a variable?
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            // Otherwise, we must be assigning to an invalid target.
            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    /* ternary() is the only ternary operator in *
     * Lox, and has right to left associativity. */

    // RULE: ternary → (equality "?" equality ":")* equality ;
    private Expr ternary() {
        // Get the left expression
        Expr expr = equality();

        // Is the '?' operator found?
        if (match(QUESTION)) {
            // If so, we must be parsing a ternary expression.

            // Get the token matched to the left operator
            Token leftOper = previous();
            // Get the middle expression
            Expr middle = equality();
            if (match(COLON)) {
                // Get the token matched to the right operator
                Token rightOper = previous();
                // Get the right expression using right recursion
                Expr right = ternary(); // Right recursion
                // Put all of the data into an AST node
                expr = new Expr.Ternary(expr, leftOper, middle, rightOper, right);
            } else {
                // If we didn't match a ':' after the '?', we know that
                // something's up. Our ternary expression is invalid as
                // it does not have valid operators or enough expressions.

                // We'll throw an error here.
                throw error(peek(), "Expected ':' after ternary operator '?'.");

            }
        }

        return expr;
    }

    /* equality() steps up to comparison,    *
     *  and has left to right associativity. */

    // RULE: equality → comparison ( ( "!=" | "==" ) comparison )*;
    private Expr equality() {
        // Grab either the left operand of an equality expression
        // or an expression with higher precedence than an equality expression
        Expr expr = comparison();

        // Is an equality operator found?
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            // If so, we know that we're parsing an equality expression

            // Grab the token matched for the operator
            Token operator = previous();
            // Step up to comparison() to get the right operand
            Expr right = comparison();
            // Combine the operator and operands into a new AST node
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /* comparison() is reached if we don't match *
     * an equality expression, and steps up the  *
     * ladder to addition() if we don't match a  *
     * comparison expression. It's virtually     *
     * identical to equality() only we're        *
     * matching precedence at one level higher.  */

    // RULE: comparison → addition ( ( ">" | ">=" | "<" | "<=" ) addition )*;
    private Expr comparison() {
        Expr expr = addition();

        // Is a comparison operator found?
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL))
        {
            // If so, we know that we're parsing a comparison expression

            // Grab the token matched for the operator
            Token operator = previous();
            // Get the left operand, with addition() or higher precedence
            Expr right = addition();
            // Combine the operator and operands into a new AST node
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /* addition() is again almost identical to   *
     * equality() and friends. Remember that     *
     * we're not just matching the '+' operator, *
     * but also the '-' operator as it has the   *
     * same precedence level.                    */

    // RULE: addition → multiplication ( ( "-" | "+" ) multiplication )*;
    private Expr addition() {
        Expr expr = multiplication();

        // Is an addition/subtraction operator found?
        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }


    /* This time, we're not stepping up to another *
     * binary operator, rather the unary() level   *
     * of precedence. Remember '*' and '/' share   *
     * the multiplication precedence level.        */

    // RULE: multiplication → unary ( ( "/" | "*" ) unary )*;
    private Expr multiplication() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /* The unary operators are a bit different, as  *
     * we're using right recursion to get the right *
     * operand. This is because the next precedence *
     * level is the highest, primary().             */

    // RULE: unary → ( "!" | "-" ) unary
    //      | primary ;
    private Expr unary() {
        // We're parsing a unary expression if we find a unary operator
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary(); // Right recursion
            return new Expr.Unary(operator, right);
        }

        // Otherwise, we must have reached the highest level of precedence.
        return primary();
    }

    /* Most of the cases for primary() are terminal  *
     * expressions, with the exception of groupings, *
     * which makes this pretty trivial.              */

    // RULE: primary → NUMBER | STRING | "false" | "true" | "nil"
    //        | "(" expression ")" ;
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        // Varying values (number and string literals)
        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        // Groupings
        if (match(LEFT_PAREN)) {
            // Get the expression inside the brackets, starting all the way down
            // at the bottom.
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expected ')' after expression");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expected expression.");
    }

    // Checks if the current token is any of the given types
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance(); // Consume the matched token
                return true; // Matched a token
            }
        }

        return false; // Failed to match a token
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronise() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

}
