package com.dandigit.jlox;

import java.util.List;

abstract class Expr {
    interface Visitor<R> {
        R visitAllotExpr(Allot expr);
        R visitArrayExpr(Array expr);
        R visitAssignExpr(Assign expr);
        R visitTernaryExpr(Ternary expr);
        R visitBinaryExpr(Binary expr);
        R visitCallExpr(Call expr);
        R visitFunctionExpr(Function expr);
        R visitGetExpr(Get expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitLogicalExpr(Logical expr);
        R visitReferenceExpr(Reference expr);
        R visitSetExpr(Set expr);
        R visitSubscriptExpr(Subscript expr);
        R visitSuperExpr(Super expr);
        R visitThisExpr(This expr);
        R visitUnaryExpr(Unary expr);
        R visitVariableExpr(Variable expr);
    }
    static class Allot extends Expr {
        Allot(Expr object, Token name, Expr value) {
            this.object = object;
            this.name = name;
            this.value = value;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAllotExpr(this);
        }
        final Expr object;
        final Token name;
        final Expr value;
    }
    static class Array extends Expr {
        Array(List<Expr> values) {
            this.values = values;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayExpr(this);
        }
        final List<Expr> values;
    }
    static class Assign extends Expr {
        Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
        final Token name;
        final Expr value;
    }
    static class Ternary extends Expr {
        Ternary(Expr left, Token leftOper, Expr middle, Token rightOper, Expr right) {
            this.left = left;
            this.leftOper = leftOper;
            this.middle = middle;
            this.rightOper = rightOper;
            this.right = right;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitTernaryExpr(this);
        }
        final Expr left;
        final Token leftOper;
        final Expr middle;
        final Token rightOper;
        final Expr right;
    }
    static class Binary extends Expr {
        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
        final Expr left;
        final Token operator;
        final Expr right;
    }
    static class Call extends Expr {
        Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
        final Expr callee;
        final Token paren;
        final List<Expr> arguments;
    }
    static class Function extends Expr {
        Function(List<Token> params, List<Stmt> body) {
            this.params = params;
            this.body = body;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionExpr(this);
        }
        final List<Token> params;
        final List<Stmt> body;
    }
    static class Get extends Expr {
        Get(Expr object, Token name) {
            this.object = object;
            this.name = name;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
        final Expr object;
        final Token name;
    }
    static class Grouping extends Expr {
        Grouping(Expr expression) {
            this.expression = expression;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
        final Expr expression;
    }
    static class Literal extends Expr {
        Literal(Object value) {
            this.value = value;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
        final Object value;
    }
    static class Logical extends Expr {
        Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
        final Expr left;
        final Token operator;
        final Expr right;
    }
    static class Reference extends Expr {
        Reference(Token operator, Expr value) {
            this.operator = operator;
            this.value = value;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitReferenceExpr(this);
        }
        final Token operator;
        final Expr value;
    }
    static class Set extends Expr {
        Set(Expr object, Token name, Expr value) {
            this.object = object;
            this.name = name;
            this.value = value;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
        final Expr object;
        final Token name;
        final Expr value;
    }
    static class Subscript extends Expr {
        Subscript(Expr object, Token name, Expr index) {
            this.object = object;
            this.name = name;
            this.index = index;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSubscriptExpr(this);
        }
        final Expr object;
        final Token name;
        final Expr index;
    }
    static class Super extends Expr {
        Super(Token keyword, Token method) {
            this.keyword = keyword;
            this.method = method;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSuperExpr(this);
        }
        final Token keyword;
        final Token method;
    }
    static class This extends Expr {
        This(Token keyword) {
            this.keyword = keyword;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitThisExpr(this);
        }
        final Token keyword;
    }
    static class Unary extends Expr {
        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
        final Token operator;
        final Expr right;
    }
    static class Variable extends Expr {
        Variable(Token name) {
            this.name = name;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
        final Token name;
    }

    abstract <R> R accept(Visitor<R> visitor);
}
