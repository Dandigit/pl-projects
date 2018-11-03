package com.dandigit.jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Lox {
    public static final List<Object> argv = new ArrayList<>();

    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    class ErrorCode
    {
        public static final int INVALID_ARGUMENTS = 64;
        public static final int FILE_ERROR = 65;
        public static final int STATIC_ERROR = 70;
        public static final int RUNTIME_ERROR = 75;
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            // Too many arguments
            System.out.println("Usage: jlox [script]");
            System.exit(ErrorCode.INVALID_ARGUMENTS);
        } else if (args.length == 1) {
            // We can run the provided file...
            try {
                runFile(args[0]);
                for (int i = 1; i < args.length; ++i) {
                    argv.add(args[i]);
                }
            } catch (IOException exception) {
                System.err.println("Error: Unable to read file '" + args[0] + "'.");
                System.exit(ErrorCode.FILE_ERROR);
            }
        } else {
            // Or run a REPL
            runPrompt();
        }
    }

    public static void runFile(String path) throws IOException {
        // Pass the contents of the file to run()
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // Don't try and execute code that has a known error
        if (hadError) System.exit(ErrorCode.STATIC_ERROR);
        if (hadRuntimeError) System.exit(ErrorCode.RUNTIME_ERROR);
    }

    public static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        // Infinitely read lines from the console and run() them
        while (true) {
            System.out.print("lox > ");
            run(reader.readLine() + "\n");

            // Don't kill the whole session if there was an error
            hadError = false;
        }
    }

    public static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) return;

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        // Stop if there was a resolution error.
        if (hadError) return;

        interpreter.interpret(statements);
    }

    private static void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else if (token.lexeme == "\n") {
            report(token.line, " at newline", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
}
