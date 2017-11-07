# Select Statements

Select statements are the most complex SQL statements.  This library duplicates the syntax of the most common
select statements, but purposely does not cover every possibility.

In general, the following are supported:

1. The typical parts of a select statement including SELECT, DISTINCT, FROM, WHERE, GROUP BY, UNION,
   ORDER BY
2. Tables can be aliased per select statement
3. Columns can be aliased per select statement
4. Some support for aggregates (avg, min, max, sum)
5. Equijoins of type INNER, LEFT OUTER, RIGHT OUTER, FULL OUTER
6. Subselects in where clauses.  For example, `where foo in (select foo from foos where id < 36)` 

At this time, we do not support the following:

1. WITH expressions
2. HAVING expressions
3. Select from another select.  For example `select count(*) from (select foo from foos where id < 36)`
4. INTERSECT, EXCEPT, etc.
5. Calculated columns in select lists or anywhere else in a statement - although this can be supported with
   custom implementations of SelectListItem and/or SqlColumn


## General Selects

## Subselects

## Joins

## Union Queries

