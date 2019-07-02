# About these Tests

GitHub issues 100 and 102 exposed issues where calling the `build()` method at an unexpected location caused the rendered SQL to be incorrect. Changes for this issue now allow for the `build()` method to be called on any intermediate object in the select statement chain * regardless if further operations have been performed * and consistent renderings will occur.

Tests in this directory cover many of the possible places where a `build()` method could be called and many of the possible paths through a select statement.
