# Overview
This implementation of Lox is based on the fantastic book, [Crafting Interpreters](https://craftinginterpreters.com). However, it's not exactly the same. You'll see why below.

## Syntax
### Operators
Below are all operators in Lox, listed from highest to lowest precedence.

| Operator(s) | Type | Associativity |
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
