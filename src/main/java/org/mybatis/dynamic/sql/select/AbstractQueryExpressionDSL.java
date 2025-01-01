/*
 *    Copyright 2016-2025 the original author or authors.
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
import java.util.function.Supplier;

import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.TableExpression;
import org.mybatis.dynamic.sql.exception.DuplicateTableAliasException;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.select.join.JoinType;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.where.AbstractWhereFinisher;
import org.mybatis.dynamic.sql.where.AbstractWhereStarter;

public abstract class AbstractQueryExpressionDSL<W extends AbstractWhereFinisher<?>,
            T extends AbstractQueryExpressionDSL<W, T>>
        implements AbstractWhereStarter<W, T> {

    private final List<Supplier<JoinSpecification>> joinSpecificationSuppliers = new ArrayList<>();
    private final Map<SqlTable, String> tableAliases = new HashMap<>();
    private final TableExpression table;

    protected AbstractQueryExpressionDSL(TableExpression table) {
        this.table = Objects.requireNonNull(table);
    }

    public TableExpression table() {
        return table;
    }

    public T join(SqlTable joinTable, SqlCriterion onJoinCriterion,
                  AndOrCriteriaGroup... andJoinCriteria) {
        addJoinSpecificationSupplier(joinTable, onJoinCriterion, JoinType.INNER, Arrays.asList(andJoinCriteria));
        return getThis();
    }

    public T join(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                  AndOrCriteriaGroup... andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return join(joinTable, onJoinCriterion, andJoinCriteria);
    }

    public T join(SqlTable joinTable, SqlCriterion onJoinCriterion,
            List<AndOrCriteriaGroup> andJoinCriteria) {
        addJoinSpecificationSupplier(joinTable, onJoinCriterion, JoinType.INNER, andJoinCriteria);
        return getThis();
    }

    public T join(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
            List<AndOrCriteriaGroup> andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return join(joinTable, onJoinCriterion, andJoinCriteria);
    }

    public T join(Buildable<SelectModel> subQuery, String tableAlias, SqlCriterion onJoinCriterion,
                  List<AndOrCriteriaGroup> andJoinCriteria) {
        addJoinSpecificationSupplier(buildSubQuery(subQuery, tableAlias), onJoinCriterion, JoinType.INNER,
                andJoinCriteria);
        return getThis();
    }

    public T leftJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                      AndOrCriteriaGroup... andJoinCriteria) {
        addJoinSpecificationSupplier(joinTable, onJoinCriterion, JoinType.LEFT, Arrays.asList(andJoinCriteria));
        return getThis();
    }

    public T leftJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                      AndOrCriteriaGroup... andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return leftJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    public T leftJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
            List<AndOrCriteriaGroup> andJoinCriteria) {
        addJoinSpecificationSupplier(joinTable, onJoinCriterion, JoinType.LEFT, andJoinCriteria);
        return getThis();
    }

    public T leftJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
            List<AndOrCriteriaGroup> andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return leftJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    public T leftJoin(Buildable<SelectModel> subQuery, String tableAlias, SqlCriterion onJoinCriterion,
                      List<AndOrCriteriaGroup> andJoinCriteria) {
        addJoinSpecificationSupplier(buildSubQuery(subQuery, tableAlias), onJoinCriterion, JoinType.LEFT,
                andJoinCriteria);
        return getThis();
    }

    public T rightJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                       AndOrCriteriaGroup... andJoinCriteria) {
        addJoinSpecificationSupplier(joinTable, onJoinCriterion, JoinType.RIGHT, Arrays.asList(andJoinCriteria));
        return getThis();
    }

    public T rightJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       AndOrCriteriaGroup... andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return rightJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    public T rightJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
            List<AndOrCriteriaGroup> andJoinCriteria) {
        addJoinSpecificationSupplier(joinTable, onJoinCriterion, JoinType.RIGHT, andJoinCriteria);
        return getThis();
    }

    public T rightJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
            List<AndOrCriteriaGroup> andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return rightJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    public T rightJoin(Buildable<SelectModel> subQuery, String tableAlias, SqlCriterion onJoinCriterion,
                      List<AndOrCriteriaGroup> andJoinCriteria) {
        addJoinSpecificationSupplier(buildSubQuery(subQuery, tableAlias), onJoinCriterion, JoinType.RIGHT,
                andJoinCriteria);
        return getThis();
    }

    public T fullJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                      AndOrCriteriaGroup... andJoinCriteria) {
        addJoinSpecificationSupplier(joinTable, onJoinCriterion, JoinType.FULL, Arrays.asList(andJoinCriteria));
        return getThis();
    }

    public T fullJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                      AndOrCriteriaGroup... andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return fullJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    public T fullJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
            List<AndOrCriteriaGroup> andJoinCriteria) {
        addJoinSpecificationSupplier(joinTable, onJoinCriterion, JoinType.FULL, andJoinCriteria);
        return getThis();
    }

    public T fullJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
            List<AndOrCriteriaGroup> andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return fullJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    public T fullJoin(Buildable<SelectModel> subQuery, String tableAlias, SqlCriterion onJoinCriterion,
                  List<AndOrCriteriaGroup> andJoinCriteria) {
        addJoinSpecificationSupplier(buildSubQuery(subQuery, tableAlias), onJoinCriterion, JoinType.FULL,
                andJoinCriteria);
        return getThis();
    }

    private void addJoinSpecificationSupplier(TableExpression joinTable, SqlCriterion onJoinCriterion,
                                              JoinType joinType, List<AndOrCriteriaGroup> andJoinCriteria) {
        joinSpecificationSuppliers.add(() -> new JoinSpecification.Builder()
                .withJoinTable(joinTable)
                .withJoinType(joinType)
                .withInitialCriterion(onJoinCriterion)
                .withSubCriteria(andJoinCriteria).build());
    }

    protected void addJoinSpecificationSupplier(Supplier<JoinSpecification> joinSpecificationSupplier) {
        joinSpecificationSuppliers.add(joinSpecificationSupplier);
    }

    protected Optional<JoinModel> buildJoinModel() {
        if (joinSpecificationSuppliers.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(JoinModel.of(joinSpecificationSuppliers.stream()
                .map(Supplier::get)
                .toList()));
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
