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

import org.mybatis.dynamic.sql.Condition;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.select.join.JoinCondition;
import org.mybatis.dynamic.sql.select.join.JoinModel;
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
    
    public class SelectSupportAfterFromBuilder {
        private SelectSupportAfterFromBuilder() {
            super();
        }
        
        public <T> SelectSupportWhereBuilder where(SqlColumn<T> column, Condition<T> condition) {
            return new SelectSupportWhereBuilder(column, condition);
        }

        public <T> SelectSupportWhereBuilder where(SqlColumn<T> column, Condition<T> condition,
                SqlCriterion<?>...subCriteria) {
            return new SelectSupportWhereBuilder(column, condition, subCriteria);
        }
        
        public SelectSupportAfterOrderByBuilder orderBy(SqlColumn<?>...columns) {
            orderByColumns = Arrays.asList(columns);
            return new SelectSupportAfterOrderByBuilder();
        }
        
        public SelectModel build() {
            return buildModel();
        }

        public JoinSpecificationBuilder join(SqlTable joinTable) {
            return new JoinSpecificationBuilder(this, joinTable);
        }
        
        public JoinSpecificationBuilder join(SqlTable joinTable, String tableAlias) {
            tableAliases.put(joinTable, tableAlias);
            return new JoinSpecificationBuilder(this, joinTable);
        }
    }
    
    public class SelectSupportWhereBuilder extends AbstractWhereModelBuilder<SelectSupportWhereBuilder> {
        private <T> SelectSupportWhereBuilder(SqlColumn<T> column, Condition<T> condition) {
            super(column, condition);
        }
        
        private <T> SelectSupportWhereBuilder(SqlColumn<T> column, Condition<T> condition,
                SqlCriterion<?>...subCriteria) {
            super(column, condition, subCriteria);
        }
        
        public SelectSupportAfterOrderByBuilder orderBy(SqlColumn<?>...columns) {
            whereModel = buildWhereModel();
            orderByColumns = Arrays.asList(columns);
            return new SelectSupportAfterOrderByBuilder();
        }
        
        public SelectModel build() {
            whereModel = buildWhereModel();
            return buildModel();
        }
        
        @Override
        protected SelectSupportWhereBuilder getThis() {
            return this;
        }
    }
    
    public class JoinSpecificationBuilder {
        private SqlTable joinTable;
        private SelectSupportJoinBuilder joinBuilder;
        
        public JoinSpecificationBuilder(SelectSupportAfterFromBuilder ancestorBuilder, SqlTable joinTable) {
            this.joinTable = joinTable;
            joinBuilder = new SelectSupportJoinBuilder(ancestorBuilder);
        }

        public <T> SelectSupportJoinBuilder on(SqlColumn<T> joinColumn, JoinCondition<T> joinCondition) {
            joinBuilder.addJoinSpecification(JoinSpecification.of(joinTable, JoinColumnAndCondition.of(joinColumn, joinCondition)));
            return joinBuilder;
        }

        public <T> SelectSupportJoinBuilder on(SqlColumn<T> joinColumn, JoinCondition<T> joinCondition, JoinColumnAndCondition<?>...joinColumnsAndConditions) {
            joinBuilder.addJoinSpecification(JoinSpecification.of(joinTable, JoinColumnAndCondition.of(joinColumn, joinCondition), joinColumnsAndConditions));
            return joinBuilder;
        }
    }

    public class SelectSupportJoinBuilder {

        private List<JoinSpecification> joinSpecifications = new ArrayList<>();
        private SelectSupportAfterFromBuilder ancestorBuilder;
        
        public void addJoinSpecification(JoinSpecification joinSpecification) {
            joinSpecifications.add(joinSpecification);
        }
        
        public <T> SelectSupportJoinBuilder(SelectSupportAfterFromBuilder ancestorBuilder) {
            this.ancestorBuilder = ancestorBuilder;
        }

        protected JoinModel buildJoinModel() {
            return JoinModel.of(joinSpecifications);
        }
        
        public SelectModel build() {
            joinModel = buildJoinModel();
            return buildModel();
        }
        
        public <T> SelectSupportWhereBuilder where(SqlColumn<T> column, Condition<T> condition) {
            joinModel = buildJoinModel();
            return new SelectSupportWhereBuilder(column, condition);
        }

        public <T> SelectSupportWhereBuilder where(SqlColumn<T> column, Condition<T> condition,
                SqlCriterion<?>...subCriteria) {
            joinModel = buildJoinModel();
            return new SelectSupportWhereBuilder(column, condition, subCriteria);
        }

        public SelectSupportAfterOrderByBuilder orderBy(SqlColumn<?>...columns) {
            joinModel = buildJoinModel();
            orderByColumns = Arrays.asList(columns);
            return new SelectSupportAfterOrderByBuilder();
        }

        public <T> SelectSupportAfterFromBuilder and(SqlColumn<T> column, JoinCondition<T> joinCondition) {
            return ancestorBuilder;
        }
    }
    
    public class SelectSupportAfterOrderByBuilder {
        private SelectSupportAfterOrderByBuilder() {
            super();
        }
        
        public SelectModel build() {
            return buildModel();
        }
    }
}
