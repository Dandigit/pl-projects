package com.dandigit.jlox;

import java.util.HashMap;
import java.util.List;

public class StandardLibrary {
    public static final String File =
            "class File {\n" +
                "init(path) {\n" +
                    "this.path = path\n" +
                "}\n" +
                "read() {\n" +
                    "return readFile(this.path)\n" +
                "}\n" +
                "write(data) {\n" +
                    "return writeFile(this.path, data)\n" +
                "}\n" +
                "append(data) {\n" +
                    "return appendFile(this.path, data)\n" +
                "}\n" +
            "}\n";

    public static final String Random =
            "import \"std:Bitwise\"\n" +
            "var _stdRandomSeed = clock()\n" +
            "class Random {\n" +
                "class seed(number) {\n" +
                    "_stdRandomSeed = number\n" +
                "}\n" +
                "class random() {\n" +
                    "_stdRandomSeed = (1103515245 * _stdRandomSeed + 12345) % 2147483648\n" +
                    "return Bitwise.rightShift(_stdRandomSeed, 0)\n" +
                "}\n" +
                "class inRange(min, max) {\n" +
                    "return min + round((max - min + 1) * num(Random.random()) * num(1 / 2147483648))\n" +
                "}\n" +
            "}\n";

    public static final NativeInstance Bitwise =
            new NativeInstance("Bitwise", new HashMap<String, LoxCallable>() {{
                put("leftShift", new LoxCallable() {
                    @Override
                    public int arity() {
                        return 2;
                    }

                    @Override
                    public Object call(Interpreter interpreter, List<Object> arguments) {
                        return ((Integer)(
                                ((Double)arguments.get(0)).intValue() <<
                                ((Double)arguments.get(1)).intValue())).doubleValue();
                    }
                });

                put("rightShift", new LoxCallable() {
                    @Override
                    public int arity() {
                        return 2;
                    }

                    @Override
                    public Object call(Interpreter interpreter, List<Object> arguments) {
                        return ((Integer)(
                                ((Double)arguments.get(0)).intValue() >>
                                ((Double)arguments.get(1)).intValue())).doubleValue();
                    }
                });
            }});

    public static void importAll(Environment environment) {
        Lox.run(File + Random);
        environment.define("Bitwise", Bitwise);
    }
}
