# Modules
With large programs, libraries or packages, we can't store all the code in one file. That would be incredibly messy! That's 
where modules come in.

## Creating modules
A Lox module is a bit like a Python module: it's a Lox file that contains only classes and functions. For example:

`Math.lox`
```
class Math {
    class square(n) {
        return n * n
    }
}
```
This is a complete module that we can now import!

## Importing modules
You can now import this module into a Lox program (or another module) with an `import` statement. These are little lines at the 
top of the file that tell the compiler what it should import. Say we have a file, `main.lox` in the same directory as `Math.lox`.
```
import "Math.lox"

print(Math.square(3))
```

The import statement is evaluated, and `main.lox` is transformed into this:
```
class Math {
    class square(n) {
        return n * n
    }
}

print(Math.square(3))
```

Pretty nifty, huh?

\
[<- Previous chapter](./07-classes.md) | [Next chapter ->](./09-standard-library.md)

[Table of contents](./00-contents.md)
