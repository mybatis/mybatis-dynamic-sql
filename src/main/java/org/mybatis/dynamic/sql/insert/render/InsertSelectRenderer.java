/*
 *    Copyright 2016-2025 the original author or authors.
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

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceAfter;

import java.util.Objects;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.insert.InsertColumnListModel;
import org.mybatis.dynamic.sql.insert.InsertSelectModel;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SubQueryRenderer;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class InsertSelectRenderer {

    private final InsertSelectModel model;
    private final RenderingContext renderingContext;

    private InsertSelectRenderer(Builder builder) {
        model = Objects.requireNonNull(builder.model);
        renderingContext = RenderingContext.withRenderingStrategy(Objects.requireNonNull(builder.renderingStrategy))
                .withStatementConfiguration(model.statementConfiguration())
                .build();
    }

    public InsertSelectStatementProvider render() {
        String statementStart = InsertRenderingUtilities.calculateInsertStatementStart(model.table());
        String columnsPhrase = calculateColumnsPhrase();
        String prefix = statementStart + spaceAfter(columnsPhrase);

        FragmentAndParameters fragmentAndParameters = SubQueryRenderer.withSelectModel(model.selectModel())
                .withRenderingContext(renderingContext)
                .withPrefix(prefix)
                .build()
                .render();

        return DefaultGeneralInsertStatementProvider.withInsertStatement(fragmentAndParameters.fragment())
                .withParameters(fragmentAndParameters.parameters())
                .build();
    }

    private String calculateColumnsPhrase() {
        return model.columnList().map(this::calculateColumnsPhrase).orElse(""); //$NON-NLS-1$
    }

    private String calculateColumnsPhrase(InsertColumnListModel columnList) {
        return columnList.columns()
                .map(SqlColumn::name)
                .collect(Collectors.joining(", ", " (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public static Builder withInsertSelectModel(InsertSelectModel model) {
        return new Builder().withInsertSelectModel(model);
    }

    public static class Builder {
        private @Nullable InsertSelectModel model;
        private @Nullable RenderingStrategy renderingStrategy;

        public Builder withInsertSelectModel(InsertSelectModel model) {
            this.model = model;
            return this;
        }

        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }

        public InsertSelectRenderer build() {
            return new InsertSelectRenderer(this);
        }
    }
}
