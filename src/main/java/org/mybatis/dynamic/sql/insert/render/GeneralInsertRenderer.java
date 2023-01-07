/*
 *    Copyright 2016-2023 the original author or authors.
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

import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.exception.InvalidSqlException;
import org.mybatis.dynamic.sql.insert.GeneralInsertModel;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.Messages;

public class GeneralInsertRenderer {

    private final GeneralInsertModel model;
    private final RenderingStrategy renderingStrategy;

    private GeneralInsertRenderer(Builder builder) {
        model = Objects.requireNonNull(builder.model);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
    }

    public GeneralInsertStatementProvider render() {
        GeneralInsertValuePhraseVisitor visitor = new GeneralInsertValuePhraseVisitor(renderingStrategy);
        FieldAndValueCollector collector = model.mapColumnMappings(m -> m.accept(visitor))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(FieldAndValueCollector.collect());

        if (collector.isEmpty()) {
            throw new InvalidSqlException(Messages.getString("ERROR.9")); //$NON-NLS-1$
        }

        String insertStatement = InsertRenderingUtilities.calculateInsertStatement(model.table(), collector);

        return DefaultGeneralInsertStatementProvider.withInsertStatement(insertStatement)
                .withParameters(collector.parameters())
                .build();
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
