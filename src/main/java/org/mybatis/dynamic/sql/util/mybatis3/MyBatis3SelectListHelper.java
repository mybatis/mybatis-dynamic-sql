/**
 *    Copyright 2016-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.util.mybatis3;

import java.util.List;
import java.util.function.Function;

import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.select.CompletableQuery;
import org.mybatis.dynamic.sql.select.MyBatis3SelectModelAdapter;
import org.mybatis.dynamic.sql.util.Buildable;

/**
 * Represents a function that can be used to create a general select method in the style
 * of MyBatis Generator. When using this function, you can create a method that does not require a user to
 * call the build().execute() methods - making client code look a bit cleaner.
 * 
 * <p>For example, you can create mapper interface methods like this:
 * 
 * <pre>
 * &#64;SelectProvider(type=SqlProviderAdapter.class, method="select")
 * &#64;Results(id="SimpleTableResult", value= {
 *         &#64;Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
 *         &#64;Result(column="first_name", property="firstName", jdbcType=JdbcType.VARCHAR),
 *         &#64;Result(column="last_name", property="lastName", jdbcType=JdbcType.VARCHAR),
 *         &#64;Result(column="birth_date", property="birthDate", jdbcType=JdbcType.DATE),
 *         &#64;Result(column="employed", property="employed", jdbcType=JdbcType.VARCHAR),
 *         &#64;Result(column="occupation", property="occupation", jdbcType=JdbcType.VARCHAR)
 * })
 * List&lt;SimpleRecord&gt; selectMany(SelectStatementProvider selectStatement);
 *
 * default List&lt;SimpleRecord&gt; select(MyBatis3SelectHelper&lt;SimpleRecord&gt; helper) {
 *     return helper.apply(SelectDSL.selectWithMapper(this::selectMany, simpleTable.allColumns())
 *             .from(simpleTable))
 *             .build()
 *             .execute();
 * }
 * </pre>
 * 
 * <p>And then call the simplified default method like this:
 * 
 * <pre>
 * List&lt;SimpleRecord&gt; rows = mapper.select(q -&gt;
 *     q.where(id, isEqualTo(1))
 *     .or(occupation, isNull()));
 * </pre>
 *  
 * <p>You can implement a "select all" with the following code:
 * 
 * <pre>
 * List&lt;SimpleRecord&gt; rows = mapper.select(q -&gt; q);
 * </pre>
 * 
 * <p>Or
 * 
 * <pre>
 * List&lt;SimpleRecord&gt; rows = mapper.select(MyBatis3SelectHelper.allRows());
 * </pre>
 * 
 * @author Jeff Butler
 */
@FunctionalInterface
public interface MyBatis3SelectListHelper<T> extends
        Function<CompletableQuery<MyBatis3SelectModelAdapter<List<T>>>,
        Buildable<MyBatis3SelectModelAdapter<List<T>>>> {

    /**
     * Returns a helper that can be used to select every row in a table.
     * 
     * @param <T> the type of row returned
     * 
     * @return the helper that will select every row in a table
     */
    static <T> MyBatis3SelectListHelper<T> allRows() {
        return h -> h;
    }

    /**
     * Returns a helper that can be used to select every row in a table with a specified sort order.
     * 
     * @param <T> the type of row returned
     * @param columns sort columns
     * 
     * @return the helper that will select every row in a table in the specified order
     */
    static <T> MyBatis3SelectListHelper<T> allRowsOrderdBy(SortSpecification...columns) {
        return h -> h.orderBy(columns);
    }
}
