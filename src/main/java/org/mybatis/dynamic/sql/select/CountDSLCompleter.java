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
import java.util.function.ToLongFunction;

import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils;

/**
 * Represents a function that can be used to create a general count method. When using this function,
 * you can create a method that does not require a user to call the build() and render() methods - making
 * client code look a bit cleaner.
 *
 * <p>This function is intended to by used in conjunction with a utility method like
 * {@link MyBatis3Utils#count(ToLongFunction, CountDSL, CountDSLCompleter)}
 *
 * <p>For example, you can create mapper interface methods like this:
 *
 * <pre>
 * &#64;SelectProvider(type=SqlProviderAdapter.class, method="select")
 * long count(SelectStatementProvider selectStatement);
 *
 * default long count(CountDSLCompleter completer) {
 *     return MyBatis3Utils.count(this::count, person, completer);
 * } 
 * </pre>
 *
 * <p>And then call the simplified default method like this:
 *
 * <pre>
 * long rows = mapper.count(c -&gt;
 *         c.where(occupation, isNull()));
 * </pre>
 *
 * <p>You can implement a "count all" with the following code:
 *
 * <pre>
 * long rows = mapper.count(c -&gt; c);
 * </pre>
 *
 * <p>Or
 *
 * <pre>
 * long rows = mapper.count(CountDSLCompleter.allRows());
 * </pre>
 *
 * @author Jeff Butler
 */
@FunctionalInterface
public interface CountDSLCompleter extends
        Function<CountDSL<SelectModel>, Buildable<SelectModel>> {
    
    /**
     * Returns a completer that can be used to count every row in a table.
     * 
     * @return the completer that will count every row in a table
     */
    static CountDSLCompleter allRows() {
        return c -> c;
    }
}
