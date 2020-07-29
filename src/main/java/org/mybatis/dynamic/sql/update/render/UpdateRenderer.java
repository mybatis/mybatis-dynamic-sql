/**
 *    Copyright 2016-2020 the original author or authors.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.AbstractColumnMapping;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
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
        SetPhraseVisitor visitor = new SetPhraseVisitor(sequence, renderingStrategy);

        List<Optional<FragmentAndParameters>> fragmentsAndParameters =
                updateModel.mapColumnMappings(toFragmentAndParameters(visitor))
                .collect(Collectors.toList());
        
        return updateModel.whereModel()
                .flatMap(this::renderWhereClause)
                .map(wc -> renderWithWhereClause(fragmentsAndParameters, wc))
                .orElseGet(() -> renderWithoutWhereClause(fragmentsAndParameters));
    }

    private UpdateStatementProvider renderWithWhereClause(List<Optional<FragmentAndParameters>> fragmentsAndParameters,
            WhereClauseProvider whereClause) {
        return DefaultUpdateStatementProvider
                .withUpdateStatement(calculateUpdateStatement(fragmentsAndParameters, whereClause))
                .withParameters(calculateParameters(fragmentsAndParameters))
                .withParameters(whereClause.getParameters())
                .build();
    }

    private String calculateUpdateStatement(List<Optional<FragmentAndParameters>> fragmentsAndParameters,
            WhereClauseProvider whereClause) {
        return calculateUpdateStatement(fragmentsAndParameters)
                + spaceBefore(whereClause.getWhereClause());
    }
    
    private String calculateUpdateStatement(List<Optional<FragmentAndParameters>> fragmentsAndParameters) {
        return "update" //$NON-NLS-1$
                + spaceBefore(updateModel.table().tableNameAtRuntime())
                + spaceBefore(calculateSetPhrase(fragmentsAndParameters));
    }
    
    private UpdateStatementProvider renderWithoutWhereClause(
            List<Optional<FragmentAndParameters>> fragmentsAndParameters) {
        return DefaultUpdateStatementProvider.withUpdateStatement(calculateUpdateStatement(fragmentsAndParameters))
                .withParameters(calculateParameters(fragmentsAndParameters))
                .build();
    }

    private String calculateSetPhrase(List<Optional<FragmentAndParameters>> fragmentsAndParameters) {
        return fragmentsAndParameters.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(FragmentAndParameters::fragment)
                .collect(Collectors.joining(", ", "set ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    private Map<String, Object> calculateParameters(List<Optional<FragmentAndParameters>> fragmentsAndParameters) {
        return fragmentsAndParameters.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(FragmentAndParameters::parameters)
                .collect(HashMap::new, HashMap::putAll, HashMap::putAll);
    }
    
    private Optional<WhereClauseProvider> renderWhereClause(WhereModel whereModel) {
        return WhereRenderer.withWhereModel(whereModel)
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .withTableAliasCalculator(TableAliasCalculator.empty())
                .build()
                .render();
    }

    private <T> Function<AbstractColumnMapping<?>, Optional<FragmentAndParameters>> toFragmentAndParameters(
            SetPhraseVisitor visitor) {
        return updateMapping -> toFragmentAndParameters(visitor, updateMapping);
    }
    
    private Optional<FragmentAndParameters> toFragmentAndParameters(SetPhraseVisitor visitor,
            AbstractColumnMapping<?> updateMapping) {
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
