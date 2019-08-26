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
package org.mybatis.dynamic.sql.select;

import java.util.function.Function;

import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils;

/**
 * Represents a function that can be used to create a general select method. When using this function,
 * you can create a method that does not require a user to call the build() and render() methods - making
 * client code look a bit cleaner.
 * 
 * <p>This function is intended to by used in conjunction with utility methods like the select methods in
 * {@link MyBatis3Utils}.
 * 
 * <p>For example, you can create mapper interface methods like this:
 * 
 * <pre>
 * &#64;SelectProvider(type=SqlProviderAdapter.class, method="select")
 * List&lt;PersonRecord&gt; selectMany(SelectStatementProvider selectStatement);
 *   
 * BasicColumn[] selectList =
 *     BasicColumn.columnList(id, firstName, lastName, birthDate, employed, occupation, addressId);
 * 
 * default List&lt;PersonRecord&gt; select(SelectDSLCompleter completer) {
 *      return MyBatis3Utils.select(this::selectMany, selectList, person, completer);
 * }
 * </pre>
 * 
 * <p>And then call the simplified default method like this:
 * 
 * <pre>
 * List&lt;PersonRecord&gt; rows = mapper.select(c -&gt;
 *         c.where(occupation, isNull()));
 * </pre>
 * 
 * <p>You can implement a "select all" with the following code:
 * 
 * <pre>
 * List&lt;PersonRecord&gt; rows = mapper.select(c -&gt; c);
 * </pre>
 * 
 * <p>Or
 * 
 * <pre>
 * List&lt;PersonRecord&gt; rows = mapper.select(SelectDSLCompleter.allRows());
 * </pre>
 *
 * <p>There is also a utility method to support selecting all rows in a specified order:
 * 
 * <pre>
 * List&lt;PersonRecord&gt; rows = mapper.select(SelectDSLCompleter.allRowsOrderedBy(lastName, firstName));
 * </pre>
 * 
 * @author Jeff Butler
 */
@FunctionalInterface
public interface SelectDSLCompleter extends
        Function<QueryExpressionDSL<SelectModel>, Buildable<SelectModel>> {
    
    /**
     * Returns a completer that can be used to select every row in a table.
     * 
     * @return the completer that will select every row in a table
     */
    static SelectDSLCompleter allRows() {
        return c -> c;
    }

    /**
     * Returns a completer that can be used to select every row in a table with specified order.
     * 
     * @param columns list of sort specifications for an order by clause
     * @return the completer that will select every row in a table with specified order
     */
    static SelectDSLCompleter allRowsOrderedBy(SortSpecification...columns) {
        return c -> c.orderBy(columns);
    }
}
