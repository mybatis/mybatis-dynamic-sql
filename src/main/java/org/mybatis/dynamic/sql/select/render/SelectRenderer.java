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
package org.mybatis.dynamic.sql.select.render;

import java.util.Objects;

import org.mybatis.dynamic.sql.render.RendererFactory;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class SelectRenderer {
    private final SelectModel selectModel;
    private final RenderingContext renderingContext;

    private SelectRenderer(Builder builder) {
        selectModel = Objects.requireNonNull(builder.selectModel);
        renderingContext = RenderingContext.withRenderingStrategy(builder.renderingStrategy)
                .withStatementConfiguration(selectModel.statementConfiguration())
                .build();
    }

    public SelectStatementProvider render() {
        FragmentAndParameters fragmentAndParameters = RendererFactory.createSubQueryRenderer(selectModel)
                .render(renderingContext);
        return DefaultSelectStatementProvider.withSelectStatement(fragmentAndParameters.fragment())
                .withParameters(fragmentAndParameters.parameters())
                .build();
    }

    public static Builder withSelectModel(SelectModel selectModel) {
        return new Builder().withSelectModel(selectModel);
    }

    public static class Builder {
        private SelectModel selectModel;
        private RenderingStrategy renderingStrategy;

        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }

        public Builder withSelectModel(SelectModel selectModel) {
            this.selectModel = selectModel;
            return this;
        }

        public SelectRenderer build() {
            return new SelectRenderer(this);
        }
    }
}
