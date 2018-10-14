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

Both have their ups and downs, and neither are perfect. Lox strikes a balance.

In Lox, semicolons are **optional**, meaning you can still separate statements with them. And, they're only ever inserted where
they traditionally would be in a C-style language, meaning you're still free to format your code how you want.

## Comments
Lox supports C-style single and multi-line comments, plus they can nest!
```
// I may be a comment, but I don't have an opinion

/* We can /*
nest now? */
That's cool! */
```
