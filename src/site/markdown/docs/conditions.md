# Where Conditions

MyBatis Dynamic SQL supports a wide variety of where clause conditions. All conditions can be combined with "and" and "or" operators to create arbitrarily complex where clauses.

In the following examples:

  * "x" and "y" are values that will be rendered as prepared statement parameters. The resulting SQL is rendered in a format that is compatible with the target runtime (either MyBatis or Spring), but we will show standard prepared statement parameter markers for simplicity.
  * "foo" and "bar" are instances of SqlColumn.

## Simple Conditions

Simple conditions are the most common - they render the basic SQL operators.

| Condition | Example | Result |
|-----------|---------|--------|
| Between | where(foo, isBetween(x).and(y)) | `where foo between ? and ?` |
| Equals | where(foo, isEqualTo(x)) | `where foo = ?` |
| Greater Than | where(foo, isGreaterThan(x)) | `where foo > ?` |
| Greater Than or Equals | where(foo, isGreaterThanOrEqualTo(x)) | `where foo >= ?` |
| In | where(foo, isIn(x, y)) | `where foo in (?,?)` |
| In (case insensitive) | where(foo, isInCaseInsensitive(x, y)) | `where upper(foo) in (?,?)` (the framework will transform the values for x and y to upper case)|
| Less Than | where(foo, isLessThan(x)) | `where foo < ?` |
| Less Than or Equals | where(foo, isLessThanOrEqualTo(x)) | `where foo <= ?` |
| Like | where(foo, isLike(x)) | `where foo like ?` (the framework DOES NOT add the SQL wild cards to the value - you will need to do that yourself) |
| Like (case insensitive) | where(foo, isLikeCaseInsensitive(x)) | `where upper(foo) like ?` (the framework DOES NOT add the SQL wild cards to the value - you will need to do that yourself, the framework will transform the value of x to upper case) |
| Not Between | where(foo, isNotBetween(x).and(y)) | `where foo not between ? and ?` |
| Not Equals | where(foo, isNotEqualTo(x)) | `where foo <> ?` |
| Not In | where(foo, isNotIn(x, y)) | `where foo not in (?,?)` |
| Not In (case insensitive) | where(foo, isNotInCaseInsensitive(x, y)) | `where upper(foo) not in (?,?)` (the framework will transform the values for x and y to upper case)|
| Not Like | where(foo, isLike(x)) | `where foo not like ?` (the framework DOES NOT add the SQL wild cards to the value - you will need to do that yourself) |
| Not Like (case insensitive) | where(foo, isNotLikeCaseInsensitive(x)) | `where upper(foo) not like ?` (the framework DOES NOT add the SQL wild cards to the value - you will need to do that yourself, the framework will transform the value of x to upper case) |
| Not Null | where(foo, isNotNull()) | `where foo is not null` |
| Null | where(foo, isNull()) | `where foo is null` |


## Sub-Selects

Many conditions can be rendered with sub selects.

| Condition | Example | Result |
|-----------|---------|--------|
| Equals | where(foo, isEqualTo(select(bar).from(table2).where(bar, isEqualTo(x))) | `where foo = (select bar from table2 where bar = ?)` |
| Greater Than | where(foo, isGreaterThan(select(bar).from(table2).where(bar, isEqualTo(x))) | `where foo > (select bar from table2 where bar = ?)` |
| Greater Than  or Equals | where(foo, isGreaterThanOrEqualTo(select(bar).from(table2).where(bar, isEqualTo(x))) | `where foo >= (select bar from table2 where bar = ?)` |
| In | where(foo, isIn(select(bar).from(table2).where(bar, isLessThan(x))) | `where foo in (select bar from table2 where bar < ?)` |
| Less Than | where(foo, isLessThan(select(bar).from(table2).where(bar, isEqualTo(x))) | `where foo < (select bar from table2 where bar = ?)` |
| Less Than  or Equals | where(foo, isLessThanOrEqualTo(select(bar).from(table2).where(bar, isEqualTo(x))) | `where foo <= (select bar from table2 where bar = ?)` |
| Not Equals | where(foo, isNotEqualTo(select(bar).from(table2).where(bar, isEqualTo(x))) | `where foo <> (select bar from table2 where bar = ?)` |
| Not In | where(foo, isNotIn(select(bar).from(table2).where(bar, isLessThan(x))) | `where foo not in (select bar from table2 where bar < ?)` |


## Column Comparison Conditions

Column comparison conditions can be used to write where clauses comparing the values of columns in a table.

| Condition | Example | Result |
|-----------|---------|--------|
| Equals | where(foo, isEqualTo(bar)) | `where foo = bar` |
| Greater Than | where(foo, isGreaterThan(bar)) | `where foo > bar` |
| Greater Than or Equals | where(foo, isGreaterThanOrEqualTo(bar)) | `where foo >= bar` |
| Less Than | where(foo, isLessThan(bar)) | `where foo < bar` |
| Less Than or Equals | where(foo, isLessThanOrEqualTo(bar)) | `where foo <= bar` |
| Not Equals | where(foo, isNotEqualTo(bar)) | `where foo <> bar` |

## Optional Conditions

The optional conditions will only render into the final SQL if they are valid.

For example, you could code a search like this:

```java
    public List<AnimalData> searchPeople(String animalName_, Double bodyWeight_, Double brainWeight_) {
        ...
        SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .where(animalName, isEqualToWhenPresent(animalName_))
                .and(bodyWeight, isEqualToWhenPresent(bodyWeight_))
                .and(brainWeight, isEqualToWhenPresent(brainWeight_))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        ...
    }
```

In this example, the three conditions are optional and will only be rendered if the values passed to them are not null. If all three values are null, then no where clause will be generated.

The optional conditions render similarly to their counterpart simple conditions. The table below lists the rendering rules for each optional condition.   

| Condition | Example | Rendering Rules |
|-----------|---------|-----------------|
| Between| where(foo, isBetweenWhenPresent(x).and(y)) | The condition will render if both x and y values are non-null |
| Equals | where(foo, isEqualToWhenPresent(x)) | The condition will render if x is non-null |
| Greater Than | where(id, isGreaterThanWhenPresent(x)) | The condition will render if x is non-null |
| Greater Than or Equals | where(id, isGreaterThanOrEqualToThanWhenPresent(x)) | The condition will render if x is non-null |
| In | where(id, isInWhenPresent(x, y)) | The condition will filter all null values out of the list, and will render if any non-null values remain |
| In Case Insensitive | where(id, isInCaseInsensitiveWhenPresent(x, y)) | The condition will filter all null values out of the list, and will render if any non-null values remain |
| Less Than | where(id, isLessThanWhenPresent(x)) | The condition will render if x is non-null |
| Less Than or Equals | where(id, isLessThanOrEqualToThanWhenPresent(x)) | The condition will render if x is non-null |
| Like | where(id, isLikeWhenPresent(x)) | The condition will render if x is non-null |
| Like Case Insensitive | where(id, isLikeCaseInsensitiveWhenPresent(x)) | The condition will render if x is non-null |
| Not Between | where(id, isNotBetweenWhenPresent(x).and(y)) | The condition will render if both x and y values are non-null |
| Not Equals | where(id, isNotEqualToWhenPresent(x)) | The condition will render if x is non-null |
| Not In | where(id, isNotInWhenPresent(x, y)) | The condition will filter all null values out of the list, and will render if any non-null values remain |
| Not In Case Insensitive | where(id, isNotInCaseInsensitiveWhenPresent(x, y)) | The condition will filter all null values out of the list, and will render if any non-null values remain |
| Not Like | where(id, isNotLikeWhenPresent(x)) | The condition will render if x is non-null |
| Not Like Case Insensitive | where(id, isNotLikeCaseInsensitiveWhenPresent(x)) | The condition will render if x is non-null |

