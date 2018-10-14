# Basic semantics
Lox has a few defining traits. It's:
- object oriented,
- dynamically typed, and
- lexically scoped.

## Same same, but different
- Note that Lox is object-oriented, not object-forced. There's no "everything is a class" mentality, just flexible language 
components.
- Dynamic typing in Lox is convenient, but some consider it dangerous, so the `is` and `as` operators provide safety to what is 
usually a highly uncertain type system.
- Lexical scoping in Lox means that you can know a variable's scoping by simply reading the code.

## Optional semicolons
There's two extremes in the world of semicolons:
- `put; them; on; everything;`
- `dontPutThemAnyWhere`

The first approach gives a lot of freedom in how you format your code, but comes with the **disgusting** statement terminator 
we've come to know and hate.

The second approach cleans thing up, making them look a bit more 2018 than the former, but isn't free of downsides. For example,
sometimes you can't put multiple statements on one line:
```
do(); something() # Python syntax error
```
And other times you can't choose how you format your code! In Go:
```
if true {
    doSomething();
}
else {
    doAnotherThing();
}
```
That won't compile! So there's no perfect system! Or is there?

In Lox, semicolons are **optional**, meaning you can still separate statements with them. And, they're only ever inserted where
they traditionally would be, so no more Go-style `};` tokens.
