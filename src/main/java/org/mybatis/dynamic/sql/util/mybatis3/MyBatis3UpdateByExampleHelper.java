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
 * Represents a function that can be used to create an "UpdateByExample" method in the style
 * of MyBatis Generator. When using this function, you can create a method that does not require a user to
 * call the build().execute() methods - making client code look a bit cleaner.
 * 
 * <p>For example, you can create mapper interface methods like this:
 * 
 * <pre>
 * &#64;UpdateProvider(type=SqlProviderAdapter.class, method="update")
 * int update(UpdateStatementProvider updateStatement);
 *   
 * default int updateByExampleSelective(SimpleTableRecord record, MyBatis3UpdateByExampleHelper helper) {
 *     return helper.apply(UpdateDSL.updateWithMapper(this::update, simpleTable)
 *             .set(id).equalToWhenPresent(record.getId())
 *             .set(firstName).equalToWhenPresent(record::getFirstName)
 *             .set(lastName).equalToWhenPresent(record::getLastName)
 *             .set(birthDate).equalToWhenPresent(record::getBirthDate)
 *             .set(employed).equalToWhenPresent(record::getEmployed)
 *             .set(occupation).equalToWhenPresent(record::getOccupation))
 *             .build()
 *             .execute();
 * }
 * </pre>
 * 
 * <p>And then call the simplified default method like this:
 * 
 * <pre>
 * int rows = mapper.updateByExampleSelective(record, q -&gt;
 *                q.where(id, isEqualTo(100))
 *                .and(firstName, isEqualTo("Joe")));
 * </pre>
 *  
 * <p>You can also do an "update all" with the following code:
 * 
 * <pre>
 * int rows = mapper.updateByExampleSelective(record, q -&gt; q);
 * </pre>
 * 
 * @author Jeff Butler
 */
@FunctionalInterface
public interface MyBatis3UpdateByExampleHelper extends
        Function<UpdateDSL<MyBatis3UpdateModelAdapter<Integer>>, Buildable<MyBatis3UpdateModelAdapter<Integer>>> {
}
