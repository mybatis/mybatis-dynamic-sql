/*
 *    Copyright 2016-2024 the original author or authors.
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
package org.mybatis.dynamic.sql.util.spring;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;

/**
 * Utility class for converting a list of rows to an array or SqlParameterSources.
 *
 * <p>This class is necessary due to the way that the library generates bindings for batch insert
 * statements. The bindings will be of the form <code>:row.propertyName</code>. The <code>createBatch</code> method
 * in this class will wrap all input rows in a class - RowHolder - with a single property named "row".
 * This will allow the generated bindings to function properly with a Spring batch insert.
 */
public class BatchInsertUtility {
    private BatchInsertUtility() {}

    public static <T> SqlParameterSource[] createBatch(List<T> rows) {
        List<RowHolder<T>> tt = rows.stream()
                .map(RowHolder::new)
                .collect(Collectors.toList());

        return SqlParameterSourceUtils.createBatch(tt);
    }

    public static class RowHolder<T> {
        private final T row;

        public RowHolder(T row) {
            this.row = row;
        }

        public T getRow() {
            return row;
        }
    }
}
