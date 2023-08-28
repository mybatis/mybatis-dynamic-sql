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
package org.mybatis.dynamic.sql.select.render;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.Objects;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.TableExpressionVisitor;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.SubQuery;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class TableExpressionRenderer implements TableExpressionVisitor<FragmentAndParameters> {
    private final RenderingContext renderingContext;

    private TableExpressionRenderer(Builder builder) {
        renderingContext = Objects.requireNonNull(builder.renderingContext);
    }

    @Override
    public FragmentAndParameters visit(SqlTable table) {
        return FragmentAndParameters.fromFragment(
                        renderingContext.tableAliasCalculator().aliasForTable(table)
                                .map(a -> table.tableNameAtRuntime() + spaceBefore(a))
                                .orElseGet(table::tableNameAtRuntime));
    }

    @Override
    public FragmentAndParameters visit(SubQuery subQuery) {
        SelectStatementProvider selectStatement = new SelectRenderer.Builder()
                .withSelectModel(subQuery.selectModel())
                .withRenderingContext(renderingContext)
                .build()
                .render();

        String fragment = "(" + selectStatement.getSelectStatement() + ")"; //$NON-NLS-1$ //$NON-NLS-2$

        fragment = applyAlias(fragment, subQuery);

        return FragmentAndParameters.withFragment(fragment)
                .withParameters(selectStatement.getParameters())
                .build();
    }

    private String applyAlias(String fragment, SubQuery subQuery) {
        return subQuery.alias()
                .map(a -> fragment + spaceBefore(a))
                .orElse(fragment);
    }

    public static class Builder {
        private RenderingContext renderingContext;

        public Builder withRenderingContext(RenderingContext renderingContext) {
            this.renderingContext = renderingContext;
            return this;
        }

        public TableExpressionRenderer build() {
            return new TableExpressionRenderer(this);
        }
    }
}
