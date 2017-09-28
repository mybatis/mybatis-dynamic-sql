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

import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.select.join.JoinCriterion;
import org.mybatis.dynamic.sql.select.join.JoinCondition;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.where.AbstractWhereModelBuilder;
import org.mybatis.dynamic.sql.where.WhereModel;

public class SelectModelBuilder {

    private boolean isDistinct;
    private List<SqlColumn<?>> columns;
    private SqlTable table;
    private Map<SqlTable, String> tableAliases = new HashMap<>();
    private WhereModel whereModel;
    private List<SqlColumn<?>> orderByColumns;
    private JoinModel joinModel;
    private List<JoinSpecification> joinSpecifications = new ArrayList<>();
    
    private SelectModelBuilder(SqlColumn<?>...columns) {
        this.columns = Arrays.asList(columns);
    }
    
    public SelectSupportAfterFromBuilder from(SqlTable table) {
        this.table = table;
        return new SelectSupportAfterFromBuilder();
    }

    public SelectSupportAfterFromBuilder from(SqlTable table, String tableAlias) {
        this.table = table;
        tableAliases.put(table, tableAlias);
        return new SelectSupportAfterFromBuilder();
    }

    public static SelectModelBuilder of(SqlColumn<?>...columns) {
        return new SelectModelBuilder(columns);
    }
    
    public static SelectModelBuilder ofDistinct(SqlColumn<?>...columns) {
        SelectModelBuilder builder = SelectModelBuilder.of(columns);
        builder.isDistinct = true;
        return builder;
    }
    
    protected SelectModel buildModel() {
        return new SelectModel.Builder(table)
                .isDistinct(isDistinct)
                .withColumns(columns)
                .withTableAliases(tableAliases)
                .withWhereModel(whereModel)
                .withOrderByColumns(orderByColumns)
                .withJoinModel(joinModel)
                .build();
    }
    
    @FunctionalInterface
    public interface Buildable {
        SelectModel build();
    }
    
    public class SelectSupportAfterFromBuilder implements Buildable {
        private SelectSupportAfterFromBuilder() {
            super();
        }
        
        public <T> SelectSupportWhereBuilder where(SqlColumn<T> column, VisitableCondition<T> condition) {
            return new SelectSupportWhereBuilder(column, condition);
        }

        public <T> SelectSupportWhereBuilder where(SqlColumn<T> column, VisitableCondition<T> condition,
                SqlCriterion<?>...subCriteria) {
            return new SelectSupportWhereBuilder(column, condition, subCriteria);
        }
        
        public SelectSupportAfterOrderByBuilder orderBy(SqlColumn<?>...columns) {
            orderByColumns = Arrays.asList(columns);
            return new SelectSupportAfterOrderByBuilder();
        }
        
        @Override
        public SelectModel build() {
            return buildModel();
        }

        public JoinSpecificationStarter join(SqlTable joinTable) {
            return new JoinSpecificationStarter(joinTable);
        }
        
        public JoinSpecificationStarter join(SqlTable joinTable, String tableAlias) {
            tableAliases.put(joinTable, tableAlias);
            return new JoinSpecificationStarter(joinTable);
        }
    }
    
    public class SelectSupportWhereBuilder extends AbstractWhereModelBuilder<SelectSupportWhereBuilder> 
            implements Buildable {
        private <T> SelectSupportWhereBuilder(SqlColumn<T> column, VisitableCondition<T> condition) {
            super(column, condition);
        }
        
        private <T> SelectSupportWhereBuilder(SqlColumn<T> column, VisitableCondition<T> condition,
                SqlCriterion<?>...subCriteria) {
            super(column, condition, subCriteria);
        }
        
        public SelectSupportAfterOrderByBuilder orderBy(SqlColumn<?>...columns) {
            whereModel = buildWhereModel();
            orderByColumns = Arrays.asList(columns);
            return new SelectSupportAfterOrderByBuilder();
        }
        
        @Override
        public SelectModel build() {
            whereModel = buildWhereModel();
            return buildModel();
        }
        
        @Override
        protected SelectSupportWhereBuilder getThis() {
            return this;
        }
    }
    
    public class JoinSpecificationStarter {
        private SqlTable joinTable;
        
        public JoinSpecificationStarter(SqlTable joinTable) {
            this.joinTable = joinTable;
        }

        public <T> JoinSpecificationFinisher on(SqlColumn<T> joinColumn, JoinCondition<T> joinCondition) {
            return new JoinSpecificationFinisher(joinTable, joinColumn, joinCondition);
        }

        public <T> JoinSpecificationFinisher on(SqlColumn<T> joinColumn, JoinCondition<T> joinCondition,
                JoinCriterion<?>...joinCriteria) {
            return new JoinSpecificationFinisher(joinTable, joinColumn, joinCondition, joinCriteria);
        }
    }

    public class JoinSpecificationFinisher implements Buildable {

        private SqlTable joinTable;
        private List<JoinCriterion<?>> joinCriteria = new ArrayList<>();
        
        public <T> JoinSpecificationFinisher(SqlTable table, SqlColumn<T> joinColumn,
                JoinCondition<T> joinCondition) {
            this.joinTable = table;

            JoinCriterion<T> joinCriterion = new JoinCriterion.Builder<>(joinColumn, joinCondition)
                    .withConnector("on") //$NON-NLS-1$
                    .build();
            
            joinCriteria.add(joinCriterion);
        }

        public <T> JoinSpecificationFinisher(SqlTable table, SqlColumn<T> joinColumn,
                JoinCondition<T> joinCondition, JoinCriterion<?>...joinCriteria) {
            this.joinTable = table;

            JoinCriterion<T> joinCriterion = new JoinCriterion.Builder<>(joinColumn, joinCondition)
                    .withConnector("on") //$NON-NLS-1$
                    .build();
            
            this.joinCriteria.add(joinCriterion);
            this.joinCriteria.addAll(Arrays.asList(joinCriteria));
        }
        
        protected JoinSpecification buildJoinSpecification() {
            return new JoinSpecification.Builder(joinTable, joinCriteria).build();
        }
        
        protected JoinModel buildJoinModel() {
            joinSpecifications.add(buildJoinSpecification());
            return JoinModel.of(joinSpecifications);
        }
        
        @Override
        public SelectModel build() {
            joinModel = buildJoinModel();
            return buildModel();
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

        public SelectSupportAfterOrderByBuilder orderBy(SqlColumn<?>...columns) {
            joinModel = buildJoinModel();
            orderByColumns = Arrays.asList(columns);
            return new SelectSupportAfterOrderByBuilder();
        }

        public <T> JoinSpecificationFinisher and(SqlColumn<T> column, JoinCondition<T> joinCondition) {
            JoinCriterion<T> joinCriterion = new JoinCriterion.Builder<>(column, joinCondition)
                    .withConnector("and") //$NON-NLS-1$
                    .build();
            this.joinCriteria.add(joinCriterion);
            return this;
        }

        public JoinSpecificationStarter join(SqlTable joinTable) {
            joinSpecifications.add(buildJoinSpecification());
            return new JoinSpecificationStarter(joinTable);
        }
        
        public JoinSpecificationStarter join(SqlTable joinTable, String tableAlias) {
            joinSpecifications.add(buildJoinSpecification());
            tableAliases.put(joinTable, tableAlias);
            return new JoinSpecificationStarter(joinTable);
        }
    }
    
    public class SelectSupportAfterOrderByBuilder implements Buildable {
        private SelectSupportAfterOrderByBuilder() {
            super();
        }
        
        @Override
        public SelectModel build() {
            return buildModel();
        }
    }
}
