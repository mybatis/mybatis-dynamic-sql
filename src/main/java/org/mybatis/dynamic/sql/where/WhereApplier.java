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
package org.mybatis.dynamic.sql.where;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion;
import org.mybatis.dynamic.sql.CriteriaGroup;
import org.mybatis.dynamic.sql.ExistsCriterion;
import org.mybatis.dynamic.sql.ExistsPredicate;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionDSL;

@FunctionalInterface
public interface WhereApplier {

    void accept(AbstractWhereFinisher<?> whereFinisher);

    /**
     * Return a composed where applier that performs this operation followed by the after operation.
     *
     * @param after the operation to perform after this operation
     * @return a composed where applier that performs this operation followed by the after operation.
     */
    default WhereApplier andThen(Consumer<AbstractBooleanExpressionDSL<?>> after) {
        return t -> {
            accept(t);
            after.accept(t);
        };
    }

    @NotNull
    static <T> WhereApplier where(BindableColumn<T> column, VisitableCondition<T> condition,
                                  AndOrCriteriaGroup... subCriteria) {
        return where(column, condition, Arrays.asList(subCriteria));
    }

    @NotNull
    static <T> WhereApplier where(BindableColumn<T> column, VisitableCondition<T> condition,
                                  List<AndOrCriteriaGroup> subCriteria) {
        ColumnAndConditionCriterion<T> ic = ColumnAndConditionCriterion.withColumn(column)
                .withCondition(condition)
                .withSubCriteria(subCriteria)
                .build();

        return d -> d.initialize(ic);
    }

    @NotNull
    static WhereApplier where(ExistsPredicate existsPredicate, AndOrCriteriaGroup... subCriteria) {
        return where(existsPredicate, Arrays.asList(subCriteria));
    }

    @NotNull
    static WhereApplier where(ExistsPredicate existsPredicate, List<AndOrCriteriaGroup> subCriteria) {
        ExistsCriterion ic = new ExistsCriterion.Builder()
                .withExistsPredicate(existsPredicate)
                .withSubCriteria(subCriteria)
                .build();

        return d -> d.initialize(ic);
    }

    @NotNull
    static WhereApplier where(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return where(initialCriterion, Arrays.asList(subCriteria));
    }

    @NotNull
    static WhereApplier where(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        CriteriaGroup ic = new CriteriaGroup.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build();

        return d -> d.initialize(ic);
    }

    @NotNull
    static WhereApplier where(List<AndOrCriteriaGroup> criteria) {
        CriteriaGroup ic = new CriteriaGroup.Builder()
                .withSubCriteria(criteria)
                .build();

        return d -> d.initialize(ic);
    }
}
