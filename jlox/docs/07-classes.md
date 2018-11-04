# Classes
As much as academia enjoy spitting on them, classes, and wider object oriented programming have become a natural way of thinking 
for many programmers around the world. Lox supports classes, but doesn't force you to use them, like Java does with its 
obsessive "everything is a class" philosophy.


## Definition
To define a class in Lox, use the `class` keyword. Methods on classes are defined just like functions, except without the `fun`.
```
class Snack {
    eat() {
        print("crunch crunch crunch")
    }
}
```

Variables on the class can be assigned with `this` - `init()` is a special method used to initialize (construct) classes, and 
it's often used to initialize the class's variables.
```
class Food {
    init(type, size) {
        this.type = type
        this.size = size
    }
    
    eat() {
        print("Eating a " + this.size + " " + this.type + ".")
    }
}
```

To initialize a new instance of a class, just call it like a function. Any paramaters that you pass will be passed to init().
```
Food("loaf of bread", "large") // Initializes a Food instance (and discards it).
var food = Food("lasagna", "tiny"); // Initializes a Food instance (and assigns it to the variable 'food')

food.eat() // Prints: "Eating a tiny lasagna."
```

## Static methods (metaclasses)
Say you want all instances of a class to share a method in common. With static methods, you can do that!

Instead of binding the method to each __instance__ of the class, a static method binds itself to the __class__ itself. For 
example:
```
class Math {
    class add(x, y) {
        return x + y
    }
    
    subtract(x, y) {
        return x - y
    }
}

// Called on the class itself
Math.add(2, 2) // 4
Math.subtract(4, 2) // error, not static method

var math = Math()
math.add(3, 1) // 4, static method retained
math.subtract(4, 1) // 3, instance method works now
```
The static method, `add()` can be called on the class itself or any instance of the class!

## Getter methods
Getter methods allow you to call a parameterless method on a class as if you were accessing a method. They're defined with no 
parameter list.
```
class Rectangle {
    init(length, width) {
        this.length = length
        this.width = width
    }
    
    // Getter method
    area {
        return this.length * this.width
    }
}

var rectangle = Rectangle(2, 2)

// Looks like a value, but really we're calling a method that returns the value.
var area = rectangle.area // 4
```

## Inheritance
Like other object-oriented languages, Lox supports inheritance. The `<` symbol is used to define a superclass, and the `super` 
keyword can be used to access the superclass.
```
class Snack {
    eat() {
        put("Eating a snack")
    }
    
    throwAway() {
        print("Wasted food. :(")
    }
}

class Chips < Snack {
    eat() {
        super.eat()
        print(" of chips.")
    }
}

var chips = Chips();
chips.eat() // Prints: "Eating a snack of chips"
chips.throwAway() // Prints: "Wasted food. :("
```

\
[<- Previous chapter](./06-functions.md) | [Next chapter ->](./08-modules.md)

[Table of contents](./00-contents.md)
