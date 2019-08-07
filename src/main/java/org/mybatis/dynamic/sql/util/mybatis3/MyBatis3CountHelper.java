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

import java.util.function.Function;

import org.mybatis.dynamic.sql.select.MyBatis3SelectModelAdapter;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.util.Buildable;

/**
 * Represents a function that can be used to create a general count method in the style
 * of MyBatis Generator. When using this function, you can create a method that does not require a user to
 * call the build().execute() methods - making client code look a bit cleaner.
 * 
 * <p>For example, you can create mapper interface methods like this:
 * 
 * <pre>
 * &#64;SelectProvider(type=SqlProviderAdapter.class, method="select")
 * long count(SelectStatementProvider selectStatement);
 *   
 * default long count(MyBatis3CountHelper helper) {
 *     return helper.apply(SelectDSL.selectWithMapper(this::count, SqlBuilder.count())
 *             .from(simpleTable))
 *             .build()
 *             .execute();
 * }
 * </pre>
 * 
 * <p>And then call the simplified default method like this:
 * 
 * <pre>
 * long rows = mapper.count(q -&gt;
 *         q.where(occupation, isNull()));
 * </pre>
 * 
 * <p>You can implement a "count all" with the following code:
 * 
 * <pre>
 * long rows = mapper.count(q -&gt; q);
 * </pre>
 * 
 * <p>Or
 * 
 * <pre>
 * long rows = mapper.count(MyBatis3CountHelper.allRows());
 * </pre>
 *  
 * @author Jeff Butler
 */
@FunctionalInterface
public interface MyBatis3CountHelper extends
        Function<QueryExpressionDSL<MyBatis3SelectModelAdapter<Long>>, Buildable<MyBatis3SelectModelAdapter<Long>>> {
    
    /**
     * Returns a helper that can be used to count every row in a table.
     * 
     * @return the helper that will count every row in a table
     */
    static MyBatis3CountHelper allRows() {
        return h -> h;
    }
}
