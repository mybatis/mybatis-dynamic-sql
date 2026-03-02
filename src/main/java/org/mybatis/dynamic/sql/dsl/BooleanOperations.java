/*
 *    Copyright 2016-2026 the original author or authors.
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
package org.mybatis.dynamic.sql.dsl;

import java.util.Arrays;
import java.util.List;

import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion;
import org.mybatis.dynamic.sql.CriteriaGroup;
import org.mybatis.dynamic.sql.ExistsCriterion;
import org.mybatis.dynamic.sql.ExistsPredicate;
import org.mybatis.dynamic.sql.RenderableCondition;
import org.mybatis.dynamic.sql.SqlCriterion;

public interface BooleanOperations<T extends BooleanOperations<T>> {
    default <S> T and(BindableColumn<S> column, RenderableCondition<S> condition,
                     AndOrCriteriaGroup... subCriteria) {
        return and(column, condition, Arrays.asList(subCriteria));
    }

    default <S> T and(BindableColumn<S> column, RenderableCondition<S> condition,
                     List<AndOrCriteriaGroup> subCriteria) {
        return addSubCriterion("and", buildCriterion(column, condition), subCriteria); //$NON-NLS-1$
    }

    default T and(ExistsPredicate existsPredicate, AndOrCriteriaGroup... subCriteria) {
        return and(existsPredicate, Arrays.asList(subCriteria));
    }

    default T and(ExistsPredicate existsPredicate, List<AndOrCriteriaGroup> subCriteria) {
        return addSubCriterion("and", buildCriterion(existsPredicate), subCriteria); //$NON-NLS-1$
    }

    default T and(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return and(initialCriterion, Arrays.asList(subCriteria));
    }

    default T and(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        return addSubCriterion("and", buildCriterion(initialCriterion), subCriteria); //$NON-NLS-1$
    }

    default T and(List<AndOrCriteriaGroup> criteria) {
        return addSubCriterion("and", criteria); //$NON-NLS-1$
    }

    default <S> T or(BindableColumn<S> column, RenderableCondition<S> condition,
                    AndOrCriteriaGroup... subCriteria) {
        return or(column, condition, Arrays.asList(subCriteria));
    }

    default <S> T or(BindableColumn<S> column, RenderableCondition<S> condition,
                    List<AndOrCriteriaGroup> subCriteria) {
        return addSubCriterion("or", buildCriterion(column, condition), subCriteria); //$NON-NLS-1$
    }

    default T or(ExistsPredicate existsPredicate, AndOrCriteriaGroup... subCriteria) {
        return or(existsPredicate, Arrays.asList(subCriteria));
    }

    default T or(ExistsPredicate existsPredicate, List<AndOrCriteriaGroup> subCriteria) {
        return addSubCriterion("or", buildCriterion(existsPredicate), subCriteria); //$NON-NLS-1$
    }

    default T or(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return or(initialCriterion, Arrays.asList(subCriteria));
    }

    default T or(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        return addSubCriterion("or", buildCriterion(initialCriterion), subCriteria); //$NON-NLS-1$
    }

    default T or(List<AndOrCriteriaGroup> criteria) {
        return addSubCriterion("or", criteria); //$NON-NLS-1$
    }

    private <R> SqlCriterion buildCriterion(BindableColumn<R> column, RenderableCondition<R> condition) {
        return ColumnAndConditionCriterion.withColumn(column).withCondition(condition).build();
    }

    private SqlCriterion buildCriterion(ExistsPredicate existsPredicate) {
        return new ExistsCriterion.Builder().withExistsPredicate(existsPredicate).build();
    }

    private SqlCriterion buildCriterion(SqlCriterion initialCriterion) {
        return new CriteriaGroup.Builder().withInitialCriterion(initialCriterion).build();
    }

    private T addSubCriterion(String connector, SqlCriterion initialCriterion,
                              List<AndOrCriteriaGroup> subCriteria) {
        return addSubCriterion(new AndOrCriteriaGroup.Builder()
                .withInitialCriterion(initialCriterion)
                .withConnector(connector)
                .withSubCriteria(subCriteria)
                .build());
    }

    private T addSubCriterion(String connector, List<AndOrCriteriaGroup> criteria) {
        return addSubCriterion(new AndOrCriteriaGroup.Builder()
                .withConnector(connector)
                .withSubCriteria(criteria)
                .build());
    }

    T addSubCriterion(AndOrCriteriaGroup subCriterion);
}
