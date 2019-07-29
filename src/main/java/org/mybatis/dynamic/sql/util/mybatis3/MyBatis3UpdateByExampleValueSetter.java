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

import java.util.function.BiFunction;

import org.mybatis.dynamic.sql.update.MyBatis3UpdateModelAdapter;
import org.mybatis.dynamic.sql.update.UpdateDSL;

/**
 * Represents a function that can be used to set values in an "UpdateByExample" method in the style
 * of MyBatis Generator. When using this function, you can create a method that will map record fields to
 * tables columns to be updated in a common mapper, and then allow a user to set a where clause as needed.
 * 
 * @author Jeff Butler
 *
 * @see MyBatis3UpdateByExampleHelper
 * 
 * @param <T> the type of record that will be updated
 */
@FunctionalInterface
public interface MyBatis3UpdateByExampleValueSetter<T> extends
        BiFunction<T, UpdateDSL<MyBatis3UpdateModelAdapter<Integer>>, UpdateDSL<MyBatis3UpdateModelAdapter<Integer>>> {
}
