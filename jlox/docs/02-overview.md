# Overview
This implementation of Lox is based on the book [Crafting Interpreters](https://craftinginterpreters.com). However, it's not exactly the same. You'll see why below.

## Features
- Implicit semicolons
- Nesting multiline comments
- Classes with inheritance, getter methods and static methods
- An extended standard library

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


## Examples
Examples of pretty much every language feature are available in [the examples directory](../examples).

\
[<- Previous chapter](./01-setup.md) | [Next chapter ->](./03-basic-semantics.md)

[Table of contents](./00-contents.md)
