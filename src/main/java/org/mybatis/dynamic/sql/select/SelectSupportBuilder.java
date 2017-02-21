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

import java.util.Arrays;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.Condition;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.where.AbstractWhereBuilder;
import org.mybatis.dynamic.sql.where.WhereSupport;

public class SelectSupportBuilder {

    private SelectSupport.Builder builder = new SelectSupport.Builder();

    private SelectSupportBuilder() {
        super();
    }
    
    private SelectSupportBuilder(SqlColumn<?>...columns) {
        builder.withColumnList(calculateColumnList(columns));
    }
    
    public SelectSupportAfterFromBuilder from(SqlTable table) {
        builder.withTable(table);
        return new SelectSupportAfterFromBuilder();
    }

    private String calculateColumnList(SqlColumn<?>...columns) {
        return Arrays.stream(columns)
                .map(SqlColumn::nameIncludingTableAndColumnAlias)
                .collect(Collectors.joining(", ")); //$NON-NLS-1$
    }
    
    private String calculateOrderByPhrase(SqlColumn<?>...columns) {
        return Arrays.stream(columns)
                .map(SqlColumn::orderByPhrase)
                .collect(Collectors.joining(", ", "order by ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public static SelectSupportBuilder of(SqlColumn<?>...columns) {
        return new SelectSupportBuilder(columns);
    }
    
    public static SelectSupportBuilder ofDistinct(SqlColumn<?>...columns) {
        SelectSupportBuilder builder = SelectSupportBuilder.of(columns);
        builder.builder.isDistinct();
        return builder;
    }
    
    public static SelectSupportBuilder forCount() {
        SelectSupportBuilder builder = new SelectSupportBuilder();
        builder.builder.withColumnList("count(*)"); //$NON-NLS-1$
        return builder;
    }
    
    public class SelectSupportAfterFromBuilder {
        private SelectSupportAfterFromBuilder() {
            super();
        }
        
        public <T> SelectSupportWhereBuilder where(SqlColumn<T> column, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            return new SelectSupportWhereBuilder(column, condition, subCriteria);
        }

        public SelectSupportAfterOrderByBuilder orderBy(SqlColumn<?>...columns) {
            builder.withOrderByClause(calculateOrderByPhrase(columns));
            return new SelectSupportAfterOrderByBuilder();
        }
        
        public SelectSupport build() {
            return builder.build();
        }
    }
    
    public class SelectSupportWhereBuilder extends AbstractWhereBuilder<SelectSupportWhereBuilder> {
        private <T> SelectSupportWhereBuilder(SqlColumn<T> column, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            super(column, condition, subCriteria);
        }
        
        public SelectSupportAfterOrderByBuilder orderBy(SqlColumn<?>...columns) {
            buildWhereSupport();
            builder.withOrderByClause(calculateOrderByPhrase(columns));
            return new SelectSupportAfterOrderByBuilder();
        }
        
        public SelectSupport build() {
            buildWhereSupport();
            return builder.build();
        }
        
        private void buildWhereSupport() {
            WhereSupport whereSupport = renderCriteria(SqlColumn::nameIncludingTableAlias);
            builder.withParameters(whereSupport.getParameters())
                .withWhereClause(whereSupport.getWhereClause());
        }

        @Override
        protected SelectSupportWhereBuilder getThis() {
            return this;
        }
    }
    
    public class SelectSupportAfterOrderByBuilder {
        private SelectSupportAfterOrderByBuilder() {
            super();
        }
        
        public SelectSupport build() {
            return builder.build();
        }
    }
}
