package com.dandigit.jlox;

import java.sql.Ref;
import java.util.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();

    Interpreter() {
        globals.define("print", new LoxCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                System.out.println(stringify(arguments.get(0)));
                return null;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("put", new LoxCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                System.out.print(stringify(arguments.get(0)));
                return null;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("input", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    return br.readLine();
                } catch (IOException exc) {
                    return null;
                }
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("str", new LoxCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                return stringify(arguments.get(0));
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("num", new LoxCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                return Double.parseDouble(stringify(arguments.get(0)));
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("round", new LoxCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                return ((Integer)((Double)arguments.get(0)).intValue()).doubleValue();
        }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("readFile", new LoxCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                String contents;

                try {
                    // File path is 1st argument
                    BufferedReader br = new BufferedReader(new FileReader(stringify(arguments.get(0))));
                    String currentLine;
                    contents = "";
                    while ((currentLine = br.readLine()) != null) {
                        contents += currentLine + "\n";
                    }
                } catch (IOException exception) {
                    return null;
                }

                return contents;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("writeFile", new LoxCallable() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                try {
                    // File path is 1st argument
                    BufferedWriter bw = new BufferedWriter(new FileWriter(stringify(arguments.get(0))));
                    // Data is 2nd argument
                    bw.write(stringify(arguments.get(1)));

                    bw.close();
                    return true;
                } catch (IOException exception) {
                    return false;
                }
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("appendFile", new LoxCallable() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                try {
                    // File path is 1st argument
                    BufferedWriter bw = new BufferedWriter(new FileWriter(stringify(arguments.get(0)), true));
                    // Data is 2nd argument
                    bw.append(stringify(arguments.get(1)));

                    bw.close();
                    return true;
                } catch (IOException exception) {
                    return false;
                }
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("len", new LoxCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Object object = arguments.get(0);

                if (object instanceof String) {
                    return (double) ((String) object).length();
                }

                if (object instanceof List) {
                    return (double) ((List) object).size();
                }

                return null;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("argv", Lox.argv);

    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitAllotExpr(Expr.Allot expr) {
        Expr.Subscript subscript = null;
        if (expr.object instanceof Expr.Subscript) {
            subscript = (Expr.Subscript)expr.object;
        }

        Object listObject = evaluate(subscript.object);
        if (!(listObject instanceof List)) {
            throw new RuntimeError(expr.name,
                    "Only arrays can be subscripted.");
        }

        List<Object> list = (List)listObject;

        Object indexObject = evaluate(subscript.index);
        if (!(indexObject instanceof Double)) {
            throw new RuntimeError(expr.name,
                    "Only numbers can be used as an array index.");
        }

        int index = ((Double) indexObject).intValue();
        if (index >= list.size()) {
            throw new RuntimeError(expr.name,
                    "Array index out of range.");
        }

        Object value = evaluate(expr.value);

        list.set(index, value);
        return value;
    }

    @Override
    public Object visitArrayExpr(Expr.Array expr) {
        List<Object> values = new ArrayList<>();
        if (expr.values != null) {
            for (Expr value : expr.values) {
                values.add(evaluate(value));
            }
        }
        return values;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        Integer distance = locals.get(expr);

        Object target = (distance != null ? environment.getAt(distance, expr.name.lexeme) : globals.get(expr.name));

        if (target instanceof Reference) {
            assignReference((Reference)target, expr.value, distance);
        } else {
            assign(expr.name, value, distance);
        }

        return value;
    }

    private void assignReference(Reference target, Expr value, Integer distance) {
        if (target instanceof Reference.Variable) {
            assign(target.name(), evaluate(value), distance);
        } else if (target instanceof Reference.Property) {
            Expr.Get expr = (Expr.Get)target.drf();
            evaluate(new Expr.Set(expr.object, expr.name, value));
        } else if (target instanceof Reference.Element) {
            Expr.Subscript expr = (Expr.Subscript)target.drf();
            evaluate(new Expr.Allot(expr.object, expr.name, expr.index));
        }
    }

    private void assign(Token name, Object value, Integer distance) {
        if (distance != null) {
            environment.assignAt(distance, name, value);
        } else {
            globals.assign(name, value);
        }
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitReferenceExpr(Expr.Reference expr) {
        if (expr.value instanceof Expr.Variable) {
            return new Reference.Variable((Expr.Variable) expr.value);
        }
        if (expr.value instanceof Expr.Get) {
            return new Reference.Property((Expr.Get) expr.value);
        }
        if (expr.value instanceof Expr.Subscript) {
            return new Reference.Element((Expr.Subscript) expr.value);
        }

        throw new RuntimeError(expr.operator, "Internal: Could not reference expression at runtime.");
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);

        if (!(object instanceof LoxInstance)) {
            throw new RuntimeError(expr.name, "Only instances have fields.");
        }

        Object value = evaluate(expr.value);
        ((LoxInstance)object).set(expr.name, value);
        return value;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        int distance = locals.get(expr);
        LoxClass superclass = (LoxClass)environment.getAt(distance, "super");

        // "this" is always one level nearer than "super"'s environment
        LoxInstance object = (LoxInstance)environment.getAt(distance - 1,
                "this");

        LoxFunction method = superclass.findMethod(
                object, expr.method.lexeme);

        if (method == null) {
            throw new RuntimeError(expr.method,
                    "Undefined property '" + expr.method.lexeme + "'.");
        }

        return method;
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVariable(expr.keyword, expr);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
        }

        // Unreachable
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.name, expr);
    }

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case COMMA:
                return right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case MINUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left - (double)right;
                }

                if (left instanceof List && right instanceof Double) {
                    List list = (List)left;
                    List<Object> newList = new ArrayList<>();
                    int newSize = list.size() - ((Double) right).intValue();

                    if (newSize < 0) {
                        throw new RuntimeError(expr.operator,
                                "Cannot remove " + ((Double) right).intValue() + " elements from an array of size " +
                                        list.size() + ".");
                    }

                    for (int index = 0; index < newSize; ++index) {
                        newList.add(list.get(index));
                    }

                    return newList;
                }

                throw new RuntimeError(expr.operator,
                        "Invalid operands to binary operator '-'.");
            // PLUS is a bit different as we can add numbers and strings
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }

                if (left instanceof List) {
                    List list = (List)left;
                    list.add(right);
                    return left;
                }

                throw new RuntimeError(expr.operator,
                        "Invalid operands to binary operator '+'.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                // Check for division by 0
                if ((double)right == 0) throw new RuntimeError(expr.operator,
                        "Cannot divide by zero.");

                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case PERCENTAGE:
                checkNumberOperands(expr.operator, left, right);
                return (double)left % (double)right;
        }

        // Unreachable
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren,
                    "Only functions and classes are callable.");
        }

        LoxCallable function = (LoxCallable)callee;

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " +
                    function.arity() + " arguments but got " +
                    arguments.size() + "."
            );
        }

        return function.call(this, arguments);
    }

    @Override
    public Object visitFunctionExpr(Expr.Function expr) {
        return new LoxFunction(null, expr, environment, false);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof LoxInstance) {
            Object result = ((LoxInstance) object).get(expr.name);

            if (result instanceof LoxFunction &&
                    ((LoxFunction) result).isGetter()) {
                result = ((LoxFunction) result).call(this, null);
            }

            return result;
        }
        if (object instanceof NativeInstance) {
            Object result = ((NativeInstance) object).findMethod(expr.name.lexeme);
            return result;
        }

        throw new RuntimeError(expr.name,
                "Only instances have properties.");
    }

    @Override
    public Object visitSubscriptExpr(Expr.Subscript expr) {
        Object listObject = evaluate(expr.object);
        if (!(listObject instanceof List)) {
            throw new RuntimeError(expr.name,
                    "Only arrays can be subscripted.");
        }

        List list = (List)listObject;

        Object indexObject = evaluate(expr.index);
        if (!(indexObject instanceof Double)) {
            throw new RuntimeError(expr.name,
                    "Only numbers can be used as an array index.");
        }

        int index = ((Double) indexObject).intValue();
        if (index >= list.size()) {
            throw new RuntimeError(expr.name,
                    "Array index out of range.");
        }

        return list.get(index);
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        Object left = evaluate(expr.left);
        Object middle = evaluate(expr.middle);
        Object right = evaluate(expr.right);

        // Conditional expression
        if (expr.leftOper.type == TokenType.QUESTION &&
                expr.rightOper.type == TokenType.COLON) {
            if (isTruthy(left)) {
                return middle;
            }

            return right;
        }

        // Unreachable
        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt.name.lexeme, stmt.function, environment, false);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitImportStmt(Stmt.Import stmt) {
        Object module = evaluate(stmt.module);
        if (!(module instanceof String)) {
            throw new RuntimeError(stmt.keyword,
                    "Module name must be a string.");
        }

        String moduleName = (String)module;

        if (moduleName.startsWith("std:")) {
            String library = moduleName.split(":")[1];
            if (library.equals("File")) {
                Lox.run(StandardLibrary.File);
            } else if (library.equals("Random")) {
                Lox.run(StandardLibrary.Random);
            } else if (library.equals("Bitwise")) {
                globals.define("Bitwise", StandardLibrary.Bitwise);
            } else if (library.equals("all")) {
                StandardLibrary.importAll(globals);
            } else {
                throw new RuntimeError(stmt.keyword,
                        "'" + moduleName + "' is not a standard library module.");
            }

            return null;
        }

        String source = "";

        try {
            BufferedReader br = new BufferedReader(new FileReader((String)module));
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                source += currentLine + "\n";
            }
        } catch (IOException exception) {
            throw new RuntimeError(stmt.keyword,
                    "Could not import module '" + module + "'.");
        }

        Lox.run(source);

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluateWithoutDeref(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        Object superclass = null;
        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass);
            if (!(superclass instanceof LoxClass)) {
                throw new RuntimeError(stmt.superclass.name,
                        "Superclass must be a class.");
            }
        }

        environment.define(stmt.name.lexeme, null);

        if (stmt.superclass != null) {
            environment = new Environment(environment);
            environment.define("super", superclass);
        }

        Map<String, LoxFunction> classMethods = new HashMap<>();
        for (Stmt.Function method : stmt.classMethods) {
            LoxFunction function = new LoxFunction(method.name.lexeme,
                    method.function, environment, false);
            classMethods.put(method.name.lexeme, function);
        }

        LoxClass metaclass = new LoxClass(null,
                stmt.name.lexeme + " metaclass", (LoxClass)superclass, classMethods);

        Map<String, LoxFunction> methods = new HashMap<>();
        for (Stmt.Function method : stmt.methods) {
            LoxFunction function = new LoxFunction(method.name.lexeme,
                    method.function, environment, method.name.lexeme.equals("init"));
            methods.put(method.name.lexeme, function);
        }

        LoxClass klass = new LoxClass(metaclass, stmt.name.lexeme,
                (LoxClass)superclass, methods);

        if (superclass != null) {
            environment = environment.enclosing;
        }

        environment.assign(stmt.name, klass);
        return null;
    }

    public void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    public void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        // nil is only equal to nil.
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof List) {
            String text = "[";
            List<Object> list = (List<Object>)object;
            for (int i = 0; i < list.size(); ++i) {
                text += stringify(list.get(i));
                if (i != list.size() - 1) {
                    text += ", ";
                }
            }
            text += "]";
            return text;
        }

        // Work around java adding ".0" to integer-valued doubles
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }

            return text;
        }

        return object.toString();
    }

    private Object evaluate(Expr expr) {
        return maybeDeref(expr.accept(this));
    }

    private Object evaluateWithoutDeref(Expr expr) {
        return expr.accept(this);
    }

    private Object maybeDeref(Object object) {
        if (object instanceof Reference) {
            return evaluate(((Reference)object).drf());
        }
        return object;
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    public void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }
}
