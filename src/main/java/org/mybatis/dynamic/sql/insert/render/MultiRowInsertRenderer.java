/*
 *    Copyright 2016-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
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

import org.mybatis.dynamic.sql.insert.MultiRowInsertModel;
import org.mybatis.dynamic.sql.render.RenderingStrategy;

public class MultiRowInsertRenderer<T> {

    private final MultiRowInsertModel<T> model;
    private final MultiRowValuePhraseVisitor visitor;

    private MultiRowInsertRenderer(Builder<T> builder) {
        model = Objects.requireNonNull(builder.model);
        // the prefix is a generic format that will be resolved below with String.format(...)
        visitor = new MultiRowValuePhraseVisitor(builder.renderingStrategy, "records[%s]"); //$NON-NLS-1$
    }

    public MultiRowInsertStatementProvider<T> render() {
        FieldAndValueCollector collector = model.columnMappings()
                .map(m -> m.accept(visitor))
                .collect(FieldAndValueCollector.collect());

        String insertStatement = calculateInsertStatement(collector);

        return new DefaultMultiRowInsertStatementProvider.Builder<T>().withRecords(model.records())
                .withInsertStatement(insertStatement)
                .build();
    }

    private String calculateInsertStatement(FieldAndValueCollector collector) {
        String statementStart = InsertRenderingUtilities.calculateInsertStatementStart(model.table());
        String columnsPhrase = collector.columnsPhrase();
        String valuesPhrase = collector.multiRowInsertValuesPhrase(model.recordCount());

        return statementStart + spaceBefore(columnsPhrase) + spaceBefore(valuesPhrase);
    }

    public static <T> Builder<T> withMultiRowInsertModel(MultiRowInsertModel<T> model) {
        return new Builder<T>().withMultiRowInsertModel(model);
    }

    public static class Builder<T> {
        private MultiRowInsertModel<T> model;
        private RenderingStrategy renderingStrategy;

        public Builder<T> withMultiRowInsertModel(MultiRowInsertModel<T> model) {
            this.model = model;
            return this;
        }

        public Builder<T> withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }

        public MultiRowInsertRenderer<T> build() {
            return new MultiRowInsertRenderer<>(this);
        }
    }
}
