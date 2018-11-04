# Arrays
Arrays in Lox are dynamic, flexible containers that allow data to be listed and indexed.

## Array literals
An array literal in Lox is a list of expressions surrounded by square brackets:
```
["Hello", 2, true, nil]
```

An empty array literal contains no expressions:
```
[]
```

And just like any other value, arrays can be assigned to variables and passed around:
```
var numbers = [1, 2, 3, 4, 5]
print(numbers)
```

## Manipulating arrays
In Lox, 'adding' an elemnt to an array is dead simple. You just 'add' the element to the array!
```
var numbers = [1, 2, 3, 4, 5]
numbers = numbers + 6
print(numbers) // [1, 2, 3, 4, 5, 6]
```

Shrinking an array is also trivial. You 'subtract' a specified number of elements from the array.
```
var numbers = [1, 2, 3, 4, 5]
numbers = numbers - 2
print(numbers) // [1, 2, 3]
```

A built in function, `len()`, obtains the length of a given array.
```
var foo = [1, 2]
len(foo) // 2
```

## Accessing array elements
To access a specific element of an array, you may **subscript** it, like you would in C.
```
var foo = [1, 2]
foo[0] // 1
```
Array indexes start at 0, so in the above example, the 0th element of the array is 1.
You can assign to individual array elements as well:
```
var foo = [1, 2]
foo[0] = 2
print(foo) // [2, 2]
```

\
[<- Previous chapter](./04-data.md) | [Next chapter ->](./06-functions.md)

[Table of contents](./00-contents.md)
