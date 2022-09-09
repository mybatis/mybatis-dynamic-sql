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
package org.mybatis.dynamic.sql.where.render;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.exception.NonRenderingWhereClauseException;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.util.FragmentCollector;
import org.mybatis.dynamic.sql.where.WhereModel;

public class WhereRenderer {
    private final WhereModel whereModel;
    private final CriterionRenderer criterionRenderer;

    private WhereRenderer(Builder builder) {
        whereModel = Objects.requireNonNull(builder.whereModel);

        criterionRenderer = new CriterionRenderer.Builder()
                .withSequence(builder.sequence)
                .withRenderingStrategy(builder.renderingStrategy)
                .withTableAliasCalculator(builder.tableAliasCalculator)
                .withParameterName(builder.parameterName)
                .build();
    }

    public Optional<WhereClauseProvider> render() {
        Optional<WhereClauseProvider> whereClause = whereModel.initialCriterion().map(this::renderWithInitialCriterion)
                .orElseGet(this::renderWithoutInitialCriterion)
                .map(rc -> WhereClauseProvider.withWhereClause(rc.fragmentAndParameters().fragment())
                        .withParameters(rc.fragmentAndParameters().parameters())
                        .build()
                );

        if (whereClause.isPresent() || whereModel.isNonRenderingClauseAllowed()) {
            return whereClause;
        } else {
            throw new NonRenderingWhereClauseException();
        }
    }

    private Optional<RenderedCriterion> renderWithInitialCriterion(SqlCriterion initialCriterion) {
        return criterionRenderer.render(initialCriterion, whereModel.subCriteria(), this::calculateWhereClause);
    }

    private Optional<RenderedCriterion> renderWithoutInitialCriterion() {
        return criterionRenderer.render(whereModel.subCriteria(), this::calculateWhereClause);
    }

    private String calculateWhereClause(FragmentCollector collector) {
        return collector.fragments()
                .collect(Collectors.joining(" ", "where ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public static Builder withWhereModel(WhereModel whereModel) {
        return new Builder().withWhereModel(whereModel);
    }

    public static class Builder {
        private WhereModel whereModel;
        private RenderingStrategy renderingStrategy;
        private TableAliasCalculator tableAliasCalculator;
        private AtomicInteger sequence;
        private String parameterName;

        public Builder withWhereModel(WhereModel whereModel) {
            this.whereModel = whereModel;
            return this;
        }

        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }

        public Builder withTableAliasCalculator(TableAliasCalculator tableAliasCalculator) {
            this.tableAliasCalculator = tableAliasCalculator;
            return this;
        }

        public Builder withSequence(AtomicInteger sequence) {
            this.sequence = sequence;
            return this;
        }

        public Builder withParameterName(String parameterName) {
            this.parameterName = parameterName;
            return this;
        }

        public WhereRenderer build() {
            return new WhereRenderer(this);
        }
    }
}
