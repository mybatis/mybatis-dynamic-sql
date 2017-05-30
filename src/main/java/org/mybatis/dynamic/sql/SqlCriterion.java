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
package org.mybatis.dynamic.sql;

import java.util.List;
import java.util.Optional;

public class SqlCriterion<T> extends AbstractCriterion<T, SqlColumn<T>, SqlCriterion<?>> {
    
    private SqlCriterion() {
        super();
    }
    
    public static class Builder<T> {
        private String connector;
        private SqlColumn<T> column;
        private Condition<T> condition;
        private List<SqlCriterion<?>> subCriteria;
        
        public Builder<T> withConnector(String connector) {
            this.connector = connector;
            return this;
        }
        
        public Builder<T> withColumn(SqlColumn<T> column) {
            this.column = column;
            return this;
        }
        
        public Builder<T> withCondition(Condition<T> condition) {
            this.condition = condition;
            return this;
        }
        
        public Builder<T> withSubCriteria(List<SqlCriterion<?>> subCriteria) {
            this.subCriteria = subCriteria;
            return this;
        }
        
        public SqlCriterion<T> build() {
            SqlCriterion<T> sqlCriterion = new SqlCriterion<>();
            sqlCriterion.connector = Optional.ofNullable(connector);
            sqlCriterion.column = column;
            sqlCriterion.condition = condition;
            sqlCriterion.subCriteria = Optional.ofNullable(subCriteria);
            return sqlCriterion;
        }
    }
}
