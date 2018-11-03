package com.dandigit.jlox;

import java.util.ArrayList;
import java.util.Arrays;
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
            if (match(CLASS)) return classDeclaration();

            if (check(FUN) && checkNext(IDENTIFIER)) {
                consume(FUN, null);
                return function("function");
            }

            if (match(VAR)) return varDeclaration();

            // Otherwise, look for a statement
            return statement();
        } catch (ParseError error) {
            synchronise();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(IDENTIFIER, "Expected class name.");

        Expr.Variable superclass = null;
        if (match(LESS)) {
            consume(IDENTIFIER, "Expected superclass name.");
            superclass = new Expr.Variable(previous());
        }

        consume(LEFT_BRACE, "Expected '{' before class body.");

        List<Stmt.Function> methods = new ArrayList<>();
        List<Stmt.Function> classMethods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            boolean isClassMethod = match(CLASS);
            (isClassMethod ? classMethods : methods).add(function("method"));
        }

        consume(RIGHT_BRACE, "Expected '}' after class body.");

        return new Stmt.Class(name, superclass, methods, classMethods);
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

    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expected " + kind + " name.");
        return new Stmt.Function(name, functionBody(kind));
    }

    private Expr.Function functionBody(String kind) {
        List<Token> paramaters = null;

        // Allow omitting the parameter list entirely in method getters.
        // A parameterless function will have an empty list of parameters,
        // whereas a getter will have a null list of parameters. Cute right?
        if (!kind.equals("method") || check(LEFT_PAREN)) {
            consume(LEFT_PAREN, "Expected '(' after " + kind + "name.");
            paramaters = new ArrayList<>();
            if (!check(RIGHT_PAREN)) {
                do {
                    if (paramaters.size() >= 8) {
                        error(peek(), "Cannot have more that 8 paramaters.");
                    }

                    paramaters.add(consume(IDENTIFIER, "Expected paramater name."));
                } while (match(COMMA));
            }
            consume(RIGHT_PAREN, "Expected ')' after parameters.");
        }

        consume(LEFT_BRACE, "Expected '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Expr.Function(paramaters, body);
    }

    /* STATEMENTS: don't need to be global or in a block. */
    private Stmt statement() {
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(RETURN)) return returnStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'for'.");

        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expected ';' after for loop condition.");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expected ')' after for clauses.");

        Stmt body = statement();

        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(
                    body,
                    new Stmt.Expression(increment)
            ));
        }

        if (condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);

        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;

        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expected ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after condition.");
        Stmt body = statement();

        return new Stmt.While(condition, body);
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

    // Covers both Assign and Set expressions
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
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get)expr;
                return new Expr.Set(get.object, get.name, value);
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
        Expr expr = or();

        // Is the '?' operator found?
        if (match(QUESTION)) {
            // If so, we must be parsing a ternary expression.

            // Get the token matched to the left operator
            Token leftOper = previous();
            // Get the middle expression
            Expr middle = or();
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

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
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

    private Expr multiplication() {
        //Expr expr = cast();
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            //Expr right = cast();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /*
    private Expr cast() {
        Expr expr = unary();

        while (match(AS)) {
            Token operator = previous();
            Token right = advance();
            expr = new Expr.Call(
                    new Expr.Variable(
                          new Token(
                                  IDENTIFIER,
                                  "cast" + right.lexeme,
                                  null,
                                  operator.line
                          )
                    ),
                    operator,
                    Arrays.asList(expr)
            );
        }

        return expr;
    }
    */

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
        return call();
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 8) {
                    error(peek(), "Cannot have more than 8 arguments.");
                }

                // Calls assignment() rather than expression()
                // to avoid the comma operator, essentially
                // "overloading" it.
                arguments.add(assignment());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Expected ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER,
                        "Expected property name after '.'.");
                expr = new Expr.Get(expr, name);
            } else if (match(LEFT_SQUARE)) {
                Expr index = primary();
                Token closeBracket = consume(RIGHT_SQUARE,
                        "Expected ']' after subscript index.");
                expr = new Expr.Subscript(expr, closeBracket, index);
            } else {
                break;
            }
        }

        return expr;
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

        if (match(FUN)) return functionBody("function");

        if (match(SUPER)) {
            Token keyword = previous();
            consume(DOT, "Expected '.' after 'super'.");
            Token method = consume(IDENTIFIER,
                    "Expected superclass method name.");
            return new Expr.Super(keyword, method);
        }

        if (match(THIS)) return new Expr.This(previous());

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        // Array literals
        if (match(LEFT_SQUARE)) {
            List<Expr> values = new ArrayList<>();
            if (match(RIGHT_SQUARE)) {
                return new Expr.Array(null);
            }
            while (!match(RIGHT_SQUARE)) {
                Expr value = assignment();
                values.add(value);
                if (peek().type != RIGHT_SQUARE) {
                    consume(COMMA,
                            "Expected comma before next expression.");
                }
            }
            return new Expr.Array(values);
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

    private boolean checkNext(TokenType type) {
        if (isAtEnd()) return false;
        if (tokens.get(current + 1).type == EOF) return false;
        return tokens.get(current + 1).type == type;
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
                case RETURN:
                    return;
            }

            advance();
        }
    }

}
