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
package org.mybatis.dynamic.sql.delete;

import java.util.function.Function;
import java.util.function.ToIntFunction;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils;

/**
 * Represents a function that can be used to create a simplified delete method. When using this function
 * you can create a method that does not require a user to call the build() and render() methods - making
 *  client code look a bit cleaner.
 * 
 * <p>This function is intended to be used in conjunction with a utility method like
 *  {@link MyBatis3Utils#deleteFrom(ToIntFunction, SqlTable, DeleteDSLCompleter)}
 * 
 * <p>For example, you can create mapper interface methods like this:
 * 
 * <pre>
 * &#64;DeleteProvider(type=SqlProviderAdapter.class, method="delete")
 * int delete(DeleteStatementProvider deleteStatement);
 *   
 * default int delete(DeleteDSLCompleter completer) {
 *     return MyBatis3Utils.deleteFrom(this::delete, person, completer);
 * }
 * </pre>
 * 
 * <p>And then call the simplified default method like this:
 * 
 * <pre>
 * int rows = mapper.delete(c -&gt;
 *           c.where(occupation, isNull()));
 * </pre>
 *  
 * <p>You can implement a "delete all" with the following code:
 * 
 * <pre>
 * int rows = mapper.delete(c -&gt; c);
 * </pre>
 * 
 * <p>Or
 * 
 * <pre>
 * long rows = mapper.delete(DeleteDSLCompleter.allRows());
 * </pre>

 * @author Jeff Butler
 */
@FunctionalInterface
public interface DeleteDSLCompleter extends
        Function<DeleteDSL<DeleteModel>, Buildable<DeleteModel>> {

    /**
     * Returns a completer that can be used to delete every row in a table.
     * 
     * @return the completer that will delete every row in a table
     */
    static DeleteDSLCompleter allRows() {
        return c -> c;
    }
}
