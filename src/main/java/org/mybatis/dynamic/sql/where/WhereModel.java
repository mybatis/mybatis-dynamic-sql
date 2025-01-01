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
package org.mybatis.dynamic.sql.where;

import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionModel;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.where.render.DefaultWhereClauseProvider;
import org.mybatis.dynamic.sql.where.render.WhereClauseProvider;
import org.mybatis.dynamic.sql.where.render.WhereRenderer;

public class WhereModel extends AbstractBooleanExpressionModel {
    private final StatementConfiguration statementConfiguration;

    private WhereModel(Builder builder) {
        super(builder);
        statementConfiguration = Objects.requireNonNull(builder.statementConfiguration);
    }

    /**
     * Renders a where clause without table aliases.
     *
     * @param renderingStrategy
     *            rendering strategy
     *
     * @return rendered where clause
     */
    public Optional<WhereClauseProvider> render(RenderingStrategy renderingStrategy) {
        RenderingContext renderingContext = RenderingContext.withRenderingStrategy(renderingStrategy)
                .withStatementConfiguration(statementConfiguration).build();

        return render(renderingContext);
    }

    public Optional<WhereClauseProvider> render(RenderingStrategy renderingStrategy,
                                                TableAliasCalculator tableAliasCalculator) {
        RenderingContext renderingContext = RenderingContext
                .withRenderingStrategy(renderingStrategy)
                .withTableAliasCalculator(tableAliasCalculator)
                .withStatementConfiguration(statementConfiguration)
                .build();

        return render(renderingContext);
    }

    public Optional<WhereClauseProvider> render(RenderingStrategy renderingStrategy, String parameterName) {
        RenderingContext renderingContext = RenderingContext
                .withRenderingStrategy(renderingStrategy)
                .withParameterName(parameterName)
                .withStatementConfiguration(statementConfiguration)
                .build();

        return render(renderingContext);
    }

    public Optional<WhereClauseProvider> render(RenderingStrategy renderingStrategy,
            TableAliasCalculator tableAliasCalculator, String parameterName) {
        RenderingContext renderingContext = RenderingContext
                .withRenderingStrategy(renderingStrategy)
                .withTableAliasCalculator(tableAliasCalculator)
                .withParameterName(parameterName)
                .withStatementConfiguration(statementConfiguration)
                .build();

        return render(renderingContext);
    }

    private Optional<WhereClauseProvider> render(RenderingContext renderingContext) {
        return WhereRenderer.withWhereModel(this)
                .withRenderingContext(renderingContext)
                .build()
                .render()
                .map(this::toWhereClauseProvider);
    }

    private WhereClauseProvider toWhereClauseProvider(FragmentAndParameters fragmentAndParameters) {
        return DefaultWhereClauseProvider.withWhereClause(fragmentAndParameters.fragment())
                .withParameters(fragmentAndParameters.parameters())
                .build();
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private StatementConfiguration statementConfiguration;

        public Builder withStatementConfiguration(StatementConfiguration statementConfiguration) {
            this.statementConfiguration = statementConfiguration;
            return this;
        }

        public WhereModel build() {
            return new WhereModel(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
