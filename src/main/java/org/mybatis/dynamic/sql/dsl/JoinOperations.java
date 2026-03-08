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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion;
import org.mybatis.dynamic.sql.RenderableCondition;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.TableExpression;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.SubQuery;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.select.join.JoinType;
import org.mybatis.dynamic.sql.util.Buildable;

/**
 * Methods related to joins.
 *
 * @param <D> a class that implements this interface. Typically, this is the owning DSL. We require this for
 *           the methods suchs as {@link #join(SqlTable, SqlCriterion, AndOrCriteriaGroup...)} that build
 *           an entire join specification in a single method call
 * @param <F> a class that extends {@link AbstractJoinSupport} - which includes "and" and "or"
 *           methods for build complex joins. We require this for methods such as {@link #join(SqlTable)}
 *           that build the join specification with fluent method calls.
 */
public interface JoinOperations<D extends JoinOperations<D, F>, F extends JoinOperations.AbstractJoinSupport<D, F>> {

    F join(JoinType joinType, TableExpression joinTable, SqlCriterion initialCriterion);

    F join(JoinType joinType, SqlTable joinTable, String tableAlias, SqlCriterion initialCriterion);

    default JoinOnGatherer<F> join(SqlTable joinTable) {
        return new JoinOnGatherer<>(ic -> join(JoinType.INNER, joinTable, ic));
    }

    default JoinOnGatherer<F> join(SqlTable joinTable, String tableAlias) {
        return new JoinOnGatherer<>(ic -> join(JoinType.INNER, joinTable, tableAlias, ic));
    }

    default JoinOnGatherer<F> join(Buildable<SelectModel> joinTable, @Nullable String tableAlias) {
        return new JoinOnGatherer<>(ic -> join(JoinType.INNER, buildSubQuery(joinTable, tableAlias), ic));
    }

    default D join(SqlTable joinTable, SqlCriterion onJoinCriterion,
                   AndOrCriteriaGroup... andJoinCriteria) {
        return join(joinTable, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default D join(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                   AndOrCriteriaGroup... andJoinCriteria) {
        return join(joinTable, tableAlias, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default D join(SqlTable joinTable, SqlCriterion onJoinCriterion,
                   List<AndOrCriteriaGroup> andJoinCriteria) {
        return join(joinTable).on(onJoinCriterion).and(andJoinCriteria).endJoin();
    }

    default D join(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                   List<AndOrCriteriaGroup> andJoinCriteria) {
        return join(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria).endJoin();
    }

    default D join(Buildable<SelectModel> subQuery, @Nullable String tableAlias, SqlCriterion onJoinCriterion,
                   List<AndOrCriteriaGroup> andJoinCriteria) {
        return join(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria).endJoin();
    }

    default JoinOnGatherer<F> leftJoin(SqlTable joinTable) {
        return new JoinOnGatherer<>(ic -> join(JoinType.LEFT, joinTable, ic));
    }

    default JoinOnGatherer<F> leftJoin(SqlTable joinTable, String tableAlias) {
        return new JoinOnGatherer<>(ic -> join(JoinType.LEFT, joinTable, tableAlias, ic));
    }

    default JoinOnGatherer<F> leftJoin(Buildable<SelectModel> joinTable, @Nullable String tableAlias) {
        return new JoinOnGatherer<>(ic -> join(JoinType.LEFT, buildSubQuery(joinTable, tableAlias), ic));
    }

    default D leftJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                       AndOrCriteriaGroup... andJoinCriteria) {
        return leftJoin(joinTable, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default D leftJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       AndOrCriteriaGroup... andJoinCriteria) {
        return leftJoin(joinTable, tableAlias, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default D leftJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                       List<AndOrCriteriaGroup> andJoinCriteria) {
        return leftJoin(joinTable).on(onJoinCriterion).and(andJoinCriteria).endJoin();
    }

    default D leftJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       List<AndOrCriteriaGroup> andJoinCriteria) {
        return leftJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria).endJoin();
    }

    default D leftJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                       SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        return leftJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria).endJoin();
    }

    default JoinOnGatherer<F> rightJoin(SqlTable joinTable) {
        return new JoinOnGatherer<>(ic -> join(JoinType.RIGHT, joinTable, ic));
    }

    default JoinOnGatherer<F> rightJoin(SqlTable joinTable, String tableAlias) {
        return new JoinOnGatherer<>(ic -> join(JoinType.RIGHT, joinTable, tableAlias, ic));
    }

    default JoinOnGatherer<F> rightJoin(Buildable<SelectModel> joinTable, @Nullable String tableAlias) {
        return new JoinOnGatherer<>(ic -> join(JoinType.RIGHT, buildSubQuery(joinTable, tableAlias), ic));
    }

    default D rightJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                        AndOrCriteriaGroup... andJoinCriteria) {
        return rightJoin(joinTable, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default D rightJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                        AndOrCriteriaGroup... andJoinCriteria) {
        return rightJoin(joinTable, tableAlias, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default D rightJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                        List<AndOrCriteriaGroup> andJoinCriteria) {
        return rightJoin(joinTable).on(onJoinCriterion).and(andJoinCriteria).endJoin();
    }

    default D rightJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                        List<AndOrCriteriaGroup> andJoinCriteria) {
        return rightJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria).endJoin();
    }

    default D rightJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                        SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        return rightJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria).endJoin();
    }

    default JoinOnGatherer<F> fullJoin(SqlTable joinTable) {
        return new JoinOnGatherer<>(ic -> join(JoinType.FULL, joinTable, ic));
    }

    default JoinOnGatherer<F> fullJoin(SqlTable joinTable, String tableAlias) {
        return new JoinOnGatherer<>(ic -> join(JoinType.FULL, joinTable, tableAlias, ic));
    }

    default JoinOnGatherer<F> fullJoin(Buildable<SelectModel> joinTable, @Nullable String tableAlias) {
        return new JoinOnGatherer<>(ic -> join(JoinType.FULL, buildSubQuery(joinTable, tableAlias), ic));
    }

    default D fullJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                       AndOrCriteriaGroup... andJoinCriteria) {
        return fullJoin(joinTable, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default D fullJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       AndOrCriteriaGroup... andJoinCriteria) {
        return fullJoin(joinTable, tableAlias, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default D fullJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                       List<AndOrCriteriaGroup> andJoinCriteria) {
        return fullJoin(joinTable).on(onJoinCriterion).and(andJoinCriteria).endJoin();
    }

    default D fullJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       List<AndOrCriteriaGroup> andJoinCriteria) {
        return fullJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria).endJoin();
    }

    default D fullJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                       SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        return fullJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria).endJoin();
    }

    private SubQuery buildSubQuery(Buildable<SelectModel> selectModel, @Nullable String alias) {
        return new SubQuery.Builder()
                .withSelectModel(selectModel.build())
                .withAlias(alias)
                .build();
    }

    class JoinOnGatherer<F extends BooleanOperations<?>> {
        private final Function<SqlCriterion, F> builder;

        public JoinOnGatherer(Function<SqlCriterion, F> builder) {
            this.builder = builder;
        }

        public <T> F on(BindableColumn<T> joinColumn, RenderableCondition<T> joinCondition) {
            return on(ColumnAndConditionCriterion.withColumn(joinColumn)
                    .withCondition(joinCondition)
                    .build());
        }

        public <T> F on(BindableColumn<T> joinColumn, RenderableCondition<T> onJoinCondition,
                        AndOrCriteriaGroup... subCriteria) {
            return on(ColumnAndConditionCriterion.withColumn(joinColumn)
                    .withCondition(onJoinCondition)
                    .withSubCriteria(Arrays.asList(subCriteria))
                    .build());
        }

        public F on(SqlCriterion initialCriterion) {
            return builder.apply(initialCriterion);
        }
    }

    abstract class AbstractJoinSupport<D extends JoinOperations<D, F>, F extends AbstractJoinSupport<D, F>>
            implements JoinOperations<D, F>, BooleanOperations<F> {
        private final JoinType joinType;
        private final TableExpression joinTable;
        private final SqlCriterion initialCriterion;
        private final List<AndOrCriteriaGroup> subCriteria = new ArrayList<>();

        protected AbstractJoinSupport(JoinType joinType, TableExpression joinTable, SqlCriterion initialCriterion) {
            this.joinType = joinType;
            this.joinTable = joinTable;
            this.initialCriterion = initialCriterion;
        }

        @Override
        public F addSubCriterion(AndOrCriteriaGroup subCriterion) {
            subCriteria.add(subCriterion);
            return getThis();
        }

        protected abstract F getThis();

        protected abstract D endJoin();

        protected JoinSpecification toJoinSpecification() {
            return JoinSpecification.withJoinTable(Objects.requireNonNull(joinTable))
                    .withJoinType(Objects.requireNonNull(joinType))
                    .withInitialCriterion(initialCriterion)
                    .withSubCriteria(subCriteria)
                    .build();
        }
    }
}
