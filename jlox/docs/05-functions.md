# Functions
Functions are an important part of any programming language, processing and spitting out data.

## Definition
Functions in Lox are defined with the `fun` keyword. Here's an example of a named function:
```
fun square(n) {
    return n * n
}
```

Lambadas, or anonymous functions, are also supported within Lox, as functions are first class values. Here's the function from 
above, but defined using a variable and lambada:
```
var square = fun (n) {
    return n * n
}
```

Both do the same thing and both can be called like this:
```
square(3) // 9.
```

## Nesting
Lox supports nested functions and closures. Take the following example:
```
fun makeCounter() {
  var i = 0
  fun count() {
    i = i + 1
    print(i)
  }

  return count
}

var counter = makeCounter()
counter() // "1".
counter() // "2".
```
The variable, `i`, is static, as it's located within the closure. This behaviour allows functions to behave somewhat like 
classes, manipulating a common dataset.

# First-class
As the previous example showed, functions in Lox are considered first class values, meaning you can pass them around just like 
you would for any other type of data. This works perfectly fine:
```
fun add(x, y) {
    return x + y
}

add(1, 2) // 3
var sum = add
sum(1, 2) // 3
```


\
[<- Previous chapter](./04-data.md) | [Next chapter ->](./06-classes.md)

[Table of contents](./00-contents.md)
