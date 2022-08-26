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
package org.mybatis.dynamic.sql.select.function;

import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.BindableColumn;

/**
 * Represents a function that can change the underlying type. For example, converting a binary field for a base64
 * string, or an integer to a string, etc.
 *
 * <p>Thanks to @endink for the idea.
 *
 * @author Jeff Butler
 *
 * @param <T> The type of the underlying column. For example, if a function converts a VARCHAR to an INT, then the
 *     underlying type will be a String
 * @param <R> The type of the column after the conversion. For example, if a function converts a VARCHAR to an INT, then
 *     the converted type will be Integer
 * @param <U> the specific subtype that implements the function
 */
public abstract class AbstractTypeConvertingFunction<T, R, U extends AbstractTypeConvertingFunction<T, R, U>>
        implements BindableColumn<R> {
    protected final BindableColumn<T> column;
    protected String alias;

    protected AbstractTypeConvertingFunction(BindableColumn<T> column) {
        this.column = Objects.requireNonNull(column);
    }

    @Override
    public Optional<String> alias() {
        return Optional.ofNullable(alias);
    }

    @Override
    public U as(String alias) {
        U newThing = copy();
        newThing.alias = alias;
        return newThing;
    }

    protected abstract U copy();
}
