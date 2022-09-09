/*
 *    Copyright 2016-2022 the original author or authors.
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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.insert.BatchInsertModel;
import org.mybatis.dynamic.sql.render.RenderingStrategy;

public class BatchInsertRenderer<T> {

    private final BatchInsertModel<T> model;
    private final RenderingStrategy renderingStrategy;

    private BatchInsertRenderer(Builder<T> builder) {
        model = Objects.requireNonNull(builder.model);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
    }

    public BatchInsert<T> render() {
        BatchValuePhraseVisitor visitor = new BatchValuePhraseVisitor(renderingStrategy, "row"); //$NON-NLS-1$)
        List<FieldAndValue> fieldsAndValues = model
                .mapColumnMappings(m -> m.accept(visitor))
                .collect(Collectors.toList());

        return BatchInsert.withRecords(model.records())
                .withInsertStatement(calculateInsertStatement(fieldsAndValues))
                .build();
    }

    private String calculateInsertStatement(List<FieldAndValue> fieldsAndValues) {
        return "insert into" //$NON-NLS-1$
                + spaceBefore(model.table().tableNameAtRuntime())
                + spaceBefore(calculateColumnsPhrase(fieldsAndValues))
                + spaceBefore(calculateValuesPhrase(fieldsAndValues));
    }

    private String calculateColumnsPhrase(List<FieldAndValue> fieldsAndValues) {
        return fieldsAndValues.stream()
                .map(FieldAndValue::fieldName)
                .collect(Collectors.joining(", ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    private String calculateValuesPhrase(List<FieldAndValue> fieldsAndValues) {
        return fieldsAndValues.stream()
                .map(FieldAndValue::valuePhrase)
                .collect(Collectors.joining(", ", "values (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
