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
 * @param <F> a class that extends {@link AbstractJoinSpecificationFinisher} - which includes "and" and "or"
 *           methods for build complex joins. We require this for methods such as {@link #join(SqlTable)}
 *           that build the join specification with fluent method calls.
 */
public interface JoinOperations<D extends JoinOperations<D, F>, F extends AbstractJoinSpecificationFinisher<D, F>> {

    default JoinOnGatherer<D, F> join(SqlTable joinTable) {
        return new JoinOnGatherer<>(buildJoinFinisher(JoinType.INNER, joinTable));
    }

    default JoinOnGatherer<D, F> join(SqlTable joinTable, String tableAlias) {
        addTableAlias(joinTable, tableAlias);
        return join(joinTable);
    }

    default JoinOnGatherer<D, F> join(Buildable<SelectModel> joinTable, @Nullable String tableAlias) {
        return new JoinOnGatherer<>(buildJoinFinisher(JoinType.INNER, buildSubQuery(joinTable, tableAlias)));
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
        return join(joinTable).on(onJoinCriterion).and(andJoinCriteria).endJoinSpecification();
    }

    default D join(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                   List<AndOrCriteriaGroup> andJoinCriteria) {
        return join(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria).endJoinSpecification();
    }

    default D join(Buildable<SelectModel> subQuery, @Nullable String tableAlias, SqlCriterion onJoinCriterion,
                   List<AndOrCriteriaGroup> andJoinCriteria) {
        return join(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria).endJoinSpecification();
    }

    default JoinOnGatherer<D, F> leftJoin(SqlTable joinTable) {
        return new JoinOnGatherer<>(buildJoinFinisher(JoinType.LEFT, joinTable));
    }

    default JoinOnGatherer<D, F> leftJoin(SqlTable joinTable, String tableAlias) {
        addTableAlias(joinTable, tableAlias);
        return leftJoin(joinTable);
    }

    default JoinOnGatherer<D, F> leftJoin(Buildable<SelectModel> joinTable, @Nullable String tableAlias) {
        return new JoinOnGatherer<>(buildJoinFinisher(JoinType.LEFT, buildSubQuery(joinTable, tableAlias)));
    }

    default D leftJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                       AndOrCriteriaGroup... andJoinCriteria) {
        return leftJoin(joinTable, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default D leftJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       AndOrCriteriaGroup... andJoinCriteria) {
        return leftJoin(joinTable, tableAlias, onJoinCriterion, Arrays.asList(andJoinCriteria)) ;
    }

    default D leftJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                       List<AndOrCriteriaGroup> andJoinCriteria) {
        return leftJoin(joinTable).on(onJoinCriterion).and(andJoinCriteria).endJoinSpecification();
    }

    default D leftJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       List<AndOrCriteriaGroup> andJoinCriteria) {
        return leftJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria).endJoinSpecification();
    }

    default D leftJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                       SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        return leftJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria).endJoinSpecification();
    }

    default JoinOnGatherer<D, F> rightJoin(SqlTable joinTable) {
        return new JoinOnGatherer<>(buildJoinFinisher(JoinType.RIGHT, joinTable));
    }

    default JoinOnGatherer<D, F> rightJoin(SqlTable joinTable, String tableAlias) {
        addTableAlias(joinTable, tableAlias);
        return rightJoin(joinTable);
    }

    default JoinOnGatherer<D, F> rightJoin(Buildable<SelectModel> joinTable, @Nullable String tableAlias) {
        return new JoinOnGatherer<>(buildJoinFinisher(JoinType.RIGHT, buildSubQuery(joinTable, tableAlias)));
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
        return rightJoin(joinTable).on(onJoinCriterion).and(andJoinCriteria).endJoinSpecification();
    }

    default D rightJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                        List<AndOrCriteriaGroup> andJoinCriteria) {
        return rightJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria).endJoinSpecification();
    }

    default D rightJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                        SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        return rightJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria).endJoinSpecification();
    }

    default JoinOnGatherer<D, F> fullJoin(SqlTable joinTable) {
        return new JoinOnGatherer<>(buildJoinFinisher(JoinType.FULL, joinTable));
    }

    default JoinOnGatherer<D, F> fullJoin(SqlTable joinTable, String tableAlias) {
        addTableAlias(joinTable, tableAlias);
        return fullJoin(joinTable);
    }

    default JoinOnGatherer<D, F> fullJoin(Buildable<SelectModel> joinTable, @Nullable String tableAlias) {
        return new JoinOnGatherer<>(buildJoinFinisher(JoinType.FULL, buildSubQuery(joinTable, tableAlias)));
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
        return fullJoin(joinTable).on(onJoinCriterion).and(andJoinCriteria).endJoinSpecification();
    }

    default D fullJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       List<AndOrCriteriaGroup> andJoinCriteria) {
        return fullJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria).endJoinSpecification();
    }

    default D fullJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                       SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        return fullJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria).endJoinSpecification();
    }

    void addTableAlias(SqlTable table, String tableAlias);

    private SubQuery buildSubQuery(Buildable<SelectModel> selectModel, @Nullable String alias) {
        return new SubQuery.Builder()
                .withSelectModel(selectModel.build())
                .withAlias(alias)
                .build();
    }

    private F buildJoinFinisher(JoinType joinType, TableExpression joinTable) {
        F finisher = buildJoinFinisher();
        finisher.setJoinTable(joinTable);
        finisher.setJoinType(joinType);
        return finisher;
    }

    /**
     * Builds a join finisher (a class that extends {@link AbstractJoinSpecificationFinisher}) and provides access to
     * other DSL methods as appropriate.
     *
     * <p>This method is called internally by the various join methods and is not intended to be called by general
     * users.</p>
     *
     * @return the join finisher
     */
    F buildJoinFinisher();

    class JoinOnGatherer<D extends JoinOperations<D, F>, F extends AbstractJoinSpecificationFinisher<D, F>> {
        private final F finisher;

        public JoinOnGatherer(F finisher) {
            this.finisher = finisher;
        }

        public F on(SqlCriterion sqlCriterion) {
            finisher.setInitialCriterion(sqlCriterion);
            return finisher;
        }

        public <T> F on(BindableColumn<T> joinColumn, RenderableCondition<T> joinCondition) {
            finisher.setInitialCriterion(ColumnAndConditionCriterion.withColumn(joinColumn)
                    .withCondition(joinCondition)
                    .build());
            return finisher;
        }

        public <T> F on(BindableColumn<T> joinColumn, RenderableCondition<T> onJoinCondition,
                        AndOrCriteriaGroup... subCriteria) {
            finisher.setInitialCriterion(ColumnAndConditionCriterion.withColumn(joinColumn)
                    .withCondition(onJoinCondition)
                    .withSubCriteria(Arrays.asList(subCriteria))
                    .build());
            return finisher;
        }
    }
}
