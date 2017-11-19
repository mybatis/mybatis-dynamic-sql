# Select Statements

Select statements are the most complex SQL statements.  This library duplicates the syntax of the most common
select statements, but purposely does not cover every possibility.

In general, the following are supported:

1. The typical parts of a select statement including SELECT, DISTINCT, FROM, JOIN, WHERE, GROUP BY, UNION,
   ORDER BY
2. Tables can be aliased per select statement
3. Columns can be aliased per select statement
4. Some support for aggregates (avg, min, max, sum)
5. Equijoins of type INNER, LEFT OUTER, RIGHT OUTER, FULL OUTER
6. Subqueries in where clauses.  For example, `where foo in (select foo from foos where id < 36)` 

At this time, we do not support the following:

1. WITH expressions
2. HAVING expressions
3. Select from another select.  For example `select count(*) from (select foo from foos where id < 36)`
4. INTERSECT, EXCEPT, etc.
5. Calculated columns in select lists or anywhere else in a statement - although this can be supported with
   custom implementations of SelectListItem and/or SqlColumn

## General Selects
One main feature of the SELECT support is the support of a very flexible WHERE clause.
 
## Subqueries

## Joins

## Union Queries

## Annotated Mapper for Select Statements

## XML Mapper for Select Statements

## Notes on Order By

Order by phrases can be difficult to calculate when there are aliased columns, aliased tables, unions, and joins.
This library has taken a simple approach - the library will either write the column alias or the column
name into the order by phrase.  For the order by phrase, the table alias (if there is one) will be ignored.

In our testing, this caused an issue in only one case.  When there is an outer join and the select list contains
both the left and right join column.  In that case, the workaround is to supply a column alias for both columns. 

