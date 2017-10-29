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
package org.mybatis.dynamic.sql.where.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;
import org.mybatis.dynamic.sql.where.WhereModel;

public class WhereRenderer {
    private WhereModel whereModel;
    private AtomicInteger sequence;
    private RenderingStrategy renderingStrategy;
    private Map<SqlTable, String> tableAliases = new HashMap<>();
    
    private WhereRenderer(Builder builder) {
        whereModel = Objects.requireNonNull(builder.whereModel);
        sequence = Objects.requireNonNull(builder.sequence);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        tableAliases.putAll(builder.tableAliases);
    }
    
    public WhereSupport render() {
        FragmentCollector fc = whereModel.criteria()
                .map(this::render)
                .collect(FragmentCollector.collect());

        return new WhereSupport.Builder()
                .withWhereClause(calculateWhereClause(fc))
                .withParameters(fc.parameters())
                .build();
    }
    
    private FragmentAndParameters render(SqlCriterion<?> criterion) {
        return new CriterionRenderer.Builder()
                .withSequence(sequence)
                .withRenderingStrategy(renderingStrategy)
                .withTableAliases(tableAliases)
                .build()
                .render(criterion);
    }
    
    private String calculateWhereClause(FragmentCollector collector) {
        return collector.fragments()
                .collect(Collectors.joining(" ", "where ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    public static class Builder {
        private WhereModel whereModel;
        private RenderingStrategy renderingStrategy;
        private Map<SqlTable, String> tableAliases = new HashMap<>();
        private AtomicInteger sequence;
        
        public Builder withWhereModel(WhereModel whereModel) {
            this.whereModel = whereModel;
            return this;
        }
        
        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }
        
        public Builder withTableAliases(Map<SqlTable, String> tableAliases) {
            this.tableAliases.putAll(tableAliases);
            return this;
        }
        
        public Builder withSequence(AtomicInteger sequence) {
            this.sequence = sequence;
            return this;
        }
        
        public WhereRenderer build() {
            return new WhereRenderer(this);
        }
    }
}
