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

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.select.join.JoinCondition;
import org.mybatis.dynamic.sql.select.join.JoinCriterion;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.select.join.JoinType;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.where.AbstractWhereDSL;
import org.mybatis.dynamic.sql.where.WhereModel;

public class QueryExpressionDSL {

    private String connector;
    private SelectDSL selectModelBuilder;
    private boolean isDistinct;
    private List<BasicColumn> selectList;
    private SqlTable table;
    private Map<SqlTable, String> tableAliases = new HashMap<>();
    private WhereModel whereModel;
    private GroupByModel groupByModel;
    private JoinModel joinModel;
    private List<JoinSpecification> joinSpecifications = new ArrayList<>();
    
    private QueryExpressionDSL(Builder builder) {
        connector = builder.connector;
        selectList = Arrays.asList(builder.selectList);
        isDistinct = builder.isDistinct;
        selectModelBuilder = Objects.requireNonNull(builder.selectModelBuilder);
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
        private BasicColumn[] selectList;
        private boolean isDistinct;
        private SelectDSL selectModelBuilder;

        public Builder withConnector(String connector) {
            this.connector = connector;
            return this;
        }
        
        public Builder withSelectList(BasicColumn...selectList) {
            this.selectList = selectList;
            return this;
        }
        
        public Builder isDistinct() {
            this.isDistinct = true;
            return this;
        }
        
        public Builder withSelectModelBuilder(SelectDSL selectModelBuilder) {
            this.selectModelBuilder = selectModelBuilder;
            return this;
        }
        
        public QueryExpressionDSL build() {
            return new QueryExpressionDSL(this);
        }
    }
    
    protected QueryExpressionModel buildModel() {
        return new QueryExpressionModel.Builder()
                .withConnector(connector)
                .withTable(table)
                .isDistinct(isDistinct)
                .withColumns(selectList)
                .withTableAliases(tableAliases)
                .withWhereModel(whereModel)
                .withJoinModel(joinModel)
                .withGroupByModel(groupByModel)
                .build();
    }
    
    public class QueryExpressionAfterFromBuilder implements Buildable<SelectModel> {
        private QueryExpressionAfterFromBuilder() {
            super();
        }
        
        public <T> QueryExpressionWhereBuilder where(BindableColumn<T> column, VisitableCondition<T> condition) {
            return new QueryExpressionWhereBuilder(column, condition);
        }

        public <T> QueryExpressionWhereBuilder where(BindableColumn<T> column, VisitableCondition<T> condition,
                SqlCriterion<?>...subCriteria) {
            return new QueryExpressionWhereBuilder(column, condition, subCriteria);
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

        public GroupByFinisher groupBy(BasicColumn...columns) {
            groupByModel = GroupByModel.of(columns);
            selectModelBuilder.addQueryExpression(buildModel());
            return new GroupByFinisher();
        }
        
        public SelectDSL orderBy(SqlColumn<?>...columns) {
            selectModelBuilder.addQueryExpression(buildModel());
            selectModelBuilder.setOrderByModel(OrderByModel.of(columns));
            return selectModelBuilder;
        }

        public UnionBuilder union() {
            selectModelBuilder.addQueryExpression(buildModel());
            return new UnionBuilder();
        }
    }
    
    public class QueryExpressionWhereBuilder extends AbstractWhereDSL<QueryExpressionWhereBuilder>
            implements Buildable<SelectModel> {
        private <T> QueryExpressionWhereBuilder(BindableColumn<T> column, VisitableCondition<T> condition) {
            super(column, condition);
        }
        
        private <T> QueryExpressionWhereBuilder(BindableColumn<T> column, VisitableCondition<T> condition,
                SqlCriterion<?>...subCriteria) {
            super(column, condition, subCriteria);
        }
        
        public UnionBuilder union() {
            whereModel = buildWhereModel();
            selectModelBuilder.addQueryExpression(buildModel());
            return new UnionBuilder();
        }

        public SelectDSL orderBy(SqlColumn<?>...columns) {
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
        protected QueryExpressionWhereBuilder getThis() {
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

        public JoinSpecificationFinisher on(BasicColumn joinColumn, JoinCondition joinCondition) {
            return new JoinSpecificationFinisher(joinTable, joinColumn, joinCondition, joinType);
        }

        public JoinSpecificationFinisher on(BasicColumn joinColumn, JoinCondition joinCondition,
                JoinCriterion...joinCriteria) {
            return new JoinSpecificationFinisher(joinTable, joinColumn, joinCondition, joinType, joinCriteria);
        }
    }

    public class JoinSpecificationFinisher implements Buildable<SelectModel> {

        private SqlTable joinTable;
        private List<JoinCriterion> joinCriteria = new ArrayList<>();
        private JoinType joinType;
        
        public JoinSpecificationFinisher(SqlTable table, BasicColumn joinColumn,
                JoinCondition joinCondition, JoinType joinType) {
            this.joinTable = table;
            this.joinType = joinType;

            JoinCriterion joinCriterion = new JoinCriterion.Builder()
                    .withJoinColumn(joinColumn)
                    .withJoinCondition(joinCondition)
                    .withConnector("on") //$NON-NLS-1$
                    .build();
            
            joinCriteria.add(joinCriterion);
        }

        public JoinSpecificationFinisher(SqlTable table, BasicColumn joinColumn,
                JoinCondition joinCondition, JoinType joinType, JoinCriterion...joinCriteria) {
            this.joinTable = table;
            this.joinType = joinType;

            JoinCriterion joinCriterion = new JoinCriterion.Builder()
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
        
        public <T> QueryExpressionWhereBuilder where(BindableColumn<T> column, VisitableCondition<T> condition) {
            joinModel = buildJoinModel();
            return new QueryExpressionWhereBuilder(column, condition);
        }

        public <T> QueryExpressionWhereBuilder where(BindableColumn<T> column, VisitableCondition<T> condition,
                SqlCriterion<?>...subCriteria) {
            joinModel = buildJoinModel();
            return new QueryExpressionWhereBuilder(column, condition, subCriteria);
        }

        public JoinSpecificationFinisher and(BasicColumn joinColumn, JoinCondition joinCondition) {
            JoinCriterion joinCriterion = new JoinCriterion.Builder()
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

        public SelectDSL orderBy(SqlColumn<?>...columns) {
            joinModel = buildJoinModel();
            selectModelBuilder.addQueryExpression(buildModel());
            selectModelBuilder.setOrderByModel(OrderByModel.of(columns));
            return selectModelBuilder;
        }
    }
    
    public class GroupByFinisher implements Buildable<SelectModel> {
        public SelectDSL orderBy(SqlColumn<?>...columns) {
            selectModelBuilder.setOrderByModel(OrderByModel.of(columns));
            return selectModelBuilder;
        }

        @Override
        public SelectModel build() {
            return selectModelBuilder.build();
        }
    }
    
    public class UnionBuilder {
        public QueryExpressionDSL select(BasicColumn...selectList) {
            return new QueryExpressionDSL.Builder()
                    .withConnector("union") //$NON-NLS-1$
                    .withSelectList(selectList)
                    .withSelectModelBuilder(selectModelBuilder)
                    .build();
        }
        
        public QueryExpressionDSL selectDistinct(BasicColumn...selectList) {
            return new QueryExpressionDSL.Builder()
                    .withConnector("union") //$NON-NLS-1$
                    .withSelectList(selectList)
                    .isDistinct()
                    .withSelectModelBuilder(selectModelBuilder)
                    .build();
        }
    }
}
