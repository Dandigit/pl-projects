package com.dandigit.jlox;

import java.util.List;
import java.util.Map;

class NativeInstance implements LoxCallable {
    final String name;
    private final Map<String, LoxCallable> methods;

    NativeInstance(String name, Map<String, LoxCallable> methods) {
        this.name = name;
        this.methods = methods;
    }

    LoxCallable findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        return null;
    }

    @Override
    public String toString() {
        return "<native instance>";
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxCallable initializer = methods.get("init");
        if (initializer != null) {
            initializer.call(interpreter, arguments);
        }

        return this;
    }

    @Override
    public int arity() {
        LoxCallable initializer = methods.get("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }
}