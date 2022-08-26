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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.util.ConfigurableStatement;

public abstract class AbstractWhereDSL<T extends AbstractWhereDSL<T>> implements ConfigurableStatement<T> {
    private SqlCriterion initialCriterion; // WARNING - may be null!
    private final List<AndOrCriteriaGroup> subCriteria = new ArrayList<>();

    private final StatementConfiguration statementConfiguration;

    protected AbstractWhereDSL(StatementConfiguration statementConfiguration) {
        this.statementConfiguration = Objects.requireNonNull(statementConfiguration);
    }

    @Override
    public T configureStatement(Consumer<StatementConfiguration> consumer) {
        consumer.accept(statementConfiguration);
        return getThis();
    }

    @NotNull
    public <S> T where(BindableColumn<S> column, VisitableCondition<S> condition,
                       AndOrCriteriaGroup... subCriteria) {
        return where(column, condition, Arrays.asList(subCriteria));
    }

    @NotNull
    public <S> T where(BindableColumn<S> column, VisitableCondition<S> condition,
                       List<AndOrCriteriaGroup> subCriteria) {
        initialCriterion = ColumnAndConditionCriterion.withColumn(column)
                .withCondition(condition)
                .withSubCriteria(subCriteria)
                .build();
        return getThis();
    }

    @NotNull
    public T where(ExistsPredicate existsPredicate, AndOrCriteriaGroup... subCriteria) {
        return where(existsPredicate, Arrays.asList(subCriteria));
    }

    @NotNull
    public T where(ExistsPredicate existsPredicate, List<AndOrCriteriaGroup> subCriteria) {
        initialCriterion = new ExistsCriterion.Builder()
                .withExistsPredicate(existsPredicate).withSubCriteria(subCriteria).build();
        return getThis();
    }

    @NotNull
    public T where(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return where(initialCriterion, Arrays.asList(subCriteria));
    }

    @NotNull
    public T where(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        this.initialCriterion = new CriteriaGroup.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build();
        return getThis();
    }

    @NotNull
    public T where(List<AndOrCriteriaGroup> criteria) {
        initialCriterion = new CriteriaGroup.Builder()
                .withSubCriteria(criteria)
                .build();
        return getThis();
    }

    @NotNull
    public T applyWhere(WhereApplier whereApplier) {
        whereApplier.accept(this);
        return getThis();
    }

    @NotNull
    public <S> T and(BindableColumn<S> column, VisitableCondition<S> condition,
                     AndOrCriteriaGroup... subCriteria) {
        return and(column, condition, Arrays.asList(subCriteria));
    }

    @NotNull
    public <S> T and(BindableColumn<S> column, VisitableCondition<S> condition,
                     List<AndOrCriteriaGroup> subCriteria) {
        addSubCriteria("and", buildCriterion(column, condition), subCriteria); //$NON-NLS-1$
        return getThis();
    }

    @NotNull
    public T and(ExistsPredicate existsPredicate, AndOrCriteriaGroup... subCriteria) {
        return and(existsPredicate, Arrays.asList(subCriteria));
    }

    @NotNull
    public T and(ExistsPredicate existsPredicate, List<AndOrCriteriaGroup> subCriteria) {
        addSubCriteria("and", buildCriterion(existsPredicate), subCriteria); //$NON-NLS-1$
        return getThis();
    }

    @NotNull
    public T and(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return and(initialCriterion, Arrays.asList(subCriteria));
    }

    @NotNull
    public T and(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        addSubCriteria("and", buildCriterion(initialCriterion), subCriteria); //$NON-NLS-1$
        return getThis();
    }

    @NotNull
    public T and(List<AndOrCriteriaGroup> criteria) {
        addSubCriteria("and", criteria); //$NON-NLS-1$
        return getThis();
    }

    @NotNull
    public <S> T or(BindableColumn<S> column, VisitableCondition<S> condition,
                    AndOrCriteriaGroup... subCriteria) {
        return or(column, condition, Arrays.asList(subCriteria));
    }

    @NotNull
    public <S> T or(BindableColumn<S> column, VisitableCondition<S> condition,
                    List<AndOrCriteriaGroup> subCriteria) {
        addSubCriteria("or", buildCriterion(column, condition), subCriteria); //$NON-NLS-1$
        return getThis();
    }

    @NotNull
    public T or(ExistsPredicate existsPredicate, AndOrCriteriaGroup... subCriteria) {
        return or(existsPredicate, Arrays.asList(subCriteria));
    }

    @NotNull
    public T or(ExistsPredicate existsPredicate, List<AndOrCriteriaGroup> subCriteria) {
        addSubCriteria("or", buildCriterion(existsPredicate), subCriteria); //$NON-NLS-1$
        return getThis();
    }

    @NotNull
    public T or(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return or(initialCriterion, Arrays.asList(subCriteria));
    }

    @NotNull
    public T or(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        addSubCriteria("or", buildCriterion(initialCriterion), subCriteria); //$NON-NLS-1$
        return getThis();
    }

    @NotNull
    public T or(List<AndOrCriteriaGroup> criteria) {
        addSubCriteria("or", criteria); //$NON-NLS-1$
        return getThis();
    }

    protected WhereModel internalBuild() {
        return new WhereModel(initialCriterion, subCriteria, statementConfiguration);
    }

    private <R> SqlCriterion buildCriterion(BindableColumn<R> column, VisitableCondition<R> condition) {
        return ColumnAndConditionCriterion.withColumn(column).withCondition(condition).build();
    }

    private SqlCriterion buildCriterion(ExistsPredicate existsPredicate) {
        return new ExistsCriterion.Builder().withExistsPredicate(existsPredicate).build();
    }

    private SqlCriterion buildCriterion(SqlCriterion initialCriterion) {
        return new CriteriaGroup.Builder().withInitialCriterion(initialCriterion).build();
    }

    private void addSubCriteria(String connector, SqlCriterion initialCriterion,
                                List<AndOrCriteriaGroup> subCriteria) {
        this.subCriteria.add(new AndOrCriteriaGroup.Builder()
                .withInitialCriterion(initialCriterion)
                .withConnector(connector)
                .withSubCriteria(subCriteria)
                .build());
    }

    private void addSubCriteria(String connector, List<AndOrCriteriaGroup> criteria) {
        this.subCriteria.add(new AndOrCriteriaGroup.Builder()
                .withConnector(connector)
                .withSubCriteria(criteria)
                .build());
    }

    protected abstract T getThis();
}
