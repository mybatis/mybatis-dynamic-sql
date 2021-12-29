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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion;
import org.mybatis.dynamic.sql.CriterionGroup;
import org.mybatis.dynamic.sql.ExistsCriterion;
import org.mybatis.dynamic.sql.ExistsPredicate;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.VisitableCondition;

public abstract class AbstractWhereDSL<T extends AbstractWhereDSL<T>> {
    private final List<SqlCriterion> criteria = new ArrayList<>();

    @NotNull
    public <S> T where(BindableColumn<S> column, VisitableCondition<S> condition) {
        criteria.add(ColumnAndConditionCriterion.withColumn(column)
                .withCondition(condition)
                .build());
        return getThis();
    }

    @NotNull
    public <S> T where(BindableColumn<S> column, VisitableCondition<S> condition, SqlCriterion...subCriteria) {
        return where(column, condition, Arrays.asList(subCriteria));
    }

    @NotNull
    public <S> T where(BindableColumn<S> column, VisitableCondition<S> condition, List<SqlCriterion> subCriteria) {
        criteria.add(ColumnAndConditionCriterion.withColumn(column)
                .withCondition(condition)
                .withSubCriteria(subCriteria)
                .build());
        return getThis();
    }

    @NotNull
    public T where(ExistsPredicate existsPredicate) {
        criteria.add(new ExistsCriterion.Builder()
                .withExistsPredicate(existsPredicate)
                .build());
        return getThis();
    }

    @NotNull
    public T where(ExistsPredicate existsPredicate, SqlCriterion...subCriteria) {
        return where(existsPredicate, Arrays.asList(subCriteria));
    }

    @NotNull
    public T where(ExistsPredicate existsPredicate, List<SqlCriterion> subCriteria) {
        criteria.add(new ExistsCriterion.Builder()
                .withExistsPredicate(existsPredicate)
                .withSubCriteria(subCriteria)
                .build());
        return getThis();
    }

    @NotNull
    public T where(CriterionGroup criterion, SqlCriterion...subCriteria) {
        return where(criterion, Arrays.asList(subCriteria));
    }

    @NotNull
    public T where(CriterionGroup criterion, List<SqlCriterion> subCriteria) {
        criteria.add(new CriterionGroup.Builder()
                        .withCriterion(criterion)
                        .withSubCriteria(subCriteria)
                        .build());
        return getThis();
    }

    @NotNull
    public T applyWhere(WhereApplier whereApplier) {
        whereApplier.accept(this);
        return getThis();
    }

    @NotNull
    public <S> T and(BindableColumn<S> column, VisitableCondition<S> condition) {
        criteria.add(ColumnAndConditionCriterion.withColumn(column)
                .withConnector("and") //$NON-NLS-1$
                .withCondition(condition)
                .build());
        return getThis();
    }

    @NotNull
    public <S> T and(BindableColumn<S> column, VisitableCondition<S> condition, SqlCriterion...subCriteria) {
        return and(column, condition, Arrays.asList(subCriteria));
    }

    @NotNull
    public <S> T and(BindableColumn<S> column, VisitableCondition<S> condition, List<SqlCriterion> subCriteria) {
        criteria.add(ColumnAndConditionCriterion.withColumn(column)
                .withConnector("and") //$NON-NLS-1$
                .withCondition(condition)
                .withSubCriteria(subCriteria)
                .build());
        return getThis();
    }

    @NotNull
    public T and(ExistsPredicate existsPredicate) {
        criteria.add(new ExistsCriterion.Builder()
                .withConnector("and") //$NON-NLS-1$
                .withExistsPredicate(existsPredicate)
                .build());
        return getThis();
    }

    @NotNull
    public T and(ExistsPredicate existsPredicate, SqlCriterion...subCriteria) {
        return and(existsPredicate, Arrays.asList(subCriteria));
    }

    @NotNull
    public T and(ExistsPredicate existsPredicate, List<SqlCriterion> subCriteria) {
        criteria.add(new ExistsCriterion.Builder()
                .withConnector("and") //$NON-NLS-1$
                .withExistsPredicate(existsPredicate)
                .withSubCriteria(subCriteria)
                .build());
        return getThis();
    }

    @NotNull
    public <S> T or(BindableColumn<S> column, VisitableCondition<S> condition) {
        criteria.add(ColumnAndConditionCriterion.withColumn(column)
                .withConnector("or") //$NON-NLS-1$
                .withCondition(condition)
                .build());
        return getThis();
    }

    @NotNull
    public <S> T or(BindableColumn<S> column, VisitableCondition<S> condition, SqlCriterion...subCriteria) {
        return or(column, condition, Arrays.asList(subCriteria));
    }

    @NotNull
    public <S> T or(BindableColumn<S> column, VisitableCondition<S> condition, List<SqlCriterion> subCriteria) {
        criteria.add(ColumnAndConditionCriterion.withColumn(column)
                .withConnector("or") //$NON-NLS-1$
                .withCondition(condition)
                .withSubCriteria(subCriteria)
                .build());
        return getThis();
    }

    @NotNull
    public T or(ExistsPredicate existsPredicate) {
        criteria.add(new ExistsCriterion.Builder()
                .withConnector("or") //$NON-NLS-1$
                .withExistsPredicate(existsPredicate)
                .build());
        return getThis();
    }

    @NotNull
    public T or(ExistsPredicate existsPredicate, SqlCriterion...subCriteria) {
        return or(existsPredicate, Arrays.asList(subCriteria));
    }

    @NotNull
    public T or(ExistsPredicate existsPredicate, List<SqlCriterion> subCriteria) {
        criteria.add(new ExistsCriterion.Builder()
                .withConnector("or") //$NON-NLS-1$
                .withExistsPredicate(existsPredicate)
                .withSubCriteria(subCriteria)
                .build());
        return getThis();
    }

    protected WhereModel internalBuild() {
        return WhereModel.of(criteria);
    }

    protected abstract T getThis();
}
