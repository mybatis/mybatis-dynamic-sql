# Extending MyBatis Dynamic SQL

The library has been designed for extension from the very beginning.  We do not believe that the library
covers all possible uses, and we wanted to make it possible to add functionality that suits the needs of different
projects.

This page details the main extension points of the library.

## Extending the SELECT Capabilities
The SELECT support is the most complex part of the library, and also the part of the library that is most likely to be
extended.  There are two main interfaces involved with extending the SELECT support.  Picking which interface to
implement is dependent on how you want to use your extension.

| Interface                                | Purpose                                                                                                                                                          |
|------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `org.mybatis.dynamic.sql.BasicColumn`    | Use this interface if you want to add capabilities to a SELECT list, a GROUP BY, or an ORDER BY expression. For example, using a database function.              |
| `org.mybatis.dynamic.sql.BindableColumn` | Use this interface if you want to add capabilities to a WHERE clause in addition to the capabilities of `BasicColumn`. For example, creating a custom condition. |

Rendering is the process of generating an appropriate SQL fragment to implement the function or calculated column.
The library will call a method `render(RenderingContext)` in your implementation. This method should return an
instance of `FragmentAndParameters` containing your desired SQL fragment and any bind parameters needed. Bind
parameter markers can be calculated by calling the `RenderingContext.calculateParameterInfo()` method. That method will
return a properly formatted bind marker for the SQL string, and a matching Map key you should use in your parameter map.
In general, you do not need to worry about adding spacing, commas, etc. before or after your fragment - the library
will properly format the final statement from all the different fragments.

See the following sections for examples.

### Supporting Calculated Columns

A calculated column can be used anywhere in a SELECT statement.  If you don't need to use it in a WHERE clause, then it
is easier to implement the `org.mybatis.dynamic.sql.BasicColumn` interface.  An example follows:

```java
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class CountAll implements BasicColumn {

    private String alias;

    @Override
    public FragmentAndParameters render(RenderingContext renderingContext) {
        return FragmentAndParameters.fromFragment("count(*)"); //$NON-NLS-1$
    }

    @Override
    public Optional<String> alias() {
        return Optional.ofNullable(alias);
    }

    @Override
    public CountAll as(String alias) {
        CountAll copy = new CountAll();
        copy.alias = alias;
        return copy;
    }
}
```

This class is used to implement the `count(*)` function in a SELECT list.  There are only three methods to implement:

1. `render` - the default renderers will write the value returned from this function into the select list - or the
   GROUP BY expression.  If your item can be altered by a table alias, then here is where you change the return value
   based on the alias specified by the user.  For a `count(*)` expression, a table alias never applies, so we just
   return the same value regardless of whether an alias has been specified by the user.
2. `as` - this method can be called by the user to add an alias to the column expression.  In the method you should
   return a new instance of the object, with the alias passed by the user.
3. `alias` - this method is called by the default renderer to obtain the column alias for the select list.  If there is
   no alias, then returning Optional.empty() will disable setting a column alias.

## Writing Custom Functions

Relational database vendors provide hundreds of functions in their SQL dialects to aid with queries and offload
processing to the database servers. This library does not try to implement every function that exists. This library
also does not provide any abstraction over the different functions on different databases. For example, bitwise operator
support is non-standard, and it would be difficult to provide a function in this library that worked on every database.
So we take the approach of supplying examples for a few very common functions, and making it relatively easy to write
your own functions.

The supplied functions are all in the `org.mybatis.dynamic.sql.select.function` package. They are all implemented
as `BindableColumn` - meaning they can appear in a select list or a where clause.

We provide some base classes that you can easily extend to write functions of your own. Those classes are as follows:

Note: the base classes are all in the `org.mybatis.dynamic.sql.select.function` package.

| Interface                                    | Purpose                                                                                                                                                                      |
|----------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `o.m.d.s.s.f.AbstractTypeConvertingFunction` | Extend this class if you want to build a function that changes a column data type. For example, using a database function to calculate the Base64 String for a binary field. |
| `o.m.d.s.s.f.AbstractUniTypeFunction`        | Extend this class if you want to build a function that does not change a column data type. For example UPPER(), LOWER(), etc.                                                |
| `o.m.d.s.s.f.OperatorFunction`               | Extend this class if you want to build a function the implements an operator. For example column1 + column2.                                                                 |

### AbstractTypeConvertingFunction Example

The following function uses HSQLDB's `TO_BASE64` function to calculate the BASE64 string for a binary field. Note that
the function changes the data type from `byte[]` to `String`.

```java
import java.sql.JDBCType;
import java.util.Optional;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.function.AbstractTypeConvertingFunction;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class ToBase64 extends AbstractTypeConvertingFunction<byte[], String, ToBase64> {

   private ToBase64(BasicColumn column) {
      super(column);
   }

   @Override
   public Optional<JDBCType> jdbcType() {
      return Optional.of(JDBCType.VARCHAR);
   }

   @Override
   public FragmentAndParameters render(RenderingContext renderingContext) {
      FragmentAndParameters renderedColumn = column.render(renderingContext);

      return FragmentAndParameters
              .withFragment("TO_BASE64(" + renderedColumn.fragment() + ")") //$NON-NLS-1$ //$NON-NLS-2$
              .withParameters(renderedColumn.parameters())
              .build();
   }

   @Override
   protected ToBase64 copy() {
      return new ToBase64(column);
   }

   public static ToBase64 toBase64(BindableColumn<byte[]> column) {
      return new ToBase64(column);
   }
}
```

### AbstractUniTypeFunction Example

The following function implements the common database `UPPER()` function.

```java
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.function.AbstractUniTypeFunction;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class Upper extends AbstractUniTypeFunction<String, Upper> {

   private Upper(BasicColumn column) {
      super(column);
   }

   @Override
   public FragmentAndParameters render(RenderingContext renderingContext) {
      FragmentAndParameters renderedColumn = column.render(renderingContext);

      return FragmentAndParameters
              .withFragment("upper(" + renderedColumn.fragment() + ")") //$NON-NLS-1$ //$NON-NLS-2$
              .withParameters(renderedColumn.parameters())
              .build();
   }

   @Override
   protected Upper copy() {
      return new Upper(column);
   }

   public static Upper of(BindableColumn<String> column) {
      return new Upper(column);
   }
}
```

Note that `FragmentAndParameters` has a utility method that can simplify the implementation if you do not need to
add any new parameters to the resulting fragment. For example, the UPPER function can be simplified as follows:

```java
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.function.AbstractUniTypeFunction;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class Upper extends AbstractUniTypeFunction<String, Upper> {

   private Upper(BasicColumn column) {
      super(column);
   }

   @Override
   public FragmentAndParameters render(RenderingContext renderingContext) {
      return column.render(renderingContext).mapFragment(f -> "upper(" + f + ")"); //$NON-NLS-1$ //$NON-NLS-2$
   }

   @Override
   protected Upper copy() {
      return new Upper(column);
   }

   public static Upper of(BindableColumn<String> column) {
      return new Upper(column);
   }
}
```


### OperatorFunction Example

The following function implements the concatenate operator. Note that the operator can be applied to list of columns of
arbitrary length:

```java
import java.util.Arrays;
import java.util.List;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.select.function.OperatorFunction;

public class Concatenate<T> extends OperatorFunction<T> {

    protected Concatenate(BasicColumn firstColumn, BasicColumn secondColumn,
            List<BasicColumn> subsequentColumns) {
        super("||", firstColumn, secondColumn, subsequentColumns); //$NON-NLS-1$
    }

    @Override
    protected Concatenate<T> copy() {
        return new Concatenate<>(column, secondColumn, subsequentColumns);
    }

    public static <T> Concatenate<T> concatenate(BindableColumn<T> firstColumn, BasicColumn secondColumn,
            BasicColumn... subsequentColumns) {
        return new Concatenate<>(firstColumn, secondColumn, Arrays.asList(subsequentColumns));
    }
}
```

## Writing Custom Rendering Strategies

A RenderingStrategy is used to format the parameter placeholders in generated SQL. The library ships with two built-in
rendering strategies:

1. A strategy that is suitable for MyBatis3.  This strategy generates placeholders in the format required by MyBatis3
   (for example `#{foo,jdbcType=INTEGER}`).
2. A strategy that is suitable for the Spring NamedParameterJDBCTemplate. This strategy generates placeholders in the
   format required by Spring (for example `:foo`).

You can write a custom rendering strategy if you want to use the library with some other framework.  For example, if you
wanted to use the library to generate SQL that could be prepared directly by JDBC, you could write a rendering strategy
that simply uses the question mark (`?`) for all parameters.

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
2. `prefix` - For INSERT statements the value will be "record", for all other statements (including inserts with
   selects) the value will be "parameters"
3. `parameterName` - this will be the unique name for the parameter.  For INSERT statements, the name will be the
   property of the inserted record mapped to this parameter.  For all other statements (including inserts with selects)
   a unique name will be generated by the library.  That unique name will also be used to place the value of the
   parameter into the parameters Map.

## Writing Custom Renderers

SQL rendering is accomplished by classes that are decoupled from the SQL model classes.  All the model classes have a
`render` method that calls the built-in default renderers, but this is completely optional, and you do not need to use
it.  You can write your own rendering support if you are dissatisfied with the SQL produced by the default renderers.

Writing a custom renderer is quite complex.  If you want to undertake that task, we suggest that you take the time to
understand how the default renderers work first.  Feel free to ask questions about this topic on the MyBatis mailing
list.

## Writing Custom Conditions

The library supplies a full range of conditions for all the common SQL operators (=, !=, like, between, etc.) Some
databases support extensions to the standard operators. For example, MySQL supports an extension to the "LIKE"
condition - the "ESCAPE" clause. If you need to implement a condition like that, then you will need to code a
custom condition.

Here's an example of implementing a LIKE condition that supports ESCAPE:

```java
@NullMarked
public class IsLikeEscape<T> extends AbstractSingleValueCondition<T> {
    private static final IsLikeEscape<?> EMPTY = new IsLikeEscape<Object>(-1, null) {
        @Override
        public Object value() {
            throw new NoSuchElementException("No value present"); //$NON-NLS-1$
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    };

    public static <T> IsLikeEscape<T> empty() {
        @SuppressWarnings("unchecked")
        IsLikeEscape<T> t = (IsLikeEscape<T>) EMPTY;
        return t;
    }

    private final @Nullable Character escapeCharacter;

    protected IsLikeEscape(T value, @Nullable Character escapeCharacter) {
        super(value);
        this.escapeCharacter = escapeCharacter;
    }

    @Override
    public String operator() {
        return "like";
    }

    @Override
    public FragmentAndParameters renderCondition(RenderingContext renderingContext, BindableColumn<T> leftColumn) {
        var fragment = super.renderCondition(renderingContext, leftColumn);
        if (escapeCharacter != null) {
            fragment = fragment.mapFragment(this::addEscape);
        }

        return fragment;
    }

    private String addEscape(String s) {
        return s + " ESCAPE '" + escapeCharacter + "'";
    }

    @Override
    public IsLikeEscape<T> filter(Predicate<? super T> predicate) {
        return filterSupport(predicate, IsLikeEscape::empty, this);
    }

    public <R> IsLikeEscape<R> map(Function<? super T, ? extends R> mapper) {
        return mapSupport(mapper, v -> new IsLikeEscape<>(v, escapeCharacter), IsLikeEscape::empty);
    }

    public static <T> IsLikeEscape<T> isLike(T value) {
        return new IsLikeEscape<>(value, null);
    }

    public static <T> IsLikeEscape<T> isLike(T value, Character escapeCharacter) {
        return new IsLikeEscape<>(value, escapeCharacter);
    }
}
```

Important notes:

1. The class extends `AbstractSingleValueCondition` - which is appropriate for like conditions
2. The class constructor accepts an escape character that will be rendered into an ESCAPE phrase
3. The class overrides `renderCondition` and changes the library generated `FragmentAndParameters` to add the ESCAPE
   phrase. **This is the key to what's needed to implement a custom condition.**
4. The class provides `map` and `filter` functions as is expected for any condition in the library
