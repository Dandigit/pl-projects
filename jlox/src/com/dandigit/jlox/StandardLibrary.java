package com.dandigit.jlox;

public class StandardLibrary {
    public static final String File =
            "class File {\n" +
                "init(path) {\n" +
                    "this.path = path\n" +
                "}\n" +
                "read() {\n" +
                    "return readFile(this.path)\n" +
                "}\n" +
                "write() {\n" +
                    "return writeFile(this.path)\n" +
                "}\n" +
                "append() {\n" +
                    "return appendFile(this.path)\n" +
                "}\n" +
            "}\n";

    public s

    public static String all() {
        return File;
    }
}
