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
package org.mybatis.dynamic.sql.update.render;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;
import org.mybatis.dynamic.sql.util.UpdateMapping;
import org.mybatis.dynamic.sql.where.WhereModel;
import org.mybatis.dynamic.sql.where.render.WhereClauseProvider;
import org.mybatis.dynamic.sql.where.render.WhereRenderer;

public class UpdateRenderer {
    private UpdateModel updateModel;
    private RenderingStrategy renderingStrategy;
    private AtomicInteger sequence = new AtomicInteger(1);
    
    private UpdateRenderer(Builder builder) {
        updateModel = Objects.requireNonNull(builder.updateModel);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
    }
    
    public UpdateStatementProvider render() {
        FragmentCollector fc = calculateColumnMappings();
        
        return updateModel.whereModel()
                .flatMap(this::renderWhereClause)
                .map(wc -> renderWithWhereClause(fc, wc))
                .orElseGet(() -> renderWithoutWhereClause(fc));
    }

    private FragmentCollector calculateColumnMappings() {
        SetPhraseVisitor visitor = new SetPhraseVisitor(sequence, renderingStrategy);

        return updateModel.mapColumnMappings(toFragmentAndParameters(visitor))
                .collect(FragmentCollector.collect());
    }
    
    private UpdateStatementProvider renderWithWhereClause(FragmentCollector columnMappings,
            WhereClauseProvider whereClause) {
        return DefaultUpdateStatementProvider.withUpdateStatement(calculateUpdateStatement(columnMappings, whereClause))
                .withParameters(columnMappings.parameters())
                .withParameters(whereClause.getParameters())
                .build();
    }

    private String calculateUpdateStatement(FragmentCollector fc, WhereClauseProvider whereClause) {
        return calculateUpdateStatement(fc)
                + spaceBefore(whereClause.getWhereClause());
    }
    
    private String calculateUpdateStatement(FragmentCollector fc) {
        return "update" //$NON-NLS-1$
                + spaceBefore(updateModel.table().tableNameAtRuntime())
                + spaceBefore(calculateSetPhrase(fc));
    }
    
    private UpdateStatementProvider renderWithoutWhereClause(FragmentCollector columnMappings) {
        return DefaultUpdateStatementProvider.withUpdateStatement(calculateUpdateStatement(columnMappings))
                .withParameters(columnMappings.parameters())
                .build();
    }

    private String calculateSetPhrase(FragmentCollector collector) {
        return collector.fragments()
                .collect(Collectors.joining(", ", "set ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    private Optional<WhereClauseProvider> renderWhereClause(WhereModel whereModel) {
        return WhereRenderer.withWhereModel(whereModel)
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .withTableAliasCalculator(TableAliasCalculator.empty())
                .build()
                .render();
    }

    private Function<UpdateMapping, FragmentAndParameters> toFragmentAndParameters(SetPhraseVisitor visitor) {
        return updateMapping -> toFragmentAndParameters(visitor, updateMapping);
    }
    
    private FragmentAndParameters toFragmentAndParameters(SetPhraseVisitor visitor, UpdateMapping updateMapping) {
        return updateMapping.accept(visitor);
    }
    
    public static Builder withUpdateModel(UpdateModel updateModel) {
        return new Builder().withUpdateModel(updateModel);
    }
    
    public static class Builder {
        private UpdateModel updateModel;
        private RenderingStrategy renderingStrategy;

        public Builder withUpdateModel(UpdateModel updateModel) {
            this.updateModel = updateModel;
            return this;
        }
        
        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }
        
        public UpdateRenderer build() {
            return new UpdateRenderer(this);
        }
    }
}
