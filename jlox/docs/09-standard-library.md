# The standard library
Currently, Lox has quite a small standard library. This is bound to change as I implement more features, but for now, it's 
small.

## Importing standard modules
To import a module from the standard library, use the `std:` prefix. For example:
```
import "std:File"
```

## I/O
### Console
- `put(value)` writes its parameter to stdout, outputting it in the console.
- `print(value)` writes both its parameter and a newline to stdout, "printing" it in the console.

### Files
The `File` class allows reading/writing of files.
- `init(path)` sets the class variable `this.path` to `path`.
- `read()` returns the contents of the file at `this.path` as a string.
- `write(data)` writes `data` to the file at `this.path`.
- `append(data)` appends `data` to the  file at `this.path`.

## Casting
- `str(value)` returns `value` as a string.
- `num(value)` returns `value` as a number.

\
[<- Previous chapter](./07-modules.md)

[Table of contents](./00-contents.md)
