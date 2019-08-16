/**
 *    Copyright 2016-2019 the original author or authors.
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
package org.mybatis.dynamic.sql.delete.render;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.where.WhereModel;
import org.mybatis.dynamic.sql.where.render.WhereClauseProvider;
import org.mybatis.dynamic.sql.where.render.WhereRenderer;

public class DeleteRenderer {
    private DeleteModel deleteModel;
    private RenderingStrategy renderingStrategy;
    
    private DeleteRenderer(Builder builder) {
        deleteModel = Objects.requireNonNull(builder.deleteModel);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
    }
    
    public DeleteStatementProvider render() {
        return deleteModel.whereModel()
                .flatMap(this::renderWhereClause)
                .map(this::renderWithWhereClause)
                .orElseGet(this::renderWithoutWhereClause);
    }
    
    private DeleteStatementProvider renderWithWhereClause(WhereClauseProvider whereClauseProvider) {
        return DefaultDeleteStatementProvider.withDeleteStatement(calculateDeleteStatement(whereClauseProvider))
                .withParameters(whereClauseProvider.getParameters())
                .build();
    }
    
    private String calculateDeleteStatement(WhereClauseProvider whereClause) {
        return calculateDeleteStatement()
                + spaceBefore(whereClause.getWhereClause());
    }

    private String calculateDeleteStatement() {
        return "delete from" //$NON-NLS-1$
                + spaceBefore(deleteModel.table().tableNameAtRuntime());
    }
    
    private DeleteStatementProvider renderWithoutWhereClause() {
        return DefaultDeleteStatementProvider.withDeleteStatement(calculateDeleteStatement())
                .build();
    }
    
    private Optional<WhereClauseProvider> renderWhereClause(WhereModel whereModel) {
        return WhereRenderer.withWhereModel(whereModel)
                .withRenderingStrategy(renderingStrategy)
                .withSequence(new AtomicInteger(1))
                .withTableAliasCalculator(TableAliasCalculator.empty())
                .build()
                .render();
    }
    
    public static Builder withDeleteModel(DeleteModel deleteModel) {
        return new Builder().withDeleteModel(deleteModel);
    }
    
    public static class Builder {
        private DeleteModel deleteModel;
        private RenderingStrategy renderingStrategy;

        public Builder withDeleteModel(DeleteModel deleteModel) {
            this.deleteModel = deleteModel;
            return this;
        }
        
        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }
        
        public DeleteRenderer build() {
            return new DeleteRenderer(this);
        }
    }
}
