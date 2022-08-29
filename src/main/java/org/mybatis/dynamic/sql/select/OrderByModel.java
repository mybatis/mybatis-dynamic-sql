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
package org.mybatis.dynamic.sql.select;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.exception.InvalidSqlException;
import org.mybatis.dynamic.sql.util.Messages;

public class OrderByModel {
    private final List<SortSpecification> columns = new ArrayList<>();

    private OrderByModel(Collection<SortSpecification> columns) {
        Objects.requireNonNull(columns);
        if (columns.isEmpty()) {
            throw new InvalidSqlException(Messages.getString("ERROR.12")); //$NON-NLS-1$
        }
        this.columns.addAll(columns);
    }

    public <R> Stream<R> mapColumns(Function<SortSpecification, R> mapper) {
        return columns.stream().map(mapper);
    }

    public static OrderByModel of(Collection<SortSpecification> columns) {
        return new OrderByModel(columns);
    }
}
