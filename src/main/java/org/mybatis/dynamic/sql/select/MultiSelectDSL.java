/*
 *    Copyright 2016-2023 the original author or authors.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.util.Buildable;

public class MultiSelectDSL implements Buildable<MultiSelectModel> {
    private final List<UnionQuery> unionQueries = new ArrayList<>();
    private final SelectModel initialSelect;
    private OrderByModel orderByModel;
    private Long limit;
    private Long offset;
    private Long fetchFirstRows;

    public MultiSelectDSL(Buildable<SelectModel> builder) {
        initialSelect = builder.build();
    }

    public MultiSelectDSL union(Buildable<SelectModel> builder) {
        unionQueries.add(new UnionQuery("union", builder.build())); //$NON-NLS-1$
        return this;
    }

    public MultiSelectDSL unionAll(Buildable<SelectModel> builder) {
        unionQueries.add(new UnionQuery("union all", builder.build())); //$NON-NLS-1$
        return this;
    }

    public MultiSelectDSL orderBy(SortSpecification... columns) {
        return orderBy(Arrays.asList(columns));
    }

    public MultiSelectDSL orderBy(Collection<SortSpecification> columns) {
        orderByModel = OrderByModel.of(columns);
        return this;
    }

    public LimitFinisher limit(long limit) {
        this.limit = limit;
        return new LimitFinisher();
    }

    public OffsetFirstFinisher offset(long offset) {
        this.offset = offset;
        return new OffsetFirstFinisher();
    }

    public FetchFirstFinisher fetchFirst(long fetchFirstRows) {
        this.fetchFirstRows = fetchFirstRows;
        return new FetchFirstFinisher();
    }

    @NotNull
    @Override
    public MultiSelectModel build() {
        return new MultiSelectModel.Builder()
                .withInitialSelect(initialSelect)
                .withUnionQueries(unionQueries)
                .withOrderByModel(orderByModel)
                .withPagingModel(buildPagingModel())
                .build();
    }

    private PagingModel buildPagingModel() {
        if (limit == null && offset == null && fetchFirstRows == null) {
            return null;
        }

        return new PagingModel.Builder()
                .withLimit(limit)
                .withOffset(offset)
                .withFetchFirstRows(fetchFirstRows)
                .build();
    }

    public class LimitFinisher implements Buildable<MultiSelectModel> {
        public OffsetFinisher offset(long offset) {
            MultiSelectDSL.this.offset(offset);
            return new OffsetFinisher();
        }

        @NotNull
        @Override
        public MultiSelectModel build() {
            return MultiSelectDSL.this.build();
        }
    }

    public class OffsetFinisher implements Buildable<MultiSelectModel> {
        @NotNull
        @Override
        public MultiSelectModel build() {
            return MultiSelectDSL.this.build();
        }
    }

    public class OffsetFirstFinisher implements Buildable<MultiSelectModel> {
        public FetchFirstFinisher fetchFirst(long fetchFirstRows) {
            MultiSelectDSL.this.fetchFirst(fetchFirstRows);
            return new FetchFirstFinisher();
        }

        @NotNull
        @Override
        public MultiSelectModel build() {
            return MultiSelectDSL.this.build();
        }
    }

    public class FetchFirstFinisher {
        public RowsOnlyFinisher rowsOnly() {
            return new RowsOnlyFinisher();
        }
    }

    public class RowsOnlyFinisher implements Buildable<MultiSelectModel> {
        @NotNull
        @Override
        public MultiSelectModel build() {
            return MultiSelectDSL.this.build();
        }
    }
}
