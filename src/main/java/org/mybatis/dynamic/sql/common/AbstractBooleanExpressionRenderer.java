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
package org.mybatis.dynamic.sql.common;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;
import org.mybatis.dynamic.sql.where.render.CriterionRenderer;
import org.mybatis.dynamic.sql.where.render.RenderedCriterion;

public abstract class AbstractBooleanExpressionRenderer<M extends AbstractBooleanExpressionModel> {
    protected final M model;
    private final CriterionRenderer criterionRenderer;
    private final String prefix;

    protected AbstractBooleanExpressionRenderer(AbstractBuilder<M, ?> builder) {
        model = Objects.requireNonNull(builder.model);

        criterionRenderer = new CriterionRenderer.Builder()
                .withSequence(builder.sequence)
                .withRenderingStrategy(builder.renderingStrategy)
                .withTableAliasCalculator(builder.tableAliasCalculator)
                .withParameterName(builder.parameterName)
                .build();

        prefix = Objects.requireNonNull(builder.prefix);
    }

    public Optional<FragmentAndParameters> render() {
        return model.initialCriterion()
                .map(this::renderWithInitialCriterion)
                .orElseGet(this::renderWithoutInitialCriterion)
                .map(rc -> FragmentAndParameters.withFragment(rc.fragmentAndParameters().fragment())
                        .withParameters(rc.fragmentAndParameters().parameters())
                        .build()
                );
    }

    private Optional<RenderedCriterion> renderWithInitialCriterion(SqlCriterion initialCriterion) {
        return criterionRenderer.render(initialCriterion, model.subCriteria(), this::calculateClause);
    }

    private Optional<RenderedCriterion> renderWithoutInitialCriterion() {
        return criterionRenderer.render(model.subCriteria(), this::calculateClause);
    }

    private String calculateClause(FragmentCollector collector) {
        if (collector.hasMultipleFragments()) {
            return collector.fragments()
                    .collect(Collectors.joining(" ", prefix + " ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else {
            return collector.firstFragment()
                    .map(this::stripEnclosingParenthesesIfPresent)
                    .map(s -> prefix + " " + s) //$NON-NLS-1$
                    .orElse(""); //$NON-NLS-1$
        }
    }

    private String stripEnclosingParenthesesIfPresent(String fragment) {
        // The fragment will have surrounding open/close parentheses if there is more than one rendered condition.
        // Since there is only a single fragment, we don't need these in the where clause
        if (fragment.startsWith("(") && fragment.endsWith(")")) { //$NON-NLS-1$ //$NON-NLS-2$
            return fragment.substring(1, fragment.length() - 1);
        } else {
            return fragment;
        }
    }
    public abstract static class AbstractBuilder<M, B extends AbstractBuilder<M, B>> {
        private final M model;
        private RenderingStrategy renderingStrategy;
        private TableAliasCalculator tableAliasCalculator;
        private AtomicInteger sequence;
        private String parameterName;

        private final String prefix;

        protected AbstractBuilder(String prefix, M model) {
            this.prefix = prefix;
            this.model = model;
        }

        public B withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return getThis();
        }

        public B withTableAliasCalculator(TableAliasCalculator tableAliasCalculator) {
            this.tableAliasCalculator = tableAliasCalculator;
            return getThis();
        }

        public B withSequence(AtomicInteger sequence) {
            this.sequence = sequence;
            return getThis();
        }

        public B withParameterName(String parameterName) {
            this.parameterName = parameterName;
            return getThis();
        }

        protected abstract B getThis();
    }
}
