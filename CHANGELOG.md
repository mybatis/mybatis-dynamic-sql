# Change Log

This log will detail notable changes to MyBatis Dynamic SQL. Full details are available on the GitHub milestone pages.

## Release 1.1.2 - Unreleased

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.2+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.2+)

### Added

- Changed the public SQLBuilder API to accept Collection instead of List for in conditions and batch record inserts. This should have no impact on existing code, but allow for some future flexibility


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
