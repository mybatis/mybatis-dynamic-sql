/**
 *    Copyright 2016-2020 the original author or authors.
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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.mybatis.dynamic.sql.insert.MultiRowInsertModel;
import org.mybatis.dynamic.sql.render.RenderingStrategy;

public class MultiRowInsertRenderer<T> {

    private MultiRowInsertModel<T> model;
    private RenderingStrategy renderingStrategy;
    
    private MultiRowInsertRenderer(Builder<T> builder) {
        model = Objects.requireNonNull(builder.model);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
    }
    
    public MultiRowInsertStatementProvider<T> render() {
        MultiRowValuePhraseVisitor visitor =
                new MultiRowValuePhraseVisitor(renderingStrategy, "records[%s]"); //$NON-NLS-1$
        List<FieldAndValue> fieldsAndValues = model
                .mapColumnMappings(MultiRowRenderingUtilities.toFieldAndValue(visitor))
                .collect(Collectors.toList());
        
        return new DefaultMultiRowInsertStatementProvider.Builder<T>().withRecords(model.records())
                .withInsertStatement(calculateInsertStatement(fieldsAndValues))
                .build();
    }
    
    private String calculateInsertStatement(List<FieldAndValue> fieldsAndValues) {
        return "insert into" //$NON-NLS-1$
                + spaceBefore(model.table().tableNameAtRuntime())
                + spaceBefore(MultiRowRenderingUtilities.calculateColumnsPhrase(fieldsAndValues))
                + spaceBefore(calculateMultiRowInsertValuesPhrase(fieldsAndValues, model.recordCount()));
    }
    
    private String calculateMultiRowInsertValuesPhrase(List<FieldAndValue> fieldsAndValues, int rowCount) {
        return IntStream.range(0, rowCount)
                .mapToObj(i -> toSingleRowOfValues(fieldsAndValues, i))
                .collect(Collectors.joining(", ", "values ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    private String toSingleRowOfValues(List<FieldAndValue> fieldsAndValues, int row) {
        return fieldsAndValues.stream()
                .map(fmv -> fmv.valuePhrase(row))
                .collect(Collectors.joining(", ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
