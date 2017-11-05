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

import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectRenderer;
import org.mybatis.dynamic.sql.select.render.SelectSupport;

public class SelectModel {
    private QueryExpression queryExpression;
    private Optional<OrderByModel> orderByModel;

    private SelectModel(Builder builder) {
        queryExpression = Objects.requireNonNull(builder.queryExpression);
        orderByModel = Optional.ofNullable(builder.orderByModel);
    }
    
    public QueryExpression queryExpression() {
        return queryExpression;
    }
    
    public Optional<OrderByModel> orderByModel() {
        return orderByModel;
    }
    
    public SelectSupport render(RenderingStrategy renderingStrategy) {
        return SelectRenderer.of(this).render(renderingStrategy);
    }
    
    public static class Builder {
        private QueryExpression queryExpression;
        private OrderByModel orderByModel;
        
        public Builder withQueryExpression(QueryExpression queryExpression) {
            this.queryExpression = queryExpression;
            return this;
        }
        
        public Builder withOrderByModel(OrderByModel orderByModel) {
            this.orderByModel = orderByModel;
            return this;
        }
        
        public SelectModel build() {
            return new SelectModel(this);
        }
    }
}
