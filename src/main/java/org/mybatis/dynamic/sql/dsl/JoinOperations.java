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
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.select.join.JoinType;
import org.mybatis.dynamic.sql.util.Buildable;

public interface JoinOperations<D extends JoinOperations<D, F>, F extends JoinOperations.JoinSpecificationStarter<F>> {

    default F join(SqlTable joinTable) {
        return buildFinisher(JoinType.INNER, joinTable);
    }

    default F join(SqlTable joinTable, String tableAlias) {
        addTableAlias(joinTable, tableAlias);
        return join(joinTable);
    }

    default F join(Buildable<SelectModel> joinTable, @Nullable String tableAlias) {
        return buildFinisher(JoinType.INNER, buildSubQuery(joinTable, tableAlias));
    }

    default F leftJoin(SqlTable joinTable) {
        return buildFinisher(JoinType.LEFT, joinTable);
    }

    default F leftJoin(SqlTable joinTable, String tableAlias) {
        addTableAlias(joinTable, tableAlias);
        return leftJoin(joinTable);
    }

    default F leftJoin(Buildable<SelectModel> joinTable, @Nullable String tableAlias) {
        return buildFinisher(JoinType.LEFT, buildSubQuery(joinTable, tableAlias));
    }

    default F rightJoin(SqlTable joinTable) {
        return buildFinisher(JoinType.RIGHT, joinTable);
    }

    default F rightJoin(SqlTable joinTable, String tableAlias) {
        addTableAlias(joinTable, tableAlias);
        return rightJoin(joinTable);
    }

    default F rightJoin(Buildable<SelectModel> joinTable, @Nullable String tableAlias) {
        return buildFinisher(JoinType.RIGHT, buildSubQuery(joinTable, tableAlias));
    }

    default F fullJoin(SqlTable joinTable) {
        return buildFinisher(JoinType.FULL, joinTable);
    }

    default F fullJoin(SqlTable joinTable, String tableAlias) {
        addTableAlias(joinTable, tableAlias);
        return fullJoin(joinTable);
    }

    default F fullJoin(Buildable<SelectModel> joinTable, @Nullable String tableAlias) {
        return buildFinisher(JoinType.FULL, buildSubQuery(joinTable, tableAlias));
    }

    // these are the "old style" join methods. They are a complete expression of a join specification
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
        return getDsl();
    }

    default D join(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                   List<AndOrCriteriaGroup> andJoinCriteria) {
        join(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
    }

    default D join(Buildable<SelectModel> subQuery, @Nullable String tableAlias, SqlCriterion onJoinCriterion,
                   List<AndOrCriteriaGroup> andJoinCriteria) {
        join(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
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
        leftJoin(joinTable).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
    }

    default D leftJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       List<AndOrCriteriaGroup> andJoinCriteria) {
        leftJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
    }

    default D leftJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                       SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        leftJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
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
        return getDsl();
    }

    default D rightJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                        List<AndOrCriteriaGroup> andJoinCriteria) {
        rightJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
    }

    default D rightJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                        SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        rightJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
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
        return getDsl();
    }

    default D fullJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       List<AndOrCriteriaGroup> andJoinCriteria) {
        fullJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
    }

    default D fullJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                       SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        fullJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
    }

    void addTableAlias(SqlTable table, String tableAlias);

    private SubQuery buildSubQuery(Buildable<SelectModel> selectModel, @Nullable String alias) {
        return new SubQuery.Builder()
                .withSelectModel(selectModel.build())
                .withAlias(alias)
                .build();
    }

    // this is similar in function to the "where()" method in WhereOperations. Needs a better name
    F buildFinisher(JoinType joinType, TableExpression joinTable);
    // this is an unfortunate leak of a method into the public API. SHould maybe be something like addJoinSpecification
    D getDsl();

    abstract class JoinSpecificationStarter<F extends JoinSpecificationStarter<F>> extends AbstractBooleanOperations<F> {
        private final TableExpression joinTable;
        private final JoinType joinType;

        protected JoinSpecificationStarter(JoinType joinType, TableExpression joinTable) {
            this.joinType = joinType;
            this.joinTable = joinTable;
        }

        public F on(SqlCriterion sqlCriterion) {
            setInitialCriterion(sqlCriterion);
            return getThis();
        }

        public <T> F on(BindableColumn<T> joinColumn, RenderableCondition<T> joinCondition) {
            ColumnAndConditionCriterion<T> criterion = ColumnAndConditionCriterion.withColumn(joinColumn)
                    .withCondition(joinCondition)
                    .build();

            setInitialCriterion(criterion);
            return getThis();
        }

        public <T> F on(BindableColumn<T> joinColumn, RenderableCondition<T> onJoinCondition,
                        AndOrCriteriaGroup... subCriteria) {
            ColumnAndConditionCriterion<T> criterion = ColumnAndConditionCriterion.withColumn(joinColumn)
                    .withCondition(onJoinCondition)
                    .withSubCriteria(Arrays.asList(subCriteria))
                    .build();

            setInitialCriterion(criterion);
            return getThis();
        }

        protected JoinSpecification buildJoinSpecification() {
            return JoinSpecification.withJoinTable(joinTable)
                    .withJoinType(joinType)
                    .withInitialCriterion(initialCriterion)
                    .withSubCriteria(subCriteria)
                    .build();
        }
    }
}
