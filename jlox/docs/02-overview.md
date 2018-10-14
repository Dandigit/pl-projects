# Overview
This implementation of Lox is based on the fantastic book, [Crafting Interpreters](https://craftinginterpreters.com). However, it's not exactly the same. You'll see why below.

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
**block:**
```
{
    <statement>
}
```

**expression**
```
<expression>
```

### Variables
**declaration:**
```
var v = "hi"
```

**constant declaration:**
```
const v = "hi"
```

### Functions
**named function:**
```
fun square(x) {
    return x * x
}
```

**lambada: (anonymous function)**
```
fun (x) {
    return x * x
}
```

**return:**
```
return <expression>
```

### Classes
**declaration:**
```
class Foo {}
```

**methods:**
```
class Foo {
    init() {
        // Constructor
    }
    
    otherMethod() {
        print("foo")
    }
```

**static methods:**
```
class Foo {
    class staticMethod(x) {
        put(x)
    }
    
    notStaticMethod(x) {
        print(x)
    }
}
```

**getters:**
```
class Foo {
    area {
        return 3.1231244
    }
}

var foo = Foo()
foo.area // Calls the getter
```

**new instance:**
```
Foo() // Creates (and discards) a new instance of Foo.
var foo = Foo() // Creates (and assigns to a variable) a new instance of Foo.
```

### Control flow
**if:**
```
if (condition) <statement>

if (condition) {
    <statement>
}
```

**while:**
```
while (condition) statement

while (condition) {
    <statement>
}
```

**for:**
```
for (initialization; condition; incrementer) <statement>

for (<initialization>; <condition>; <incrementer>) {
    <statement>
}
```
