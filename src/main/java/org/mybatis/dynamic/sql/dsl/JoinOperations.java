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
import org.mybatis.dynamic.sql.select.join.JoinType;
import org.mybatis.dynamic.sql.util.Buildable;

/**
 * Methods related to joins.
 *
 * @param <D> a class that implements this interface. Typically, this is the owning DSL. We require this for
 *           the methods suchs as {@link #join(SqlTable, SqlCriterion, AndOrCriteriaGroup...)} that build
 *           an entire join specification in a single method call
 * @param <F> a class that extends {@link BooleanOperations} - which includes "and" and "or"
 *           methods for build complex joins. We require this for methods such as {@link #join(SqlTable)}
 *           that build the join specification with fluent method calls.
 */
public interface JoinOperations<D extends JoinOperations<D, F>, F extends BooleanOperations<?>> {

    D endJoinSpecification();

    void addTableAlias(SqlTable table, String tableAlias);

    /**
     * Builds a join finisher (a class that extends {@link BooleanOperations}) and provides access to
     * other DSL methods as appropriate.
     *
     * @return the join finisher
     */
    F join(JoinType joinType, TableExpression joinTable, SqlCriterion initialCriterion);

    default JoinOnGatherer<F> join(SqlTable joinTable) {
        return new JoinOnGatherer<>(JoinType.INNER, joinTable, this::join);
    }

    default JoinOnGatherer<F> join(SqlTable joinTable, String tableAlias) {
        var g = join(joinTable);
        addTableAlias(joinTable, tableAlias);
        return g;
    }

    default JoinOnGatherer<F> join(Buildable<SelectModel> joinTable, @Nullable String tableAlias) {
        return new JoinOnGatherer<>(JoinType.INNER, buildSubQuery(joinTable, tableAlias), this::join);
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
        join(joinTable).on(onJoinCriterion).and(andJoinCriteria);
        return endJoinSpecification();
    }

    default D join(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                   List<AndOrCriteriaGroup> andJoinCriteria) {
        join(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return endJoinSpecification();
    }

    default D join(Buildable<SelectModel> subQuery, @Nullable String tableAlias, SqlCriterion onJoinCriterion,
                   List<AndOrCriteriaGroup> andJoinCriteria) {
        join(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return endJoinSpecification();
    }

    default JoinOnGatherer<F> leftJoin(SqlTable joinTable) {
        return new JoinOnGatherer<>(JoinType.LEFT, joinTable, this::join);
    }

    default JoinOnGatherer<F> leftJoin(SqlTable joinTable, String tableAlias) {
        var g = leftJoin(joinTable);
        addTableAlias(joinTable, tableAlias);
        return g;
    }

    default JoinOnGatherer<F> leftJoin(Buildable<SelectModel> joinTable, @Nullable String tableAlias) {
        return new JoinOnGatherer<>(JoinType.LEFT, buildSubQuery(joinTable, tableAlias), this::join);
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
        leftJoin(joinTable).on(onJoinCriterion).and(andJoinCriteria);
        return endJoinSpecification();
    }

    default D leftJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       List<AndOrCriteriaGroup> andJoinCriteria) {
        leftJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return endJoinSpecification();
    }

    default D leftJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                       SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        leftJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return endJoinSpecification();
    }

    default JoinOnGatherer<F> rightJoin(SqlTable joinTable) {
        return new JoinOnGatherer<>(JoinType.RIGHT, joinTable, this::join);
    }

    default JoinOnGatherer<F> rightJoin(SqlTable joinTable, String tableAlias) {
        var g = rightJoin(joinTable);
        addTableAlias(joinTable, tableAlias);
        return g;
    }

    default JoinOnGatherer<F> rightJoin(Buildable<SelectModel> joinTable, @Nullable String tableAlias) {
        return new JoinOnGatherer<>(JoinType.RIGHT, buildSubQuery(joinTable, tableAlias), this::join);
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
        rightJoin(joinTable).on(onJoinCriterion).and(andJoinCriteria);
        return endJoinSpecification();
    }

    default D rightJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                        List<AndOrCriteriaGroup> andJoinCriteria) {
        rightJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return endJoinSpecification();
    }

    default D rightJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                        SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        rightJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return endJoinSpecification();
    }

    default JoinOnGatherer<F> fullJoin(SqlTable joinTable) {
        return new JoinOnGatherer<>(JoinType.FULL, joinTable, this::join);
    }

    default JoinOnGatherer<F> fullJoin(SqlTable joinTable, String tableAlias) {
        var g = fullJoin(joinTable);
        addTableAlias(joinTable, tableAlias);
        return g;
    }

    default JoinOnGatherer<F> fullJoin(Buildable<SelectModel> joinTable, @Nullable String tableAlias) {
        return new JoinOnGatherer<>(JoinType.FULL, buildSubQuery(joinTable, tableAlias), this::join);
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
        fullJoin(joinTable).on(onJoinCriterion).and(andJoinCriteria);
        return endJoinSpecification();
    }

    default D fullJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       List<AndOrCriteriaGroup> andJoinCriteria) {
        fullJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return endJoinSpecification();
    }

    default D fullJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                       SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        fullJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return endJoinSpecification();
    }

    private SubQuery buildSubQuery(Buildable<SelectModel> selectModel, @Nullable String alias) {
        return new SubQuery.Builder()
                .withSelectModel(selectModel.build())
                .withAlias(alias)
                .build();
    }

    interface JoinBuilder<R> {
        R apply(JoinType joinType, TableExpression tableExpression, SqlCriterion initialCriterion);
    }

    class JoinOnGatherer<F extends BooleanOperations<?>> {
        private final JoinType joinType;
        private final TableExpression joinTable;
        private final JoinBuilder<F> builder;

        public JoinOnGatherer(JoinType joinType, TableExpression joinTable, JoinBuilder<F> builder) {
            this.joinType = joinType;
            this.joinTable = joinTable;
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
            return builder.apply(joinType, joinTable, initialCriterion);
        }
    }
}
