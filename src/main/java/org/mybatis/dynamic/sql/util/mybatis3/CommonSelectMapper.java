/*
 *    Copyright 2016-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.util.mybatis3;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.ibatis.annotations.SelectProvider;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

/**
 * This is a general purpose MyBatis mapper for select statements. It allows you to execute select statements without
 * having to write a custom {@link org.apache.ibatis.annotations.ResultMap} for each statement.
 *
 * <p>This mapper contains three types of methods:
 * <ul>
 *   <li>The selectOneMappedRow and selectManyMappedRows methods allow you to use select statements with
 *     any number of columns. MyBatis will process the rows and return a Map of values, or a List of Maps.</li>
 *   <li>The selectOne and selectMany methods also allow you to use select statements with any number of columns.
 *   These methods also allow you to specify a function that will transform a Map of row values into a specific
 *   object.</li>
 *   <li>The other methods are for result sets with a single column. There are functions for many
 *   data types (Integer, Long, String, etc.) There are also functions that return a single value, and Optional value,
 *   or a List of values.</li>
 * </ul>
 *
 * <p>This mapper can be injected as-is into a MyBatis configuration, or it can be extended with existing mappers.
 *
 * @author Jeff Butler
 */
public interface CommonSelectMapper {
    /**
     * Select a single row as a Map of values. The row may have any number of columns.
     * The Map key will be the column name as returned from the
     * database (may be aliased if an alias is specified in the select statement). Map entries will be
     * of data types determined by the JDBC driver. MyBatis will call ResultSet.getObject() to retrieve
     * values from the ResultSet. Reference your JDBC driver documentation to learn about type mappings
     * for your specific database.
     *
     * @param selectStatement the select statement
     * @return A Map containing the row values.
     */
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    Map<String, Object> selectOneMappedRow(SelectStatementProvider selectStatement);

    /**
     * Select a single row of values and then convert the values to a custom type. This is similar
     * to the Spring JDBC template method of processing result sets. In this case, MyBatis will first extract
     * the row values into a Map, and then a row mapper can retrieve values from the Map and use them
     * to construct a custom object.
     *
     * <p>See {@link CommonSelectMapper#selectOneMappedRow(SelectStatementProvider)} for details about
     * how MyBatis will construct the Map of values.
     *
     * @param selectStatement the select statement
     * @param rowMapper a function that will convert a Map of row values to the desired data type
     * @param <R> the datatype of the converted object
     * @return the converted object
     */
    default <R> R selectOne(SelectStatementProvider selectStatement,
                            Function<Map<String, Object>, R> rowMapper) {
        return rowMapper.apply(selectOneMappedRow(selectStatement));
    }

    /**
     * Select any number of rows and return a List of Maps containing row values (one Map for each row returned).
     * The rows may have any number of columns.
     * The Map key will be the column name as returned from the
     * database (may be aliased if an alias is specified in the select statement). Map entries will be
     * of data types determined by the JDBC driver. MyBatis will call ResultSet.getObject() to retrieve
     * values from the ResultSet. Reference your JDBC driver documentation to learn about type mappings
     * for your specific database.
     *
     * @param selectStatement the select statement
     * @return A List of Maps containing the row values.
     */
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    List<Map<String, Object>> selectManyMappedRows(SelectStatementProvider selectStatement);

    /**
     * Select any number of rows and then convert the values to a custom type. This is similar to the
     * Spring JDBC template method of processing result sets. In this case, MyBatis will first extract the
     * row values into a List of Map, and them a row mapper can retrieve values from the Map and use them
     * to construct a custom object for each row.
     *
     * @param selectStatement the select statement
     * @param rowMapper a function that will convert a Map of row values to the desired data type
     * @param <R> the datatype of the converted object
     * @return the List of converted objects
     */
    default <R> List<R> selectMany(SelectStatementProvider selectStatement,
                                   Function<Map<String, Object>, R> rowMapper) {
        return selectManyMappedRows(selectStatement).stream()
                .map(rowMapper)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a single {@link java.math.BigDecimal} from a result set. The result set must have
     * only one column and one or zero rows. The column must be retrievable from the result set
     * via the ResultSet.getBigDecimal() method.
     *
     * @param selectStatement the select statement
     * @return the extracted value. May be null if zero rows are returned, or if the returned
     *     column is null
     */
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    BigDecimal selectOneBigDecimal(SelectStatementProvider selectStatement);

    /**
     * Retrieve a single {@link java.math.BigDecimal} from a result set. The result set must have
     * only one column and one or zero rows. The column must be retrievable from the result set
     * via the ResultSet.getBigDecimal() method.
     *
     * @param selectStatement the select statement
     * @return the extracted value. The Optional will be empty if zero rows are returned, or if the returned
     *     column is null
     */
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    Optional<BigDecimal> selectOptionalBigDecimal(SelectStatementProvider selectStatement);

    /**
     * Retrieve a List of {@link java.math.BigDecimal} from a result set. The result set must have
     * only one column, but can have any number of rows. The column must be retrievable from the result set
     * via the ResultSet.getBigDecimal() method.
     *
     * @param selectStatement the select statement
     * @return the list of extracted values. Any value may be null if a column in the result set is null
     */
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    List<BigDecimal> selectManyBigDecimals(SelectStatementProvider selectStatement);

    /**
     * Retrieve a single {@link java.lang.Double} from a result set. The result set must have
     * only one column and one or zero rows. The column must be retrievable from the result set
     * via the ResultSet.getDouble() method.
     *
     * @param selectStatement the select statement
     * @return the extracted value. May be null if zero rows are returned, or if the returned
     *     column is null
     */
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    Double selectOneDouble(SelectStatementProvider selectStatement);

    /**
     * Retrieve a single {@link java.lang.Double} from a result set. The result set must have
     * only one column and one or zero rows. The column must be retrievable from the result set
     * via the ResultSet.getDouble() method.
     *
     * @param selectStatement the select statement
     * @return the extracted value. The Optional will be empty if zero rows are returned, or if the returned
     *     column is null
     */
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    Optional<Double> selectOptionalDouble(SelectStatementProvider selectStatement);

    /**
     * Retrieve a List of {@link java.lang.Double} from a result set. The result set must have
     * only one column, but can have any number of rows. The column must be retrievable from the result set
     * via the ResultSet.getDouble() method.
     *
     * @param selectStatement the select statement
     * @return the list of extracted values. Any value may be null if a column in the result set is null
     */
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    List<Double> selectManyDoubles(SelectStatementProvider selectStatement);

    /**
     * Retrieve a single {@link java.lang.Integer} from a result set. The result set must have
     * only one column and one or zero rows. The column must be retrievable from the result set
     * via the ResultSet.getInt() method.
     *
     * @param selectStatement the select statement
     * @return the extracted value. May be null if zero rows are returned, or if the returned
     *     column is null
     */
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    Integer selectOneInteger(SelectStatementProvider selectStatement);

    /**
     * Retrieve a single {@link java.lang.Integer} from a result set. The result set must have
     * only one column and one or zero rows. The column must be retrievable from the result set
     * via the ResultSet.getInt() method.
     *
     * @param selectStatement the select statement
     * @return the extracted value. The Optional will be empty if zero rows are returned, or if the returned
     *     column is null
     */
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    Optional<Integer> selectOptionalInteger(SelectStatementProvider selectStatement);

    /**
     * Retrieve a List of {@link java.lang.Integer} from a result set. The result set must have
     * only one column, but can have any number of rows. The column must be retrievable from the result set
     * via the ResultSet.getInt() method.
     *
     * @param selectStatement the select statement
     * @return the list of extracted values. Any value may be null if a column in the result set is null
     */
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    List<Integer> selectManyIntegers(SelectStatementProvider selectStatement);

    /**
     * Retrieve a single {@link java.lang.Long} from a result set. The result set must have
     * only one column and one or zero rows. The column must be retrievable from the result set
     * via the ResultSet.getLong() method.
     *
     * @param selectStatement the select statement
     * @return the extracted value. May be null if zero rows are returned, or if the returned
     *     column is null
     */
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    Long selectOneLong(SelectStatementProvider selectStatement);

    /**
     * Retrieve a single {@link java.lang.Long} from a result set. The result set must have
     * only one column and one or zero rows. The column must be retrievable from the result set
     * via the ResultSet.getLong() method.
     *
     * @param selectStatement the select statement
     * @return the extracted value. The Optional will be empty if zero rows are returned, or if the returned
     *     column is null
     */
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    Optional<Long> selectOptionalLong(SelectStatementProvider selectStatement);

    /**
     * Retrieve a List of {@link java.lang.Long} from a result set. The result set must have
     * only one column, but can have any number of rows. The column must be retrievable from the result set
     * via the ResultSet.getLong() method.
     *
     * @param selectStatement the select statement
     * @return the list of extracted values. Any value may be null if a column in the result set is null
     */
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    List<Long> selectManyLongs(SelectStatementProvider selectStatement);

    /**
     * Retrieve a single {@link java.lang.String} from a result set. The result set must have
     * only one column and one or zero rows. The column must be retrievable from the result set
     * via the ResultSet.getString() method.
     *
     * @param selectStatement the select statement
     * @return the extracted value. May be null if zero rows are returned, or if the returned
     *     column is null
     */
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    String selectOneString(SelectStatementProvider selectStatement);

    /**
     * Retrieve a single {@link java.lang.String} from a result set. The result set must have
     * only one column and one or zero rows. The column must be retrievable from the result set
     * via the ResultSet.getString() method.
     *
     * @param selectStatement the select statement
     * @return the extracted value. The Optional will be empty if zero rows are returned, or if the returned
     *     column is null
     */
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    Optional<String> selectOptionalString(SelectStatementProvider selectStatement);

    /**
     * Retrieve a List of {@link java.lang.String} from a result set. The result set must have
     * only one column, but can have any number of rows. The column must be retrievable from the result set
     * via the ResultSet.getString() method.
     *
     * @param selectStatement the select statement
     * @return the list of extracted values. Any value may be null if a column in the result set is null
     */
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    List<String> selectManyStrings(SelectStatementProvider selectStatement);
}
