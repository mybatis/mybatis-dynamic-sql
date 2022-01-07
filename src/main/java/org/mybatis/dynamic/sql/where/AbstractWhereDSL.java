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
import org.mybatis.dynamic.sql.CriteriaGroup;
import org.mybatis.dynamic.sql.CriteriaGroupWithConnector;
import org.mybatis.dynamic.sql.ExistsCriterion;
import org.mybatis.dynamic.sql.ExistsPredicate;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.VisitableCondition;

public abstract class AbstractWhereDSL<T extends AbstractWhereDSL<T>> {
    private SqlCriterion initialCriterion; // WARNING - may be null!
    private final List<CriteriaGroupWithConnector> subCriteria = new ArrayList<>();

    @NotNull
    public <S> T where(BindableColumn<S> column, VisitableCondition<S> condition,
                       CriteriaGroupWithConnector...subCriteria) {
        return where(column, condition, Arrays.asList(subCriteria));
    }

    @NotNull
    public <S> T where(BindableColumn<S> column, VisitableCondition<S> condition,
                       List<CriteriaGroupWithConnector> subCriteria) {
        initialCriterion = buildCriterion(column, condition, subCriteria);
        return getThis();
    }

    @NotNull
    public T where(ExistsPredicate existsPredicate, CriteriaGroupWithConnector...subCriteria) {
        return where(existsPredicate, Arrays.asList(subCriteria));
    }

    @NotNull
    public T where(ExistsPredicate existsPredicate, List<CriteriaGroupWithConnector> subCriteria) {
        initialCriterion = buildCriterion(existsPredicate, subCriteria);
        return getThis();
    }

    @NotNull
    public T where(CriteriaGroup criterion, CriteriaGroupWithConnector...subCriteria) {
        return where(criterion, Arrays.asList(subCriteria));
    }

    @NotNull
    public T where(CriteriaGroup criterion, List<CriteriaGroupWithConnector> subCriteria) {
        initialCriterion = buildCriterion(criterion, subCriteria);
        return getThis();
    }

    @NotNull
    public T applyWhere(WhereApplier whereApplier) {
        whereApplier.accept(this);
        return getThis();
    }

    @NotNull
    public <S> T and(BindableColumn<S> column, VisitableCondition<S> condition,
                     CriteriaGroupWithConnector...subCriteria) {
        return and(column, condition, Arrays.asList(subCriteria));
    }

    @NotNull
    public <S> T and(BindableColumn<S> column, VisitableCondition<S> condition,
                     List<CriteriaGroupWithConnector> subCriteria) {
        addCriteriaGroup("and", buildCriterion(column, condition), subCriteria);
        return getThis();
    }

    @NotNull
    public T and(ExistsPredicate existsPredicate, CriteriaGroupWithConnector...subCriteria) {
        return and(existsPredicate, Arrays.asList(subCriteria));
    }

    @NotNull
    public T and(ExistsPredicate existsPredicate, List<CriteriaGroupWithConnector> subCriteria) {
        addCriteriaGroup("and", buildCriterion(existsPredicate), subCriteria);
        return getThis();
    }

    @NotNull
    public T and(CriteriaGroup criteriaGroup, CriteriaGroupWithConnector...subCriteria) {
        return and(criteriaGroup, Arrays.asList(subCriteria));
    }

    @NotNull
    public T and(CriteriaGroup criteriaGroup, List<CriteriaGroupWithConnector> subCriteria) {
        addCriteriaGroup("and", buildCriterion(criteriaGroup), subCriteria);
        return getThis();
    }

    @NotNull
    public <S> T or(BindableColumn<S> column, VisitableCondition<S> condition,
                    CriteriaGroupWithConnector...subCriteria) {
        return or(column, condition, Arrays.asList(subCriteria));
    }

    @NotNull
    public <S> T or(BindableColumn<S> column, VisitableCondition<S> condition,
                    List<CriteriaGroupWithConnector> subCriteria) {
        addCriteriaGroup("or", buildCriterion(column, condition), subCriteria);
        return getThis();
    }

    @NotNull
    public T or(ExistsPredicate existsPredicate, CriteriaGroupWithConnector...subCriteria) {
        return or(existsPredicate, Arrays.asList(subCriteria));
    }

    @NotNull
    public T or(ExistsPredicate existsPredicate, List<CriteriaGroupWithConnector> subCriteria) {
        addCriteriaGroup("or", buildCriterion(existsPredicate), subCriteria);
        return getThis();
    }

    @NotNull
    public T or(CriteriaGroup criteriaGroup, CriteriaGroupWithConnector...subCriteria) {
        return or(criteriaGroup, Arrays.asList(subCriteria));
    }

    @NotNull
    public T or(CriteriaGroup criteriaGroup, List<CriteriaGroupWithConnector> subCriteria) {
        addCriteriaGroup("or", buildCriterion(criteriaGroup), subCriteria);
        return getThis();
    }

    protected WhereModel internalBuild() {
        return new WhereModel(initialCriterion, subCriteria);
    }

    private <R> ColumnAndConditionCriterion<R> buildCriterion(BindableColumn<R> column,
                                                              VisitableCondition<R> condition) {
        return ColumnAndConditionCriterion.withColumn(column).withCondition(condition).build();
    }

    private <R> ColumnAndConditionCriterion<R> buildCriterion(BindableColumn<R> column, VisitableCondition<R> condition,
                                                              List<CriteriaGroupWithConnector> subCriteria) {
        return ColumnAndConditionCriterion.withColumn(column)
                .withCondition(condition)
                .withSubCriteria(subCriteria)
                .build();
    }

    private ExistsCriterion buildCriterion(ExistsPredicate existsPredicate) {
        return new ExistsCriterion.Builder().withExistsPredicate(existsPredicate).build();
    }

    private ExistsCriterion buildCriterion(ExistsPredicate existsPredicate,
                                           List<CriteriaGroupWithConnector> subCriteria) {
        return new ExistsCriterion.Builder().withExistsPredicate(existsPredicate).withSubCriteria(subCriteria).build();
    }

    private CriteriaGroup buildCriterion(CriteriaGroup criteriaGroup) {
        return new CriteriaGroup.Builder().withInitialCriterion(criteriaGroup).build();
    }

    private CriteriaGroup buildCriterion(CriteriaGroup criteriaGroup, List<CriteriaGroupWithConnector> subCriteria) {
        return new CriteriaGroup.Builder().withInitialCriterion(criteriaGroup).withSubCriteria(subCriteria).build();
    }

    private void addCriteriaGroup(String connector, SqlCriterion initialCriterion,
                                  List<CriteriaGroupWithConnector> subCriteria) {
        this.subCriteria.add(new CriteriaGroupWithConnector.Builder()
                .withInitialCriterion(initialCriterion)
                .withConnector(connector)
                .withSubCriteria(subCriteria)
                .build());
    }

    protected abstract T getThis();
}
