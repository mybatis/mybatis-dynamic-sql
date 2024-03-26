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
package org.mybatis.dynamic.sql.common;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceAfter;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;
import org.mybatis.dynamic.sql.where.render.CriterionRenderer;
import org.mybatis.dynamic.sql.where.render.RenderedCriterion;

public abstract class AbstractBooleanExpressionRenderer {
    protected final AbstractBooleanExpressionModel model;
    private final String prefix;
    private final CriterionRenderer criterionRenderer;
    protected final RenderingContext renderingContext;

    protected AbstractBooleanExpressionRenderer(String prefix, AbstractBuilder<?> builder) {
        model = Objects.requireNonNull(builder.model);
        this.prefix = Objects.requireNonNull(prefix);
        renderingContext = Objects.requireNonNull(builder.renderingContext);
        criterionRenderer = new CriterionRenderer(renderingContext);
    }

    public Optional<FragmentAndParameters> render() {
        return model.initialCriterion()
                .map(this::renderWithInitialCriterion)
                .orElseGet(this::renderWithoutInitialCriterion)
                .map(RenderedCriterion::fragmentAndParameters);
    }

    private Optional<RenderedCriterion> renderWithInitialCriterion(SqlCriterion initialCriterion) {
        return criterionRenderer.render(initialCriterion, model.subCriteria(), this::calculateClause);
    }

    private Optional<RenderedCriterion> renderWithoutInitialCriterion() {
        return criterionRenderer.render(model.subCriteria(), this::calculateClause);
    }

    private String calculateClause(FragmentCollector collector) {
        if (collector.hasMultipleFragments()) {
            return collector.collectFragments(
                    Collectors.joining(" ", spaceAfter(prefix), "")); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            return collector.firstFragment()
                    .map(this::stripEnclosingParenthesesIfPresent)
                    .map(this::addPrefix)
                    .orElse(""); //$NON-NLS-1$
        }
    }

    private String stripEnclosingParenthesesIfPresent(String fragment) {
        // The fragment will have surrounding open/close parentheses if there is more than one rendered condition.
        // Since there is only a single fragment, we don't need these in the final rendered clause
        if (fragment.startsWith("(") && fragment.endsWith(")")) { //$NON-NLS-1$ //$NON-NLS-2$
            return fragment.substring(1, fragment.length() - 1);
        } else {
            return fragment;
        }
    }

    private String addPrefix(String fragment) {
        return spaceAfter(prefix) + fragment;
    }

    public abstract static class AbstractBuilder<B extends AbstractBuilder<B>> {
        private final AbstractBooleanExpressionModel model;
        private RenderingContext renderingContext;

        protected AbstractBuilder(AbstractBooleanExpressionModel model) {
            this.model = model;
        }

        public B withRenderingContext(RenderingContext renderingContext) {
            this.renderingContext = renderingContext;
            return getThis();
        }

        protected abstract B getThis();
    }
}
