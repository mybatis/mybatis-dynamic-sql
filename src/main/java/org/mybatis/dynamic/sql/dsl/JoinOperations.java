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

public interface JoinOperations<F extends BooleanOperations<F>> {

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

    default F join(SqlTable joinTable, SqlCriterion onJoinCriterion,
                   AndOrCriteriaGroup... andJoinCriteria) {
        return join(joinTable, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default F join(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                   AndOrCriteriaGroup... andJoinCriteria) {
        return join(joinTable, tableAlias, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default F join(SqlTable joinTable, SqlCriterion onJoinCriterion,
                   List<AndOrCriteriaGroup> andJoinCriteria) {
        return join(joinTable).on(onJoinCriterion).and(andJoinCriteria);
    }

    default F join(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                   List<AndOrCriteriaGroup> andJoinCriteria) {
        return join(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
    }

    default F join(Buildable<SelectModel> subQuery, @Nullable String tableAlias, SqlCriterion onJoinCriterion,
                   List<AndOrCriteriaGroup> andJoinCriteria) {
        return join(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
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

    default F leftJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                       AndOrCriteriaGroup... andJoinCriteria) {
        return leftJoin(joinTable, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default F leftJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       AndOrCriteriaGroup... andJoinCriteria) {
        return leftJoin(joinTable, tableAlias, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default F leftJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                       List<AndOrCriteriaGroup> andJoinCriteria) {
        return leftJoin(joinTable).on(onJoinCriterion).and(andJoinCriteria);
    }

    default F leftJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       List<AndOrCriteriaGroup> andJoinCriteria) {
        return leftJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
    }

    default F leftJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                       SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        return leftJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
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

    default F rightJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                        AndOrCriteriaGroup... andJoinCriteria) {
        return rightJoin(joinTable, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default F rightJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                        AndOrCriteriaGroup... andJoinCriteria) {
        return rightJoin(joinTable, tableAlias, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default F rightJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                        List<AndOrCriteriaGroup> andJoinCriteria) {
        return rightJoin(joinTable).on(onJoinCriterion).and(andJoinCriteria);
    }

    default F rightJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                        List<AndOrCriteriaGroup> andJoinCriteria) {
        return rightJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
    }

    default F rightJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                        SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        return rightJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
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

    default F fullJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                       AndOrCriteriaGroup... andJoinCriteria) {
        return fullJoin(joinTable, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default F fullJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       AndOrCriteriaGroup... andJoinCriteria) {
        return fullJoin(joinTable, tableAlias, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default F fullJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                       List<AndOrCriteriaGroup> andJoinCriteria) {
        return fullJoin(joinTable).on(onJoinCriterion).and(andJoinCriteria);
    }

    default F fullJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       List<AndOrCriteriaGroup> andJoinCriteria) {
        return fullJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
    }

    default F fullJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                       SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        return fullJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
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

}
