# Kotlin Where Clauses

Where clauses can be supplied to delete, select, and update statements. The Kotlin DSL provides an implementation
of a where clause that looks very close to natural SQL. This is accomplished through a combination of operator
overload functions, infix functions, and Kotlin receiver functions.

## Simple Where Clauses

The simplest form of where clause includes a single condition. An example follows:

```kotlin
select(foo) {
    from(bar)
    where { id isEqualTo 3 }
}

select(foo) {
    from(bar)
    where { id isBetween 3 and 7 }
}
```

In this case, `id` is an SqlColumn of type Integer, `isEqualTo` and `isBetween` are infix functions. These clauses can
also be written as follows by explicitly calling each function:

```kotlin
select(foo) {
    from(bar)
    where { id.isEqualTo(3) }
}

select(foo) {
    from(bar)
    where { id.isBetween(3).and(7) }
}
```

Most, but not all, of the built-in conditions can be expressed as infix functions. Conditions without parameters,
or varargs conditions, cannot be called with infix. Good examples of those conditions are `isNull` and `isIn`. In
those cases you will need to call the function directly as follows:

```kotlin
select(foo) {
    from(bar)
    where { id.isNull() }
}

select(foo) {
    from(bar)
    where { id.isIn(1, 2, 3) }
}
```

## Using Filter and Map

Many conditions support `filter` and `map` functions that can be used to test whether the condition should be rendered
or to change the value of the condition parameter(s). If you need to use the `filter` or `map` functions, then you
cannot use the infix functions. In this case you can use a function that creates the condition and then apply
that condition to the where clause. For example:

```kotlin
select(foo) {
    from(bar)
    where { id (isEqualTo(3).map { it + 2 }) }
}
```

In this case, `isEqualTo` is a function in the `org.mybatis.dynamic.sql.util.kotlin.elements` package, not the infix
function. Note that the condition is enclosed in parentheses. This is actually a function call using a Kotlin invoke
operator overload. This can also be called explicitly without the operator overload as follows:

```kotlin
select(foo) {
    from(bar)
    where { id.invoke(isEqualTo(3).map { it + 2 }) }
}
```

## Compound Where Clauses

Of course many where clauses are composed of more than one condition. The where DSL supports arbitrarily complex
where clauses with and/or/not phrases.

```kotlin
select(foo) {
    from(bar)
    where {
        id isEqualTo 3
        and { id isEqualTo 4 }
    }
    or { id isEqualTo 4 }
    and { not { id isEqualTo 6 } }
}
```

The `and`, `or`, and `not` functions each create a new context that can in turn include `and`, `or`, and `not`
functions. The DSL has no limit on the depth of nesting of these functions. When there are nested `and` and `or`
functions, the curly braces will essentially be rendered as parentheses in the final SQL.

## Initial and Subsequent Conditions

As shown above, the `where`, `and`, `or`, `not`, and `group` functions create a context where conditions can be
specified (`group` is detailed below). Every context supports two types of conditions:

1. A single initial condition (like `id isEqualTo 3`). If you specify more than one initial condition, the library
   will throw a runtime exception. There are multiple types of initial conditions detailed below.
2. Any number of subsequent conditions created by the `and` or `or` functions

Everything is optional - if you don't specify an initial condition, or any subsequent conditions, then nothing will
render.

For each context, the renderer will add parenthesis around the rendered context if there is more than one condition in
the context. Remember that the `filter` function can be used to remove a condition from rendering, so the parentheses
are added only if the enclosed conditions all render.

If you forget to specify an initial condition and only specify `and` and `or` groups, then the first "and" or "or"
will be removed during rendering. This to avoid a rendered where clause like "where and id = 3".

### Initial Condition Types

There are four types of initial conditions. Only one of the initial condition types may be specified in any
given context. Others must be enclosed in an `and` or an `or` block. The four types are as follows:

1. **Column and Criterion** (`id isEqualTo 3` - as shown above)
2. **Not** (`not { id isEqualTo 3 }` - as shown above)
3. **Exists** - for executing an exists sub-query:

    ```kotlin
    select(foo) {
        from(bar)
        where {
            exists {
                select(foo.allColumns())
                from(foo)
                where { foo.id isEqualTo bar.fooId }
            }
        }
    }
    ```
   
    You can accomplish a "not exists" by nesting `exists` inside a `not` block:

    ```kotlin
    select(foo) {
        from(bar)
        where { not {
            exists {
                select(foo.allColumns())
                from(foo)
                where { foo.id isEqualTo bar.fooId }
            }
        }}
    }
    ```
4. 
5. **Group** - for grouping conditions with parentheses:

    ```kotlin
    select(foo) {
        from(bar)
        where {
            group {
                id isEqualTo 3
                and { id isEqualTo 4 }
            }
            or { firstName.isNull() }
        }
    }
    ```

    The `group` function is used to insert parentheses around a group of conditions before
    and `and` or an `or`.

## Extending Where Clauses

In addition to the built-in conditions supplied with the library, it is possible to write your own custom
conditions. Any custom condition can be used with the "invoke operator" method shown above in the "Using Filter And Map"
section above.

At this time, it is not possible to add infix functions for custom conditions to the library. This is due to an
underlying limitation in Kotlin itself. There is a Kotlin language enhancement on the roadmap that will likely
remove this limitation. The enhancement will allow multiple receivers for an extension function. You can follow
progress of that enhancement here: https://youtrack.jetbrains.com/issue/KT-42435
