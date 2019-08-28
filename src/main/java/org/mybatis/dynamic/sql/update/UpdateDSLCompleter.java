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
package org.mybatis.dynamic.sql.update;

import java.util.function.Function;
import java.util.function.ToIntFunction;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils;

/**
 * Represents a function that can be used to create a general update method. When using this function,
 * you can create a method that does not require a user to call the build() and render() methods - making
 *  client code look a bit cleaner.
 * 
 * <p>This function is intended to be used in conjunction in the utility method like
 *  {@link MyBatis3Utils#update(ToIntFunction, SqlTable, UpdateDSLCompleter)}
 * 
 * <p>For example, you can create mapper interface methods like this:
 * 
 * <pre>
 * &#64;UpdateProvider(type=SqlProviderAdapter.class, method="update")
 * int update(UpdateStatementProvider updateStatement);
 *   
 * default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, person, completer);
 * }
 * </pre>
 * 
 * <p>And then call the simplified default method like this:
 * 
 * <pre>
 * int rows = mapper.update(c -&gt;
 *                c.set(firstName).equalTo("Fred")
 *                .where(id, isEqualTo(100))
 *            );
 * </pre>
 *  
 * <p>You can implement an "update all" simply by omitting a where clause:
 * 
 * <pre>
 * int rows = mapper.update(c -&gt;
 *                c.set(firstName).equalTo("Fred")
 *            );
 * </pre>
 * 
 * <p>You could also implement a helper method that would set fields based on values of a record. For example,
 * the following method would set all fields of a record based on whether or not the values are null:
 * 
 * <pre>
 * static UpdateDSL&lt;UpdateModel&gt; updateSelectiveColumns(PersonRecord record,
 *         UpdateDSL&lt;UpdateModel&gt; dsl) {
 *     return dsl.set(id).equalToWhenPresent(record::getId)
 *             .set(firstName).equalToWhenPresent(record::getFirstName)
 *             .set(lastName).equalToWhenPresent(record::getLastName)
 *             .set(birthDate).equalToWhenPresent(record::getBirthDate)
 *             .set(employed).equalToWhenPresent(record::getEmployed)
 *             .set(occupation).equalToWhenPresent(record::getOccupation);
 * }
 * </pre>
 * 
 * <p>The helper method could be used like this:
 * 
 * <pre>
 * rows = mapper.update(c -&gt;
 *        PersonMapper.updateSelectiveColumns(record, c)
 *        .where(id, isLessThan(100)));
 * </pre>
 * 
 * <p>In this way, you could mimic the function of the old style "updateByExampleSelective" methods from
 * MyBatis Generator.
 * 
 * @author Jeff Butler
 */
@FunctionalInterface
public interface UpdateDSLCompleter extends
        Function<UpdateDSL<UpdateModel>, Buildable<UpdateModel>> {
}
