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
package org.mybatis.dynamic.sql.select.join;

import java.util.Objects;

import org.mybatis.dynamic.sql.BasicColumn;

public abstract class JoinCriterion {

    private BasicColumn leftColumn;
    private JoinCondition joinCondition;
    
    protected JoinCriterion(AbstractBuilder<?> builder) {
        leftColumn = Objects.requireNonNull(builder.joinColumn);
        joinCondition = Objects.requireNonNull(builder.joinCondition);
    }

    public abstract String connector();
    
    public BasicColumn leftColumn() {
        return leftColumn;
    }
    
    public BasicColumn rightColumn() {
        return joinCondition.rightColumn();
    }
    
    public String operator() {
        return joinCondition.operator();
    }
    
    public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
        private BasicColumn joinColumn;
        private JoinCondition joinCondition;
        
        public T withJoinColumn(BasicColumn joinColumn) {
            this.joinColumn = joinColumn;
            return getThis();
        }
        
        public T withJoinCondition(JoinCondition joinCondition) {
            this.joinCondition = joinCondition;
            return getThis();
        }
        
        protected abstract T getThis();
    }
}
