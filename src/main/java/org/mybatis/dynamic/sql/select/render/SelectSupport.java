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
package org.mybatis.dynamic.sql.select.render;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.util.StringUtilities;

public class SelectSupport {
    
    private RenderedQueryExpression renderedQueryExpression;
    private Optional<String> orderByClause;
    
    private SelectSupport(Builder builder) {
        renderedQueryExpression = Objects.requireNonNull(builder.renderedQueryExpression);
        orderByClause = Optional.ofNullable(builder.orderByClause);
    }
    
    public boolean isDistinct() {
        return renderedQueryExpression.isDistinct();
    }
    
    public String getWhereClause() {
        return renderedQueryExpression.getWhereClause();
    }

    public Map<String, Object> getParameters() {
        return renderedQueryExpression.getParameters();
    }
    
    public String getOrderByClause() {
        return orderByClause.orElse(""); //$NON-NLS-1$
    }
    
    public Optional<String> orderByClause() {
        return orderByClause;
    }
    
    public String getColumnList() {
        return renderedQueryExpression.getColumnList();
    }
    
    public String getFullSelectStatement() {
        return renderedQueryExpression.getFullSelectStatement()
                + StringUtilities.spaceBefore(orderByClause());
    }
    
    public static class Builder {
        private RenderedQueryExpression renderedQueryExpression;
        private String orderByClause;
        
        public Builder withRenderedQueryExpression(RenderedQueryExpression renderedQueryExpression) {
            this.renderedQueryExpression = renderedQueryExpression;
            return this;
        }
        
        public Builder withOrderByClause(String orderByClause) {
            this.orderByClause = orderByClause;
            return this;
        }
        
        public SelectSupport build() {
            return new SelectSupport(this);
        }
    }
}
