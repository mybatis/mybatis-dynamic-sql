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

import org.mybatis.dynamic.sql.select.MyBatis3SelectModelAdapter;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.util.Buildable;

/**
 * Represents a function that can be used to create a "SelectByExample" method in the style
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
 * List&lt;SimpleTableRecord&gt; selectMany(SelectStatementProvider selectStatement);
 *
 * default List&lt;SimpleTableRecord&gt; selectByExample(MyBatis3SelectByExampleHelper&lt;SimpleTableRecord&gt; helper) {
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
 * List&lt;SimpleTableRecord&gt; rows = mapper.selectByExample(q -&gt;
 *     q.where(id, isEqualTo(1))
 *     .or(occupation, isNull()));
 * </pre>
 *  
 * <p>You can also do a "select all" with the following code:
 * 
 * <pre>
 * List&lt;SimpleTableRecord&gt; rows = mapper.selectByExample(q -&gt; q);
 * </pre>
 * 
 * @author Jeff Butler
 */
@FunctionalInterface
public interface MyBatis3SelectByExampleHelper<T> extends
        Function<QueryExpressionDSL<MyBatis3SelectModelAdapter<List<T>>>,
        Buildable<MyBatis3SelectModelAdapter<List<T>>>> {
}
