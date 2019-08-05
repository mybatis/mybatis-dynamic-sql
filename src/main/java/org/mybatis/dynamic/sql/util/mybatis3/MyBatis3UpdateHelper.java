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

import org.mybatis.dynamic.sql.update.MyBatis3UpdateModelAdapter;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.util.Buildable;

/**
 * Represents a function that can be used to create a general update method in the style
 * of MyBatis Generator. When using this function, you can create a method that does not require a user to
 * call the build().execute() methods - making client code look a bit cleaner.
 * 
 * <p>For example, you can create mapper interface methods like this:
 * 
 * <pre>
 * &#64;UpdateProvider(type=SqlProviderAdapter.class, method="update")
 * int update(UpdateStatementProvider updateStatement);
 *   
 * default int update(MyBatis3UpdateHelper helper) {
 *     return helper.apply(UpdateDSL.updateWithMapper(this::update, simpleTable))
 *             .build()
 *             .execute();
 * }
 * </pre>
 * 
 * <p>And then call the simplified default method like this:
 * 
 * <pre>
 * int rows = mapper.update(q -&gt;
 *                q.set(firstName).equalTo("Fred")
 *                .where(id, isEqualTo(100))
 *            );
 * </pre>
 *  
 * <p>You can implement an "update all" simply by omitting a where clause:
 * 
 * <pre>
 * int rows = mapper.update(q -&gt;
 *                q.set(firstName).equalTo("Fred")
 *            );
 * </pre>
 * 
 * <p>You could also implement a helper method that would set fields based on values of a record. For example,
 * the following method would set all fields of a record based on whether or not the values are null:
 * 
 * <pre>
 * static UpdateDSL&lt;MyBatis3UpdateModelAdapter&lt;Integer&gt;&gt; setSelective(SimpleTableRecord record,
 *         UpdateDSL&lt;MyBatis3UpdateModelAdapter&lt;Integer&gt;&gt; dsl) {
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
 * rows = mapper.update(dsl -&gt;
 *        SimpleTableAnnotatedMapperNewStyle.setSelective(record, dsl)
 *        .where(id, isLessThan(100)));
 * </pre>
 * 
 * <p>In this way, you could mimic the function of the old style "updateByExampleSelective" methods from
 * MyBatis Generator.
 * 
 * @author Jeff Butler
 */
@FunctionalInterface
public interface MyBatis3UpdateHelper extends
        Function<UpdateDSL<MyBatis3UpdateModelAdapter<Integer>>, Buildable<MyBatis3UpdateModelAdapter<Integer>>> {
}
