/*
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
package org.mybatis.dynamic.sql.where;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.VisitableCondition;

/**
 * Base class for DSLs that support where clauses.
 *
 * @param <W> the implementation of the Where DSL.
 */
public abstract class AbstractWhereSupportingDSL<W extends AbstractWhereDSL<?>> {

    public abstract W where();

    public <T> W where(BindableColumn<T> column, VisitableCondition<T> condition, SqlCriterion<?>...subCriteria) {
        return where(column, condition, Arrays.asList(subCriteria));
    }

    public <T> W where(BindableColumn<T> column, VisitableCondition<T> condition, List<SqlCriterion<?>> subCriteria) {
        return apply(w -> w.where(column, condition, subCriteria));
    }

    public W applyWhere(WhereApplier whereApplier) {
        return apply(w -> w.applyWhere(whereApplier));
    }

    private W apply(Consumer<W> block) {
        W dsl = where();
        block.accept(dsl);
        return dsl;
    }
}
