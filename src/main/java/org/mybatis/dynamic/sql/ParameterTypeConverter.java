/*
 *    Copyright 2016-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql;

/**
 * A parameter type converter is used to change a parameter value from one type to another
 * during statement rendering and before the parameter is placed into the parameter map. This can be used
 * to somewhat mimic the function of a MyBatis type handler for runtimes such as Spring that don't have
 * a corresponding concept.
 *
 * <p>Since Spring does not have the concept of type handlers, it is a best practice to only use
 * Java data types that have a clear correlation to SQL data types (for example Java String correlates
 * automatically with VARCHAR). Using a parameter type converter will allow you to use data types in your
 * model classes that would otherwise be difficult to use with Spring.
 *
 * <p>A parameter type converter is associated with a SqlColumn.
 *
 * <p>This interface is based on Spring's general Converter interface and is intentionally compatible with it.
 * Existing converters may be reused if they are marked with this additional interface.
 *
 * <p>The converter is only used for parameters in a parameter map. It is not used for result set processing.
 * It is also not used for insert statements that are based on an external record class. The converter will be called
 * in the following circumstances:
 *
 * <ul>
 *     <li>Parameters in a general insert statement (for the Value and ValueWhenPresent mappings)</li>
 *     <li>Parameters in an update statement (for the Value and ValueWhenPresent mappings)</li>
 *     <li>Parameters in a where clause in any statement (for conditions that accept a value or multiple values)</li>
 * </ul>
 *
 * @param <S> Source Type
 * @param <T> Target Type
 *
 * @see SqlColumn
 * @author Jeff Butler
 * @since 1.1.5
 */
@FunctionalInterface
public interface ParameterTypeConverter<S, T> {
    T convert(S source);
}
