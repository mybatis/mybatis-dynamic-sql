# Extending MyBatis Dynamic SQL

The library has been designed for extension from the very start of the design.  We do not believe that the library covers all possible uses and we wanted to make it possible to add functionality that suits the needs of different projects.

This page details the main extension points of the library.

## Extending the SELECT Capabilities
The SELECT support is the most complex part of the library, and also the part of the library that is most likely to be extended.  There are two main interfaces involved with extending the SELECT support.  Picking which interface to implement is dependent on how you want to use your extension.

| Interface | Purpose|
|-----------|--------|
| `org.mybatis.dynamic.sql.BasicColumn` | Use this interface if you want to add capabilities to a SELECT list or a GROUP BY expression. For example, creating a calculated column. |
| `org.mybatis.dynamic.sql.BindableColumn` | Use this interface if you want to add capabilities to a WHERE clause. For example, creating a custom condition. |

See the following sections for examples.

### Supporting Calculated Columns

A calculated column can be used anywhere in a SELECT statement.  If you don't need to use it in a WHERE clause, then it is easier to implement the `org.mybatis.dynamic.sql.BasicColumn` interface.  An example from the library itself is the `org.mybatis.dynamic.sql.select.aggregate.CountAll` class:

```java
public class CountAll implements BasicColumn {
    
    private Optional<String> alias = Optional.empty();

    public CountAll() {
        super();
    }

    @Override
    public String renderWithTableAlias(TableAliasCalculator tableAliasCalculator) {
        return "count(*)"; //$NON-NLS-1$
    }

    @Override
    public Optional<String> alias() {
        return alias;
    }

    @Override
    public CountAll as(String alias) {
        CountAll copy = new CountAll();
        copy.alias = Optional.of(alias);
        return copy;
    }
}
```

This class is used to implement the `count(*)` function in a SELECT list.  There are only three methods to implement:

1. `renderWithTableAlias` - the default renderers will write the value returned from this function into the select list - or the GROUP BY expression.  If your item can be altered by a table alias, then here is where you change the return value based on the alias specified by the user.  For a `count(*)` expression, a table alias never applies, so we just return the same value whether or not an alias has been specified by the user.
2.  `as` - this method can be called by the user to add an alias to the column expression.  In the method you should return a new instance of the object, with the alias passed by the user.
3. `alias` - this method is called by the default renderer to obtain the column alias for the select list.  If there is no alias, then returning Optional.empty() will disable setting a column alias.

### Writing a Custom Where Condition

If you want to use your calculated column in a WHERE clause in addition the select list and the GROUP BY clause, then you must implement `org.mybatis.dynamic.sql.BindableColumn`.  This interface extends the `BasicColumn` interface from above and adds two methods.  An example from the library is the `org.mybatis.dynamic.sql.select.function.Add` class:

```java
public class Add<T extends Number> implements BindableColumn<T> {
    
    private Optional<String> alias = Optional.empty();
    private BindableColumn<T> column1;
    private BindableColumn<T> column2;
    
    private Add(BindableColumn<T> column1, BindableColumn<T> column2) {
        this.column1 = Objects.requireNonNull(column1);
        this.column2 = Objects.requireNonNull(column2);
    }

    @Override
    public Optional<String> alias() {
        return alias;
    }

    @Override
    public String renderWithTableAlias(TableAliasCalculator tableAliasCalculator) {
        return column1.applyTableAliasToName(tableAliasCalculator)
                + " + " //$NON-NLS-1$
                + column2.applyTableAliasToName(tableAliasCalculator);
    }

    @Override
    public BindableColumn<T> as(String alias) {
        Add<T> newColumn = new Add<>(column1, column2);
        newColumn.alias = Optional.of(alias);
        return newColumn;
    }

    @Override
    public JDBCType jdbcType() {
        return column1.jdbcType();
    }

    @Override
    public Optional<String> typeHandler() {
        return column1.typeHandler();
    }

    public static <T extends Number> Add<T> of(BindableColumn<T> column1, BindableColumn<T> column2) {
        return new Add<>(column1, column2);
    }
}
```

This class implements the idea of adding two numeric columns together in a SELECT statement.  This class accepts two other columns as parameters and then overrides `renderWithTableAlias` to render the addition.  The `alias` and `as` methods work as described above.

The two additional methods are:

1. `jdbcType` - returns the JDBC Type of the column for the WHERE clause.  This is used by the MyBatis3 rendering strategy to render a full MyBatis parameter expression.
2. `typeHandler` - returns a type handler if specified by the user.  Again, this is used by the MyBatis3 rendering strategy to render a full MyBatis parameter expression.  


## Writing Custom Rendering Strategies

A RenderingStrategy is used to format the parameter placeholders in generated SQL. The library ships with two built-in rendering strategies:

1. A strategy that is suitable for MyBatis3.  This strategy generates placeholders in the format required by MyBatis3 (for example `#{foo,jdbcType=INTEGER}`).
2. A strategy that is suitable for the Spring NamedParameterJDBCTemplate. This strategy generates placeholders in the format required by Spring (for example `:foo`).

You can write a custom rendering strategy if you want to use the library with some other framework.  For example, if you wanted to use the library to generate SQL that could be prepared directly by JDBC, you could write a rendering strategy that simply uses the question mark (`?`) for all parameters.

```java
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;

public class PlainJDBCRenderingStrategy extends RenderingStrategy {
    @Override
    public String getFormattedJdbcPlaceholder(BindableColumn<?> column, String prefix, String parameterName) {
        return "?";
    }

    @Override
    public String getFormattedJdbcPlaceholder(String prefix, String parameterName) {
        return "?";
    }
}

```
The library will pass the following parameters to the `getFormattedJdbcPlaceholder` method:

1. `column` - the column definition for the current placeholder
2. `prefix` - For INSERT statements the value will be "record", for all other statements (including inserts with selects) the value will be "parameters"
3. `parameterName` - this will be the unique name for the the parameter.  For INSERT statements, the name will be the property of the inserted record that is mapped to this parameter.  For all other statements (including inserts with selects) a unique name will be generated by the library.  That unique name will also be used to place the value of the parameter into the parameters Map.

## Writing Custom Renderers

SQL rendering is accomplished by classes that are decoupled from the SQL model classes.  All the model classes have a `render` method that calls the built-in default renderers, but this is completely optional and you do not need to use it.  You can write your own rendering support if you are dissatisfied with the SQL produced by the default renderers.

Writing a custom renderer is quite complex.  If you want to undertake that task, we suggest that you take the time to understand how the default renderers work first.  And feel free to ask questions about this topic on the MyBatis mailing list.
