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

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion;
import org.mybatis.dynamic.sql.CriteriaGroup;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionDSL;

@FunctionalInterface
public interface HavingApplier {

    void accept(AbstractBooleanExpressionDSL<?> havingStarter);

    /**
     * Return a composed having applier that performs this operation followed by the after operation.
     *
     * @param after the operation to perform after this operation
     *
     * @return a composed having applier that performs this operation followed by the after operation.
     */
    default HavingApplier andThen(Consumer<AbstractBooleanExpressionDSL<?>> after) {
        return t -> {
            accept(t);
            after.accept(t);
        };
    }

    static <T> HavingApplier having(BindableColumn<T> column, VisitableCondition<T> condition,
                        AndOrCriteriaGroup... subCriteria) {
        return having(column, condition, Arrays.asList(subCriteria));
    }

    static <T> HavingApplier having(BindableColumn<T> column, VisitableCondition<T> condition,
                        List<AndOrCriteriaGroup> subCriteria) {
        ColumnAndConditionCriterion<T> initialCriterion = ColumnAndConditionCriterion.withColumn(column)
                .withCondition(condition)
                .withSubCriteria(subCriteria)
                .build();

        return d -> d.and(initialCriterion);
    }

    static HavingApplier having(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return having(initialCriterion, Arrays.asList(subCriteria));
    }

    static HavingApplier having(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        CriteriaGroup ic = new CriteriaGroup.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build();

        return d -> d.and(ic);
    }
}
