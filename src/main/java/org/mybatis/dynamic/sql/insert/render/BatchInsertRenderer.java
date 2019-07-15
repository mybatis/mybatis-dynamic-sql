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
package org.mybatis.dynamic.sql.insert.render;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.Objects;

import org.mybatis.dynamic.sql.insert.BatchInsertModel;
import org.mybatis.dynamic.sql.render.RenderingStrategy;

public class BatchInsertRenderer<T> {

    private BatchInsertModel<T> model;
    private RenderingStrategy renderingStrategy;
    
    private BatchInsertRenderer(Builder<T> builder) {
        model = Objects.requireNonNull(builder.model);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
    }
    
    public BatchInsert<T> render() {
        ValuePhraseVisitor visitor = new ValuePhraseVisitor(renderingStrategy);
        FieldAndValueCollector collector = model.mapColumnMappings(MultiRowRenderingUtilities.toFieldAndValue(visitor))
                .collect(FieldAndValueCollector.collect());
        
        return BatchInsert.withRecords(model.records())
                .withInsertStatement(calculateInsertStatement(collector))
                .build();
    }
    
    private String calculateInsertStatement(FieldAndValueCollector collector) {
        return "insert into" //$NON-NLS-1$
                + spaceBefore(model.table().tableNameAtRuntime())
                + spaceBefore(collector.columnsPhrase())
                + spaceBefore(collector.valuesPhrase());
    }
    
    public static <T> Builder<T> withBatchInsertModel(BatchInsertModel<T> model) {
        return new Builder<T>().withBatchInsertModel(model);
    }
    
    public static class Builder<T> {
        private BatchInsertModel<T> model;
        private RenderingStrategy renderingStrategy;
        
        public Builder<T> withBatchInsertModel(BatchInsertModel<T> model) {
            this.model = model;
            return this;
        }
        
        public Builder<T> withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }
        
        public BatchInsertRenderer<T> build() {
            return new BatchInsertRenderer<>(this);
        }
    }
}
