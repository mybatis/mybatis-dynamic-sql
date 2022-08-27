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
package org.mybatis.dynamic.sql.where;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.ExistsPredicate;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.util.ConfigurableStatement;

/**
 * Base class for DSLs that support where clauses - which is every DSL except Insert.
 * The purpose of the class is to provide an implementation of the {@link AbstractWhereDSL}
 * that is customized for a particular DSL, and to add the initiating common "where"
 * methods.
 *
 * @param <W> the implementation of the Where DSL customized for a particular SQL statement.
 */
public abstract class AbstractWhereSupport<W extends AbstractWhereDSL<?>, D extends AbstractWhereSupport<W, D>>
        implements ConfigurableStatement<D> {

    public abstract W where();

    public <T> W where(BindableColumn<T> column, VisitableCondition<T> condition, AndOrCriteriaGroup... subCriteria) {
        return where(column, condition, Arrays.asList(subCriteria));
    }

    public <T> W where(BindableColumn<T> column, VisitableCondition<T> condition,
                       List<AndOrCriteriaGroup> subCriteria) {
        return apply(w -> w.where(column, condition, subCriteria));
    }

    public W where(ExistsPredicate existsPredicate, AndOrCriteriaGroup... subCriteria) {
        return where(existsPredicate, Arrays.asList(subCriteria));
    }

    public W where(ExistsPredicate existsPredicate, List<AndOrCriteriaGroup> subCriteria) {
        return apply(w -> w.where(existsPredicate, subCriteria));
    }

    public W where(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return where(initialCriterion, Arrays.asList(subCriteria));
    }

    public W where(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        return apply(w -> w.where(initialCriterion, subCriteria));
    }

    public W where(List<AndOrCriteriaGroup> subCriteria) {
        return apply(w -> w.where(subCriteria));
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
