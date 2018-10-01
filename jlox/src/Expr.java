package com.dandigit.jlox;

import java.util.List;

abstract class Expr {
    interface Visitor<R> {
        R visitTernaryExpr(Ternary expr);
        R visitBinaryExpr(Binary expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitUnaryExpr(Unary expr);
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

    abstract <R> R accept(Visitor<R> visitor);
}
