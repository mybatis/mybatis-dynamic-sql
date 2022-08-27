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

import java.sql.JDBCType;
import java.util.Optional;

import org.mybatis.dynamic.sql.render.RenderingStrategy;

/**
 * Describes additional attributes of columns that are necessary for binding the column as a JDBC parameter.
 * Columns in where clauses are typically bound.
 *
 * @author Jeff Butler
 *
 * @param <T>
 *            - the Java type that corresponds to this column
 */
public interface BindableColumn<T> extends BasicColumn {

    /**
     * Override the base method definition to make it more specific to this interface.
     */
    @Override
    BindableColumn<T> as(String alias);

    default Optional<JDBCType> jdbcType() {
        return Optional.empty();
    }

    default Optional<String> typeHandler() {
        return Optional.empty();
    }

    default Optional<RenderingStrategy> renderingStrategy() {
        return Optional.empty();
    }

    default Object convertParameterType(T value) {
        return value;
    }

    default Optional<Class<T>> javaType() {
        return Optional.empty();
    }
}
