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
package org.mybatis.dynamic.sql.select;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.util.Validator;

public class GroupByModel {
    private final List<BasicColumn> columns = new ArrayList<>();

    private GroupByModel(Collection<? extends BasicColumn> columns) {
        Objects.requireNonNull(columns);
        Validator.assertNotEmpty(columns, "ERROR.11"); //$NON-NLS-1$
        this.columns.addAll(columns);
    }

    public Stream<BasicColumn> columns() {
        return columns.stream();
    }

    public static GroupByModel of(Collection<? extends BasicColumn> columns) {
        return new GroupByModel(columns);
    }
}
