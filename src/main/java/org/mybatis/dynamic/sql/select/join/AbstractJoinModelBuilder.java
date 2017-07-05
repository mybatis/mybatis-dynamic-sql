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
package org.mybatis.dynamic.sql.select.join;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.select.JoinSpecification;

public abstract class AbstractJoinModelBuilder<T extends AbstractJoinModelBuilder<T>> {

    private List<JoinSpecification> joinSpecifications = new ArrayList<>();
    
    protected <S> AbstractJoinModelBuilder(SqlTable joinTable, SqlColumn<S> joinColumn, JoinCondition<S> joinCondition) {
        joinSpecifications.add(JoinSpecification.of(joinTable, joinColumn, joinCondition));
    }
    
    public JoinBuilder join(SqlTable table) {
        return new JoinBuilder(table);
    }
    
    protected JoinModel buildJoinModel() {
        return JoinModel.of(joinSpecifications);
    }
    
    protected abstract T getThis();
    
    public class JoinBuilder {
        private SqlTable table;
        
        public JoinBuilder(SqlTable table) {
            this.table = table;
        }
        
        public <S> T on(SqlColumn<S> joinColumn, JoinCondition<S> joinCondition) {
            joinSpecifications.add(JoinSpecification.of(table, joinColumn, joinCondition));
            return getThis();
        }
    }
}
