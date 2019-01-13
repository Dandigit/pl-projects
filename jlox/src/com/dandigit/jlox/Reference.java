package com.dandigit.jlox;

abstract class Reference {
    abstract Expr drf();
    abstract Token name();

    static class Variable extends Reference {
        private Expr.Variable expr;

        Variable(Expr.Variable expr) {
            this.expr = expr;
        }

        @Override
        Token name() {
            return this.expr.name;
        }

        @Override
        Expr drf() {
            return this.expr;
        }
    }

    static class Property extends Reference {
        private Expr.Get expr;

        Property(Expr.Get expr) {
            this.expr = expr;
        }

        @Override
        Token name() {
            return this.expr.name;
        }

        @Override
        Expr drf() {
            return this.expr;
        }
    }

    static class Element extends Reference {
        private Expr.Subscript expr;

        Element(Expr.Subscript expr) {
            this.expr = expr;
        }

        @Override
        Token name() {
            return this.expr.name;
        }

        @Override
        Expr drf() {
            return this.expr;
        }
    }
}
