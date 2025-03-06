/*
 *    Copyright 2016-2025 the original author or authors.
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

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion;
import org.mybatis.dynamic.sql.CriteriaGroup;
import org.mybatis.dynamic.sql.ExistsCriterion;
import org.mybatis.dynamic.sql.ExistsPredicate;
import org.mybatis.dynamic.sql.RenderableCondition;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.util.ConfigurableStatement;

/**
 * Base class for DSLs that support where clauses - which is every DSL except Insert.
 * The purpose of the class is to provide a common set of where methods that can be used by
 * any statement.
 *
 * @param <F> the implementation of the Where DSL customized for a particular SQL statement.
 */
public interface AbstractWhereStarter<F extends AbstractWhereFinisher<?>, D extends AbstractWhereStarter<F, D>>
        extends ConfigurableStatement<D> {

    default <T> F where(BindableColumn<T> column, RenderableCondition<T> condition, AndOrCriteriaGroup... subCriteria) {
        return where(column, condition, Arrays.asList(subCriteria));
    }

    default <T> F where(BindableColumn<T> column, RenderableCondition<T> condition,
                       List<AndOrCriteriaGroup> subCriteria) {
        SqlCriterion sqlCriterion = ColumnAndConditionCriterion.withColumn(column)
                .withCondition(condition)
                .withSubCriteria(subCriteria)
                .build();

        return initialize(sqlCriterion);
    }

    default F where(ExistsPredicate existsPredicate, AndOrCriteriaGroup... subCriteria) {
        return where(existsPredicate, Arrays.asList(subCriteria));
    }

    default F where(ExistsPredicate existsPredicate, List<AndOrCriteriaGroup> subCriteria) {
        ExistsCriterion sqlCriterion = new ExistsCriterion.Builder()
                .withExistsPredicate(existsPredicate)
                .withSubCriteria(subCriteria)
                .build();

        return initialize(sqlCriterion);
    }

    default F where(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return where(initialCriterion, Arrays.asList(subCriteria));
    }

    default F where(@Nullable SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        SqlCriterion sqlCriterion = new CriteriaGroup.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build();

        return initialize(sqlCriterion);
    }

    default F where(List<AndOrCriteriaGroup> subCriteria) {
        SqlCriterion sqlCriterion = new CriteriaGroup.Builder()
                .withSubCriteria(subCriteria)
                .build();

        return initialize(sqlCriterion);
    }

    F where();

    default F applyWhere(WhereApplier whereApplier) {
        F finisher = where();
        whereApplier.accept(finisher);
        return finisher;
    }

    private F initialize(SqlCriterion sqlCriterion) {
        F finisher = where();
        finisher.initialize(sqlCriterion);
        return finisher;
    }
}
