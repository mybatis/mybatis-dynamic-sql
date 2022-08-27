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
package org.mybatis.dynamic.sql.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.TableExpression;
import org.mybatis.dynamic.sql.exception.DuplicateTableAliasException;
import org.mybatis.dynamic.sql.select.join.JoinCriterion;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.select.join.JoinType;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.where.AbstractWhereDSL;
import org.mybatis.dynamic.sql.where.AbstractWhereSupport;

public abstract class AbstractQueryExpressionDSL<W extends AbstractWhereDSL<?>,
            T extends AbstractQueryExpressionDSL<W, T>>
        extends AbstractWhereSupport<W, T> {

    private final List<JoinSpecification.Builder> joinSpecificationBuilders = new ArrayList<>();
    private final Map<SqlTable, String> tableAliases = new HashMap<>();
    private final TableExpression table;

    protected AbstractQueryExpressionDSL(TableExpression table) {
        this.table = Objects.requireNonNull(table);
    }

    public TableExpression table() {
        return table;
    }

    public T join(SqlTable joinTable, JoinCriterion onJoinCriterion,
            JoinCriterion... andJoinCriteria) {
        addJoinSpecificationBuilder(joinTable, onJoinCriterion, JoinType.INNER, Arrays.asList(andJoinCriteria));
        return getThis();
    }

    public T join(SqlTable joinTable, String tableAlias, JoinCriterion onJoinCriterion,
            JoinCriterion... andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return join(joinTable, onJoinCriterion, andJoinCriteria);
    }

    public T join(SqlTable joinTable, JoinCriterion onJoinCriterion,
            List<JoinCriterion> andJoinCriteria) {
        addJoinSpecificationBuilder(joinTable, onJoinCriterion, JoinType.INNER, andJoinCriteria);
        return getThis();
    }

    public T join(SqlTable joinTable, String tableAlias, JoinCriterion onJoinCriterion,
            List<JoinCriterion> andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return join(joinTable, onJoinCriterion, andJoinCriteria);
    }

    public T join(Buildable<SelectModel> subQuery, String tableAlias, JoinCriterion onJoinCriterion,
                  List<JoinCriterion> andJoinCriteria) {
        addJoinSpecificationBuilder(buildSubQuery(subQuery, tableAlias), onJoinCriterion, JoinType.INNER,
                andJoinCriteria);
        return getThis();
    }

    public T leftJoin(SqlTable joinTable, JoinCriterion onJoinCriterion,
            JoinCriterion... andJoinCriteria) {
        addJoinSpecificationBuilder(joinTable, onJoinCriterion, JoinType.LEFT, Arrays.asList(andJoinCriteria));
        return getThis();
    }

    public T leftJoin(SqlTable joinTable, String tableAlias, JoinCriterion onJoinCriterion,
            JoinCriterion... andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return leftJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    public T leftJoin(SqlTable joinTable, JoinCriterion onJoinCriterion,
            List<JoinCriterion> andJoinCriteria) {
        addJoinSpecificationBuilder(joinTable, onJoinCriterion, JoinType.LEFT, andJoinCriteria);
        return getThis();
    }

    public T leftJoin(SqlTable joinTable, String tableAlias, JoinCriterion onJoinCriterion,
            List<JoinCriterion> andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return leftJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    public T leftJoin(Buildable<SelectModel> subQuery, String tableAlias, JoinCriterion onJoinCriterion,
                      List<JoinCriterion> andJoinCriteria) {
        addJoinSpecificationBuilder(buildSubQuery(subQuery, tableAlias), onJoinCriterion, JoinType.LEFT,
                andJoinCriteria);
        return getThis();
    }

    public T rightJoin(SqlTable joinTable, JoinCriterion onJoinCriterion,
            JoinCriterion... andJoinCriteria) {
        addJoinSpecificationBuilder(joinTable, onJoinCriterion, JoinType.RIGHT, Arrays.asList(andJoinCriteria));
        return getThis();
    }

    public T rightJoin(SqlTable joinTable, String tableAlias, JoinCriterion onJoinCriterion,
            JoinCriterion... andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return rightJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    public T rightJoin(SqlTable joinTable, JoinCriterion onJoinCriterion,
            List<JoinCriterion> andJoinCriteria) {
        addJoinSpecificationBuilder(joinTable, onJoinCriterion, JoinType.RIGHT, andJoinCriteria);
        return getThis();
    }

    public T rightJoin(SqlTable joinTable, String tableAlias, JoinCriterion onJoinCriterion,
            List<JoinCriterion> andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return rightJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    public T rightJoin(Buildable<SelectModel> subQuery, String tableAlias, JoinCriterion onJoinCriterion,
                      List<JoinCriterion> andJoinCriteria) {
        addJoinSpecificationBuilder(buildSubQuery(subQuery, tableAlias), onJoinCriterion, JoinType.RIGHT,
                andJoinCriteria);
        return getThis();
    }

    public T fullJoin(SqlTable joinTable, JoinCriterion onJoinCriterion,
            JoinCriterion... andJoinCriteria) {
        addJoinSpecificationBuilder(joinTable, onJoinCriterion, JoinType.FULL, Arrays.asList(andJoinCriteria));
        return getThis();
    }

    public T fullJoin(SqlTable joinTable, String tableAlias, JoinCriterion onJoinCriterion,
            JoinCriterion... andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return fullJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    public T fullJoin(SqlTable joinTable, JoinCriterion onJoinCriterion,
            List<JoinCriterion> andJoinCriteria) {
        addJoinSpecificationBuilder(joinTable, onJoinCriterion, JoinType.FULL, andJoinCriteria);
        return getThis();
    }

    public T fullJoin(SqlTable joinTable, String tableAlias, JoinCriterion onJoinCriterion,
            List<JoinCriterion> andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return fullJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    public T fullJoin(Buildable<SelectModel> subQuery, String tableAlias, JoinCriterion onJoinCriterion,
                  List<JoinCriterion> andJoinCriteria) {
        addJoinSpecificationBuilder(buildSubQuery(subQuery, tableAlias), onJoinCriterion, JoinType.FULL,
                andJoinCriteria);
        return getThis();
    }

    private void addJoinSpecificationBuilder(TableExpression joinTable, JoinCriterion onJoinCriterion,
            JoinType joinType, List<JoinCriterion> andJoinCriteria) {
        joinSpecificationBuilders.add(new JoinSpecification.Builder()
                .withJoinTable(joinTable)
                .withJoinType(joinType)
                .withJoinCriterion(onJoinCriterion)
                .withJoinCriteria(andJoinCriteria));
    }

    protected void addJoinSpecificationBuilder(JoinSpecification.Builder builder) {
        joinSpecificationBuilders.add(builder);
    }

    protected Optional<JoinModel> buildJoinModel() {
        if (joinSpecificationBuilders.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(JoinModel.of(joinSpecificationBuilders.stream()
                .map(JoinSpecification.Builder::build)
                .collect(Collectors.toList())));
    }

    protected void addTableAlias(SqlTable table, String tableAlias) {
        if (tableAliases.containsKey(table)) {
            throw new DuplicateTableAliasException(table, tableAlias, tableAliases.get(table));
        }

        tableAliases.put(table, tableAlias);
    }

    protected Map<SqlTable, String> tableAliases() {
        return Collections.unmodifiableMap(tableAliases);
    }

    protected static SubQuery buildSubQuery(Buildable<SelectModel> selectModel) {
        return new SubQuery.Builder()
                .withSelectModel(selectModel.build())
                .build();
    }

    protected static SubQuery buildSubQuery(Buildable<SelectModel> selectModel, String alias) {
        return new SubQuery.Builder()
                .withSelectModel(selectModel.build())
                .withAlias(alias)
                .build();
    }

    protected abstract T getThis();
}
