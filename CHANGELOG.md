# Change Log

This log will detail notable changes to MyBatis Dynamic SQL. Full details are available on the GitHub milestone pages.

## Release 1.1.3 - Unreleased

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.3+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.3+)

### Added

- Added support for `count(distinct ...)` ([#112](https://github.com/mybatis/mybatis-dynamic-sql/issues/112))
- Added support for multiple row inserts ([#116](https://github.com/mybatis/mybatis-dynamic-sql/issues/116))
- Utility classes and a new canonical pattern for MyBatis Generator (CRUD) mappers ([#118](https://github.com/mybatis/mybatis-dynamic-sql/issues/118)) ([#125](https://github.com/mybatis/mybatis-dynamic-sql/pull/125)) ([#128](https://github.com/mybatis/mybatis-dynamic-sql/pull/128))
- Kotlin Extensions and Kotlin DSL ([#133](https://github.com/mybatis/mybatis-dynamic-sql/pull/133))


## Release 1.1.2 - July 5, 2019

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.2+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.2+)

### Added

- Changed the public SQLBuilder API to accept Collection instead of List for in conditions and batch record inserts. This should have no impact on existing code, but allow for some future flexibility ([#88](https://github.com/mybatis/mybatis-dynamic-sql/pull/88))
- Added the ability have have table catalog and/or schema calculated at query runtime. This is useful for situations where there are different database schemas for different environments, or in some sharding situations ([#92](https://github.com/mybatis/mybatis-dynamic-sql/pull/92))
- Add support for paging queries with "offset" and "fetch first" - this seems to be standard on most databases ([#96](https://github.com/mybatis/mybatis-dynamic-sql/pull/96))
- Added the ability to call a builder method on any intermediate object in a select statement and receive a fully rendered statement. This makes it easier to build very dynamic queries ([#106](https://github.com/mybatis/mybatis-dynamic-sql/pull/106))
- Add the ability to modify values on any condition before they are placed in the parameter map ([#105](https://github.com/mybatis/mybatis-dynamic-sql/issues/105))
- Add the ability to call `where()` with no parameters. This aids in constructing very dynamic queries ([#107](https://github.com/mybatis/mybatis-dynamic-sql/issues/107))


## Release 1.1.1 - April 7, 2019

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.1+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.1+)

### Added

- Limit and offset support in the select statement
- Utilities for Spring Batch
- All conditions now support conditional rendering with lambdas
- Select * support
- Union all support

### Bugs Fixed

- Fixed self joins


## Release 1.1.0 - April 24, 2018

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.0+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.0+)

### Added

- Support for optional conditions
- Support for column comparison conditions
- Support for sub-queries in the update statement
- Support for expressions and constants in the select statement
- Support for function in the update statement

### Bugs Fixed

- Support group by after where

## Initial Release - December 17, 2017
