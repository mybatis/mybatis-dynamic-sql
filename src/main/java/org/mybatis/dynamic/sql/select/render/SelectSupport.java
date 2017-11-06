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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.util.StringUtilities;

public class SelectSupport {
    
    private List<RenderedQueryExpression> renderedQueryExpressions;
    private Optional<String> orderByClause;
    
    private SelectSupport(Builder builder) {
        renderedQueryExpressions = builder.renderedQueryExpressions;
        orderByClause = Optional.ofNullable(builder.orderByClause);
    }
    
    public Map<String, Object> getParameters() {
        // TODO - this should be better
        Map<String, Object> parameters = new HashMap<>();
        
        renderedQueryExpressions.stream()
        .map(RenderedQueryExpression::getParameters)
        .forEach(parameters::putAll);
        
        return parameters;
    }
    
    public String getFullSelectStatement() {
        return renderedQueryExpressions.stream()
                .map(RenderedQueryExpression::getFullSelectStatement)
                .collect(Collectors.joining(" "))
                + StringUtilities.spaceBefore(orderByClause);
    }
    
    public static class Builder {
        private List<RenderedQueryExpression> renderedQueryExpressions = new ArrayList<>();
        private String orderByClause;
        
        public Builder withRenderedQueryExpressions(List<RenderedQueryExpression> renderedQueryExpressions) {
            this.renderedQueryExpressions.addAll(renderedQueryExpressions);
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
