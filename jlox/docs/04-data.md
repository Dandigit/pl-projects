# Data
Data in Lox can be evaluated in expressions or functions, stored in variables and grouped in classes. First, let's go over the primitive data types.

## Data types
Lox is a dynamically typed language, meaning that the same variable can represent any type. The built in data types in Lox are as follows:

 - `num`: A double-precision number, e.g. `42`, `4.2`
 - `bool`: True or false (or truthy/falsey), e.g. `false`, `2 == 2.0`
 - `str`: An ASCII string. Literals are wrapped in `"` quotes, e.g `"Hello, world!"`
 - `nil`: Nothing. Nil. Null. None. e.g. `nil`
 - `fun`: A function. Can be called with `()`. e.g. `fun (x) { return x; }`

## Storing data
Variables can store data in Lox. They can be `volatile` (able to change) or `constant` (a constant value).

To define a volatile variable, use the `var` keyword:
```
var foo = 2
```

To define a constant variable, use the `const` keyword: (not implemented yet)
```
const goo = "I do not change."
```

After being defined, the values of volatile variables can be modified through **assignment**.
```
var life = 41 // oops!
life = 42 // fixed!
```
Constant variables, however, cannot be reassigned.
```
const life = 41 // oh no!
life = 42 // error
```


\
[<- Previous chapter](./03-basic-semantics.md) | [Next chapter ->](./05-functions.md)

[Table of contents](./00-contents.md)
