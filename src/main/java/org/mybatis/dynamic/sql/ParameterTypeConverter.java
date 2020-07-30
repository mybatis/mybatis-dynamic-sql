/**
 *    Copyright 2016-2020 the original author or authors.
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
package org.mybatis.dynamic.sql;

/**
 * A parameter type converter is used to change a parameter value from one type to another
 * during statement rendering before the parameter is placed into the parameter map. This can be used
 * to somewhat mimic the function of a MyBatis type handler for runtimes such as Spring that don't have
 * a corresponding concept.
 *
 * <p>A parameter type converter is associated with a SqlColumn.
 * 
 * <p>A parameter type converter is compatible with Spring's general Converter interface so existing converters
 * can be reused here if they are marked with this additional interface.
 *
 * <p>The converter is only used for parameters - it is not used for result set processing. The converter will be
 * called in the following circumstances:
 *
 * <ul>
 *     <li>Parameters in a general insert statement</li>
 *     <li>Parameters in an update statement</li>
 *     <li>Parameters in a where clause in any statement</li>
 * </ul>
 * 
 * @param <S> Source Type
 *
 * @see SqlColumn
 * @author Jeff Butler
 */
@FunctionalInterface
public interface ParameterTypeConverter<S> {
    Object convert(S source);
}
