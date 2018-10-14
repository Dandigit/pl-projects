# Overview
This implementation of Lox is based on the book [Crafting Interpreters](https://craftinginterpreters.com). However, it's not exactly the same. You'll see why below.

## Operators
Below are all operators in Lox, listed from highest to lowest precedence.

| Operator(s)          | Type           | Associativity |
| :------------------: | -------------- | ------------- |
| `()` (call)          | Unary postfix  | Left-to-right |
| `!`, `-`             | Unary prefix   | Right-to-left |
| `*`, `/`             | Binary         | Left-to-right |
| `+`, `-`             | Binary         | Left-to-right |
| `<`, `>`, `<=`, `>=` | Binary         | Left-to-right |
| `==`, `!=`           | Binary         | Left-to-right |
| `and`                | Logical binary | Left-to-right |
| `or`                 | Logical binary | Left-to-right |
| `? :`                | Ternary        | Right-to-left |
| `=`                  | Binary         | Right-to-left |
| `,`                  | Binary         | Left-to-right |


## Statements
### General
Lox supports block statements, which group multiple statements into one.
Expression statements evaluate an expression and discard the result.

[example](../examples/general.lox)

### Functions
Functions in Lox are defined with the `fun` keyword. They can take a maximum of 8 paramaters. Lambadas (anonymous functions) are also supported.

[example](../examples/function.lox)

### Classes
Lox classes can be defined with the `class` keyword. Inheritance is supported. Classes can define static or instance methods and getter methods. Values can be assigned through `this`.

[example](../examples/class.lox)

### Control flow
Lox supports if/else conditional statements. While loops and C-style for loops are also supported.

[example](../examples/control-flow.lox)

[<- Previous chapter](./01-setup.md) ------ [Next chapter ->](./03-data.md)
