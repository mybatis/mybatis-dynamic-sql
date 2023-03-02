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
import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionDSL;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.util.ConfigurableStatement;

public abstract class AbstractWhereDSL<T extends AbstractWhereDSL<T>> extends AbstractBooleanExpressionDSL<T>
        implements ConfigurableStatement<T> {
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
        setInitialCriterion(ColumnAndConditionCriterion.withColumn(column)
                .withCondition(condition)
                .withSubCriteria(subCriteria)
                .build(), "ERROR.32"); //$NON-NLS-1$
        return getThis();
    }

    @NotNull
    public T where(ExistsPredicate existsPredicate, AndOrCriteriaGroup... subCriteria) {
        return where(existsPredicate, Arrays.asList(subCriteria));
    }

    @NotNull
    public T where(ExistsPredicate existsPredicate, List<AndOrCriteriaGroup> subCriteria) {
        setInitialCriterion(new ExistsCriterion.Builder()
                .withExistsPredicate(existsPredicate).withSubCriteria(subCriteria).build(), "ERROR.32"); //$NON-NLS-1$
        return getThis();
    }

    @NotNull
    public T where(SqlCriterion initialCriterion, AndOrCriteriaGroup... subCriteria) {
        return where(initialCriterion, Arrays.asList(subCriteria));
    }

    @NotNull
    public T where(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        setInitialCriterion(new CriteriaGroup.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build(), "ERROR.32"); //$NON-NLS-1$
        return getThis();
    }

    @NotNull
    public T where(List<AndOrCriteriaGroup> criteria) {
        setInitialCriterion(new CriteriaGroup.Builder()
                .withSubCriteria(criteria)
                .build(), "ERROR.32"); //$NON-NLS-1$
        return getThis();
    }

    @NotNull
    public T applyWhere(WhereApplier whereApplier) {
        whereApplier.accept(this);
        return getThis();
    }

    protected WhereModel internalBuild() {
        return new WhereModel(getInitialCriterion(), subCriteria, statementConfiguration);
    }

    @Override
    protected abstract T getThis();
}
