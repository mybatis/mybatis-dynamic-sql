/**
 *    Copyright 2016-2017 the original author or authors.
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

import org.mybatis.dynamic.sql.SelectListItem;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.select.join.JoinCondition;
import org.mybatis.dynamic.sql.select.join.JoinCriterion;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.select.join.JoinType;
import org.mybatis.dynamic.sql.where.AbstractWhereModelBuilder;
import org.mybatis.dynamic.sql.where.WhereModel;

// TODO - union in more places
public class QueryExpressionBuilder {

    private String connector;
    private SelectModelBuilder selectModelBuilder;
    private boolean isDistinct;
    private List<SelectListItem> selectList;
    private SqlTable table;
    private Map<SqlTable, String> tableAliases = new HashMap<>();
    private WhereModel whereModel;
    private JoinModel joinModel;
    private List<JoinSpecification> joinSpecifications = new ArrayList<>();
    
    private QueryExpressionBuilder(Builder builder) {
        this.connector = builder.connector;
        this.selectList = Arrays.asList(builder.selectList);
        this.isDistinct = builder.isDistinct;
        this.selectModelBuilder = Objects.requireNonNull(builder.selectModelBuilder);
    }
    
    public QueryExpressionAfterFromBuilder from(SqlTable table) {
        this.table = table;
        return new QueryExpressionAfterFromBuilder();
    }

    public QueryExpressionAfterFromBuilder from(SqlTable table, String tableAlias) {
        this.table = table;
        tableAliases.put(table, tableAlias);
        return new QueryExpressionAfterFromBuilder();
    }

    public static class Builder {
        private String connector;
        private SelectListItem[] selectList;
        private boolean isDistinct;
        private SelectModelBuilder selectModelBuilder;

        public Builder withConnector(String connector) {
            this.connector = connector;
            return this;
        }
        
        public Builder withSelectList(SelectListItem...selectList) {
            this.selectList = selectList;
            return this;
        }
        
        public Builder isDistinct() {
            this.isDistinct = true;
            return this;
        }
        
        public Builder withSelectModelBuilder(SelectModelBuilder selectModelBuilder) {
            this.selectModelBuilder = selectModelBuilder;
            return this;
        }
        
        public QueryExpressionBuilder build() {
            return new QueryExpressionBuilder(this);
        }
    }
    
    protected QueryExpression buildModel() {
        return new QueryExpression.Builder()
                .withConnector(connector)
                .withTable(table)
                .isDistinct(isDistinct)
                .withColumns(selectList)
                .withTableAliases(tableAliases)
                .withWhereModel(whereModel)
                .withJoinModel(joinModel)
                .build();
    }
    
    public class QueryExpressionAfterFromBuilder implements Buildable<SelectModel> {
        private QueryExpressionAfterFromBuilder() {
            super();
        }
        
        public <T> SelectSupportWhereBuilder where(SqlColumn<T> column, VisitableCondition<T> condition) {
            return new SelectSupportWhereBuilder(column, condition);
        }

        public <T> SelectSupportWhereBuilder where(SqlColumn<T> column, VisitableCondition<T> condition,
                SqlCriterion<?>...subCriteria) {
            return new SelectSupportWhereBuilder(column, condition, subCriteria);
        }
        
        @Override
        public SelectModel build() {
            selectModelBuilder.addQueryExpression(buildModel());
            return selectModelBuilder.build();
        }

        public JoinSpecificationStarter join(SqlTable joinTable) {
            return new JoinSpecificationStarter(joinTable, JoinType.INNER);
        }
        
        public JoinSpecificationStarter join(SqlTable joinTable, String tableAlias) {
            tableAliases.put(joinTable, tableAlias);
            return join(joinTable);
        }

        public JoinSpecificationStarter leftJoin(SqlTable joinTable) {
            return new JoinSpecificationStarter(joinTable, JoinType.LEFT);
        }
        
        public JoinSpecificationStarter leftJoin(SqlTable joinTable, String tableAlias) {
            tableAliases.put(joinTable, tableAlias);
            return leftJoin(joinTable);
        }

        public JoinSpecificationStarter rightJoin(SqlTable joinTable) {
            return new JoinSpecificationStarter(joinTable, JoinType.RIGHT);
        }
        
        public JoinSpecificationStarter rightJoin(SqlTable joinTable, String tableAlias) {
            tableAliases.put(joinTable, tableAlias);
            return rightJoin(joinTable);
        }

        public JoinSpecificationStarter fullJoin(SqlTable joinTable) {
            return new JoinSpecificationStarter(joinTable, JoinType.FULL);
        }
        
        public JoinSpecificationStarter fullJoin(SqlTable joinTable, String tableAlias) {
            tableAliases.put(joinTable, tableAlias);
            return fullJoin(joinTable);
        }

        public SelectModelBuilder orderBy(SqlColumn<?>...columns) {
            selectModelBuilder.addQueryExpression(buildModel());
            selectModelBuilder.setOrderByModel(OrderByModel.of(columns));
            return selectModelBuilder;
        }
    }
    
    public class SelectSupportWhereBuilder extends AbstractWhereModelBuilder<SelectSupportWhereBuilder>
            implements Buildable<SelectModel> {
        private <T> SelectSupportWhereBuilder(SqlColumn<T> column, VisitableCondition<T> condition) {
            super(column, condition);
        }
        
        private <T> SelectSupportWhereBuilder(SqlColumn<T> column, VisitableCondition<T> condition,
                SqlCriterion<?>...subCriteria) {
            super(column, condition, subCriteria);
        }
        
        public UnionBuilder union() {
            whereModel = buildWhereModel();
            selectModelBuilder.addQueryExpression(buildModel());
            return new UnionBuilder();
        }

        public SelectModelBuilder orderBy(SqlColumn<?>...columns) {
            whereModel = buildWhereModel();
            selectModelBuilder.addQueryExpression(buildModel());
            selectModelBuilder.setOrderByModel(OrderByModel.of(columns));
            return selectModelBuilder;
        }
        
        @Override
        public SelectModel build() {
            whereModel = buildWhereModel();
            selectModelBuilder.addQueryExpression(buildModel());
            return selectModelBuilder.build();
        }
        
        @Override
        protected SelectSupportWhereBuilder getThis() {
            return this;
        }
    }
    
    public class JoinSpecificationStarter {
        private SqlTable joinTable;
        private JoinType joinType;
        
        public JoinSpecificationStarter(SqlTable joinTable, JoinType joinType) {
            this.joinTable = joinTable;
            this.joinType = joinType;
        }

        public <T> JoinSpecificationFinisher on(SqlColumn<T> joinColumn, JoinCondition<T> joinCondition) {
            return new JoinSpecificationFinisher(joinTable, joinColumn, joinCondition, joinType);
        }

        public <T> JoinSpecificationFinisher on(SqlColumn<T> joinColumn, JoinCondition<T> joinCondition,
                JoinCriterion<?>...joinCriteria) {
            return new JoinSpecificationFinisher(joinTable, joinColumn, joinCondition, joinType, joinCriteria);
        }
    }

    public class JoinSpecificationFinisher implements Buildable<SelectModel> {

        private SqlTable joinTable;
        private List<JoinCriterion<?>> joinCriteria = new ArrayList<>();
        private JoinType joinType;
        
        public <T> JoinSpecificationFinisher(SqlTable table, SqlColumn<T> joinColumn,
                JoinCondition<T> joinCondition, JoinType joinType) {
            this.joinTable = table;
            this.joinType = joinType;

            JoinCriterion<T> joinCriterion = new JoinCriterion.Builder<T>()
                    .withJoinColumn(joinColumn)
                    .withJoinCondition(joinCondition)
                    .withConnector("on") //$NON-NLS-1$
                    .build();
            
            joinCriteria.add(joinCriterion);
        }

        public <T> JoinSpecificationFinisher(SqlTable table, SqlColumn<T> joinColumn,
                JoinCondition<T> joinCondition, JoinType joinType, JoinCriterion<?>...joinCriteria) {
            this.joinTable = table;
            this.joinType = joinType;

            JoinCriterion<T> joinCriterion = new JoinCriterion.Builder<T>()
                    .withJoinColumn(joinColumn)
                    .withJoinCondition(joinCondition)
                    .withConnector("on") //$NON-NLS-1$
                    .build();
            
            this.joinCriteria.add(joinCriterion);
            this.joinCriteria.addAll(Arrays.asList(joinCriteria));
        }
        
        protected JoinSpecification buildJoinSpecification() {
            return new JoinSpecification.Builder()
                    .withJoinCriteria(joinCriteria)
                    .withJoinTable(joinTable)
                    .withJoinType(joinType)
                    .build();
        }
        
        protected JoinModel buildJoinModel() {
            joinSpecifications.add(buildJoinSpecification());
            return JoinModel.of(joinSpecifications);
        }
        
        @Override
        public SelectModel build() {
            joinModel = buildJoinModel();
            selectModelBuilder.addQueryExpression(buildModel());
            return selectModelBuilder.build();
        }
        
        public <T> SelectSupportWhereBuilder where(SqlColumn<T> column, VisitableCondition<T> condition) {
            joinModel = buildJoinModel();
            return new SelectSupportWhereBuilder(column, condition);
        }

        public <T> SelectSupportWhereBuilder where(SqlColumn<T> column, VisitableCondition<T> condition,
                SqlCriterion<?>...subCriteria) {
            joinModel = buildJoinModel();
            return new SelectSupportWhereBuilder(column, condition, subCriteria);
        }

        public <T> JoinSpecificationFinisher and(SqlColumn<T> joinColumn, JoinCondition<T> joinCondition) {
            JoinCriterion<T> joinCriterion = new JoinCriterion.Builder<T>()
                    .withJoinColumn(joinColumn)
                    .withJoinCondition(joinCondition)
                    .withConnector("and") //$NON-NLS-1$
                    .build();
            this.joinCriteria.add(joinCriterion);
            return this;
        }

        public JoinSpecificationStarter join(SqlTable joinTable) {
            joinSpecifications.add(buildJoinSpecification());
            return new JoinSpecificationStarter(joinTable, JoinType.INNER);
        }
        
        public JoinSpecificationStarter join(SqlTable joinTable, String tableAlias) {
            tableAliases.put(joinTable, tableAlias);
            return join(joinTable);
        }

        public JoinSpecificationStarter leftJoin(SqlTable joinTable) {
            joinSpecifications.add(buildJoinSpecification());
            return new JoinSpecificationStarter(joinTable, JoinType.LEFT);
        }
        
        public JoinSpecificationStarter leftJoin(SqlTable joinTable, String tableAlias) {
            tableAliases.put(joinTable, tableAlias);
            return leftJoin(joinTable);
        }

        public JoinSpecificationStarter rightJoin(SqlTable joinTable) {
            joinSpecifications.add(buildJoinSpecification());
            return new JoinSpecificationStarter(joinTable, JoinType.RIGHT);
        }
        
        public JoinSpecificationStarter rightJoin(SqlTable joinTable, String tableAlias) {
            tableAliases.put(joinTable, tableAlias);
            return rightJoin(joinTable);
        }

        public JoinSpecificationStarter fullJoin(SqlTable joinTable) {
            joinSpecifications.add(buildJoinSpecification());
            return new JoinSpecificationStarter(joinTable, JoinType.FULL);
        }
        
        public JoinSpecificationStarter fullJoin(SqlTable joinTable, String tableAlias) {
            tableAliases.put(joinTable, tableAlias);
            return fullJoin(joinTable);
        }

        public SelectModelBuilder orderBy(SqlColumn<?>...columns) {
            joinModel = buildJoinModel();
            selectModelBuilder.addQueryExpression(buildModel());
            selectModelBuilder.setOrderByModel(OrderByModel.of(columns));
            return selectModelBuilder;
        }
    }
    
    public class UnionBuilder {
        public QueryExpressionBuilder select(SelectListItem...selectList) {
            return new QueryExpressionBuilder.Builder()
                    .withConnector("union") //$NON-NLS-1$
                    .withSelectList(selectList)
                    .withSelectModelBuilder(selectModelBuilder)
                    .build();
        }
        
        public QueryExpressionBuilder selectDistinct(SelectListItem...selectList) {
            return new QueryExpressionBuilder.Builder()
                    .withConnector("union") //$NON-NLS-1$
                    .withSelectList(selectList)
                    .isDistinct()
                    .withSelectModelBuilder(selectModelBuilder)
                    .build();
        }
    }
}
