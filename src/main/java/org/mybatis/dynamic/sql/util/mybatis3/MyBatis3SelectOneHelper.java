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

import java.util.Optional;
import java.util.function.Function;

import org.mybatis.dynamic.sql.select.MyBatis3SelectModelAdapter;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.util.Buildable;

/**
 * Represents a function that can be used to create a general select one method in the style
 * of MyBatis Generator. When using this function, you can create a method that does not require a user to
 * call the build().execute() methods - making client code look a bit cleaner.
 * 
 * <p>For example, you can create mapper interface methods like this:
 * 
 * <pre>
 * &#64;SelectProvider(type=SqlProviderAdapter.class, method="select")
 * &#64;ResultMap("SimpleTableResult")
 * Optional&lt;SimpleTableRecord&gt; selectOne(SelectStatementProvider selectStatement);
 *
 * default Optional&lt;SimpleTableRecord&gt; selectOne(MyBatis3SelectOneHelper&lt;SimpleTableRecord&gt; helper) {
 *     return helper.apply(SelectDSL.selectWithMapper(this::selectOne, simpleTable.allColumns())
 *             .from(simpleTable))
 *             .build()
 *             .execute();
 * }
 * </pre>
 * 
 * <p>And then call the simplified default method like this:
 * 
 * <pre>
 * Optional&lt;SimpleRecord&gt; record = mapper.selectOne(q -&gt;
 *     q.where(id, isEqualTo(1)));
 * </pre>
 *  
 * @author Jeff Butler
 */
@FunctionalInterface
public interface MyBatis3SelectOneHelper<T> extends
        Function<QueryExpressionDSL<MyBatis3SelectModelAdapter<Optional<T>>>,
        Buildable<MyBatis3SelectModelAdapter<Optional<T>>>> {
}
