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

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.mybatis.dynamic.sql.insert.GeneralInsertModel;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.AbstractColumnMapping;

public class GeneralInsertRenderer {

    private GeneralInsertModel model;
    private RenderingStrategy renderingStrategy;
    
    private GeneralInsertRenderer(Builder builder) {
        model = Objects.requireNonNull(builder.model);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
    }
    
    public GeneralInsertStatementProvider render() {
        GeneralInsertValuePhraseVisitor visitor = new GeneralInsertValuePhraseVisitor(renderingStrategy);
        FieldAndValueAndParametersCollector collector = model.mapColumnMappings(toFieldAndValue(visitor))
                .collect(FieldAndValueAndParametersCollector.collect());
        
        return DefaultGeneralInsertStatementProvider.withInsertStatement(calculateInsertStatement(collector))
                .withParameters(collector.parameters())
                .build();
    }

    private String calculateInsertStatement(FieldAndValueAndParametersCollector collector) {
        return "insert into" //$NON-NLS-1$
                + spaceBefore(model.table().tableNameAtRuntime())
                + spaceBefore(collector.columnsPhrase())
                + spaceBefore(collector.valuesPhrase());
    }

    private Function<AbstractColumnMapping, Optional<FieldAndValueAndParameters>> toFieldAndValue(
            GeneralInsertValuePhraseVisitor visitor) {
        return insertMapping -> toFieldAndValue(visitor, insertMapping);
    }
    
    private Optional<FieldAndValueAndParameters> toFieldAndValue(GeneralInsertValuePhraseVisitor visitor,
            AbstractColumnMapping insertMapping) {
        return insertMapping.accept(visitor);
    }
    
    public static Builder withInsertModel(GeneralInsertModel model) {
        return new Builder().withInsertModel(model);
    }
    
    public static class Builder {
        private GeneralInsertModel model;
        private RenderingStrategy renderingStrategy;
        
        public Builder withInsertModel(GeneralInsertModel model) {
            this.model = model;
            return this;
        }
        
        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }
        
        public GeneralInsertRenderer build() {
            return new GeneralInsertRenderer(this);
        }
    }
}
