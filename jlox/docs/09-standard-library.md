# The standard library
Currently, Lox has quite a small standard library. This is bound to change as I implement more features, but for now, it's 
small.

## Importing standard modules
To import a module from the standard library, use the `std:` prefix. For example:
```
import "std:File"
```

## Built in functions
These functions are built right into Lox, so you don't have to import any modules to use them.
### Console I/O
- `put(value)` writes its parameter to stdout, outputting it in the console.
- `print(value)` writes both its parameter and a newline to stdout, "printing" it in the console.
- `input()` gets a line of input from stdin and returns it.

### Type casting
- `str(value)` returns `value` as a string.
- `num(value)` returns `value` as a number.

### Other
- `len(object)` takes either a string or a list and returns the amount of characters or elements respectively.

## `std:Bitwise` module
The `Bitwise` module contains functions that emulate the behaviour of classic bitwise operators.
- `Bitwise.leftShift(x, y)` emulates `x << y`.
- `Bitwise.rightShift(x, y)` emulates `x >> y`.

## `std:File` module
The `File` class allows reading/writing of files.
- `File(path)` opens the file at `path` If it doesn't exist, it is created..
- `File.read()` returns the contents of the open file as a string.
- `File.write(data)` writes `data` to the open file.
- `File.append(data)` appends `data` to the open file.

## `std:Random` module
The `Random` module allows for the generation of pseudo-random numbers through a liner congruential generator.
- `Random.seed(seed)`

\
[<- Previous chapter](./08-modules.md)

[Table of contents](./00-contents.md)
