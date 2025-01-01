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
package org.mybatis.dynamic.sql.select.render;

import java.util.Objects;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.exception.InvalidSqlException;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;
import org.mybatis.dynamic.sql.util.Messages;

public class JoinRenderer {
    private final JoinModel joinModel;
    private final TableExpressionRenderer tableExpressionRenderer;
    private final RenderingContext renderingContext;

    private JoinRenderer(Builder builder) {
        joinModel = Objects.requireNonNull(builder.joinModel);
        tableExpressionRenderer = Objects.requireNonNull(builder.tableExpressionRenderer);
        renderingContext = Objects.requireNonNull(builder.renderingContext);
    }

    public FragmentAndParameters render() {
        return joinModel.joinSpecifications()
                .map(this::renderJoinSpecification)
                .collect(FragmentCollector.collect())
                .toFragmentAndParameters(Collectors.joining(" ")); //$NON-NLS-1$
    }

    private FragmentAndParameters renderJoinSpecification(JoinSpecification joinSpecification) {
        FragmentCollector fc = new FragmentCollector();
        fc.add(FragmentAndParameters.fromFragment(joinSpecification.joinType().type()));
        fc.add(joinSpecification.table().accept(tableExpressionRenderer));
        fc.add(JoinSpecificationRenderer
                .withJoinSpecification(joinSpecification)
                .withRenderingContext(renderingContext)
                .build()
                .render()
                .orElseThrow(() -> new InvalidSqlException(Messages.getString("ERROR.46")))); //$NON-NLS-1$

        return fc.toFragmentAndParameters(Collectors.joining(" ")); //$NON-NLS-1$
    }

    public static Builder withJoinModel(JoinModel joinModel) {
        return new Builder().withJoinModel(joinModel);
    }

    public static class Builder {
        private JoinModel joinModel;
        private TableExpressionRenderer tableExpressionRenderer;
        private RenderingContext renderingContext;

        public Builder withJoinModel(JoinModel joinModel) {
            this.joinModel = joinModel;
            return this;
        }

        public Builder withTableExpressionRenderer(TableExpressionRenderer tableExpressionRenderer) {
            this.tableExpressionRenderer = tableExpressionRenderer;
            return this;
        }

        public Builder withRenderingContext(RenderingContext renderingContext) {
            this.renderingContext = renderingContext;
            return this;
        }

        public JoinRenderer build() {
            return new JoinRenderer(this);
        }
    }
}
