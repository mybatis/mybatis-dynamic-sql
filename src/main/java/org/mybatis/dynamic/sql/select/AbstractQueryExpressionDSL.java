/**
 *    Copyright 2016-2019 the original author or authors.
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
package org.mybatis.dynamic.sql.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.select.join.JoinCriterion;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.select.join.JoinType;
import org.mybatis.dynamic.sql.util.Buildable;

public abstract class AbstractQueryExpressionDSL<T extends AbstractQueryExpressionDSL<T, R>, R> 
        implements Buildable<R> {

    private List<JoinSpecification.Builder> joinSpecificationBuilders = new ArrayList<>();
    protected Map<SqlTable, String> tableAliases = new HashMap<>();
    private SqlTable table;
    
    protected AbstractQueryExpressionDSL(SqlTable table) {
        this.table = Objects.requireNonNull(table);
    }
    
    public SqlTable table() {
        return table;
    }
    
    public T join(SqlTable joinTable, JoinCriterion onJoinCriterion,
            JoinCriterion...andJoinCriteria) {
        addJoinSpecificationBuilder(joinTable, onJoinCriterion, JoinType.INNER, Arrays.asList(andJoinCriteria));
        return getThis();
    }
    
    public T join(SqlTable joinTable, String tableAlias, JoinCriterion onJoinCriterion,
            JoinCriterion...andJoinCriteria) {
        tableAliases.put(joinTable, tableAlias);
        return join(joinTable, onJoinCriterion, andJoinCriteria);
    }
    
    public T join(SqlTable joinTable, JoinCriterion onJoinCriterion,
            List<JoinCriterion> andJoinCriteria) {
        addJoinSpecificationBuilder(joinTable, onJoinCriterion, JoinType.INNER, andJoinCriteria);
        return getThis();
    }
    
    public T join(SqlTable joinTable, String tableAlias, JoinCriterion onJoinCriterion,
            List<JoinCriterion> andJoinCriteria) {
        tableAliases.put(joinTable, tableAlias);
        return join(joinTable, onJoinCriterion, andJoinCriteria);
    }
    
    public T leftJoin(SqlTable joinTable, JoinCriterion onJoinCriterion,
            JoinCriterion...andJoinCriteria) {
        addJoinSpecificationBuilder(joinTable, onJoinCriterion, JoinType.LEFT, Arrays.asList(andJoinCriteria));
        return getThis();
    }
    
    public T leftJoin(SqlTable joinTable, String tableAlias, JoinCriterion onJoinCriterion,
            JoinCriterion...andJoinCriteria) {
        tableAliases.put(joinTable, tableAlias);
        return leftJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }
    
    public T leftJoin(SqlTable joinTable, JoinCriterion onJoinCriterion,
            List<JoinCriterion> andJoinCriteria) {
        addJoinSpecificationBuilder(joinTable, onJoinCriterion, JoinType.LEFT, andJoinCriteria);
        return getThis();
    }
    
    public T leftJoin(SqlTable joinTable, String tableAlias, JoinCriterion onJoinCriterion,
            List<JoinCriterion> andJoinCriteria) {
        tableAliases.put(joinTable, tableAlias);
        return leftJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }
    
    public T rightJoin(SqlTable joinTable, JoinCriterion onJoinCriterion,
            JoinCriterion...andJoinCriteria) {
        addJoinSpecificationBuilder(joinTable, onJoinCriterion, JoinType.RIGHT, Arrays.asList(andJoinCriteria));
        return getThis();
    }
    
    public T rightJoin(SqlTable joinTable, String tableAlias, JoinCriterion onJoinCriterion,
            JoinCriterion...andJoinCriteria) {
        tableAliases.put(joinTable, tableAlias);
        return rightJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    public T rightJoin(SqlTable joinTable, JoinCriterion onJoinCriterion,
            List<JoinCriterion> andJoinCriteria) {
        addJoinSpecificationBuilder(joinTable, onJoinCriterion, JoinType.RIGHT, andJoinCriteria);
        return getThis();
    }
    
    public T rightJoin(SqlTable joinTable, String tableAlias, JoinCriterion onJoinCriterion,
            List<JoinCriterion> andJoinCriteria) {
        tableAliases.put(joinTable, tableAlias);
        return rightJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    public T fullJoin(SqlTable joinTable, JoinCriterion onJoinCriterion,
            JoinCriterion...andJoinCriteria) {
        addJoinSpecificationBuilder(joinTable, onJoinCriterion, JoinType.FULL, Arrays.asList(andJoinCriteria));
        return getThis();
    }
    
    public T fullJoin(SqlTable joinTable, String tableAlias, JoinCriterion onJoinCriterion,
            JoinCriterion...andJoinCriteria) {
        tableAliases.put(joinTable, tableAlias);
        return fullJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    public T fullJoin(SqlTable joinTable, JoinCriterion onJoinCriterion,
            List<JoinCriterion> andJoinCriteria) {
        addJoinSpecificationBuilder(joinTable, onJoinCriterion, JoinType.FULL, andJoinCriteria);
        return getThis();
    }
    
    public T fullJoin(SqlTable joinTable, String tableAlias, JoinCriterion onJoinCriterion,
            List<JoinCriterion> andJoinCriteria) {
        tableAliases.put(joinTable, tableAlias);
        return fullJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    private void addJoinSpecificationBuilder(SqlTable joinTable, JoinCriterion onJoinCriterion, JoinType joinType,
            List<JoinCriterion> andJoinCriteria) {
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
    
    protected abstract T getThis();
}
