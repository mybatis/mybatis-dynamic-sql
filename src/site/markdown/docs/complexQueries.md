# Complex Queries
Enhancements in version 1.1.2 make it easier to code complex queries. The Select DSL is implemented as a set of related objects. As the select statement is built, intermediate objects of various types are returned from the various methods that implement the DSL. The select statement can be completed by calling the `build()` method many of the intermediate objects. Prior to version 1.1.2, it was necessary to call `build()` on the **last** intermediate object. This restriction has been removed and it is now possible to call `build()` on **any** intermediate object. This, along with several other enhancements, has simplified the coding of complex queries.

For example, suppose you want to code a complex search on a Person table. The search parameters are id, first name, and last name. The rules are:

1. If an id is entered, use the id and ignore the other search parameters
1. If an id is not entered, then do a fuzzy search based on the other parameters

This can be implemented with code like the following...

```java
public SelectStatementProvider search(Integer targetId, String fName, String lName) {
    var builder = select(id, firstName, lastName)    // (1)
            .from(person)
            .where();    // (2)
        
    if (targetId != null) {    // (3)
        builder
            .and(id, isEqualTo(targetId));
    } else {
        builder
            .and(firstName, isLike(fName).when(Objects::nonNull).then(s -> "%" + s + "%"))    // (4)
            .and(lastName, isLikeWhenPresent(lName).then(this::addWildcards));    // (5)
    }

    builder
        .orderBy(lastName, firstName)
        .fetchFirst(50).rowsOnly();    // (6)
        
    return builder.build().render(RenderingStrategies.MYBATIS3);    // (7)
}
    
public String addWildcards(String s) {
    return "%" + s + "%";
}
```

Notes:

1. Note the use of the `var` keyword here. If you are using an older version of Java, the actual type is `QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder`
1. Here we are calling `where()` with no parameters. This sets up the builder to accept conditions further along in the code. If no conditions are added, then the where clause will not be rendered
1. This `if` statement implements the rules of the search. If an ID is entered , use it. Otherwise do a fuzzy search based on first name and last name.
1. The `then` statement on this line allows you to change the parameter value before it is placed in the parameter Map. In this case we are adding SQL wildcards to the start and end of the search String - but only if the search String is not null. If the search String is null, the lambda will not be called and the condition will not render
1. This shows using a method reference instead of a lambda on the `then`. Method references allow you to more clearly express intent. Note also the use of the `isLikeWhenPresent` condition which is a built in condition that checks for nulls
1. It is a good idea to limit the number of rows returned from a search. The library now supports `fetch first` syntax for limiting rows
1. Note that we are calling the `build` method from the intermediate object retrieved in step 1. It is no longer necessary to call `build` on the last object returned from a select builder

