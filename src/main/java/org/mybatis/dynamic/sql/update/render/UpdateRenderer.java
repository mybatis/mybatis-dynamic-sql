/**
 *    Copyright 2016-2017 the original author or authors.
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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;
import org.mybatis.dynamic.sql.util.UpdateMapping;
import org.mybatis.dynamic.sql.where.WhereModel;
import org.mybatis.dynamic.sql.where.render.WhereRenderer;
import org.mybatis.dynamic.sql.where.render.WhereSupport;

public class UpdateRenderer {
    private UpdateModel updateModel;
    
    private UpdateRenderer(UpdateModel updateModel) {
        this.updateModel = updateModel;
    }
    
    public UpdateSupport render(RenderingStrategy renderingStrategy) {
        SetPhraseVisitor visitor = new SetPhraseVisitor(renderingStrategy);

        FragmentCollector fc = updateModel.mapColumnValues(toFragmentAndParameters(visitor))
                .collect(FragmentCollector.collect());
        
        UpdateSupport.Builder builder = new UpdateSupport.Builder()
                .withTableName(updateModel.table().name())
                .withSetClause(calculateSetPhrase(fc))
                .withParameters(fc.parameters());
        
        updateModel.whereModel().ifPresent(applyWhere(builder, renderingStrategy));
        
        return builder.build();
    }

    private String calculateSetPhrase(FragmentCollector collector) {
        return collector.fragments()
                .collect(Collectors.joining(", ", "set ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    private Consumer<WhereModel> applyWhere(UpdateSupport.Builder builder, RenderingStrategy renderingStrategy) {
        return whereModel -> applyWhere(builder, renderingStrategy, whereModel); 
    }
    
    private void applyWhere(UpdateSupport.Builder builder, RenderingStrategy renderingStrategy, WhereModel whereModel) {
        WhereSupport whereSupport = new WhereRenderer.Builder()
                .withWhereModel(whereModel)
                .withRenderingStrategy(renderingStrategy)
                .withSequence(new AtomicInteger(1))
                .withTableAliasCalculator(TableAliasCalculator.empty())
                .build()
                .render();
        
        builder.withWhereClause(whereSupport.getWhereClause());
        builder.withParameters(whereSupport.getParameters());
    }
    
    private Function<UpdateMapping, FragmentAndParameters> toFragmentAndParameters(SetPhraseVisitor visitor) {
        return updateMapping -> toFragmentAndParameters(visitor, updateMapping);
    }
    
    private FragmentAndParameters toFragmentAndParameters(SetPhraseVisitor visitor, UpdateMapping updateMapping) {
        return updateMapping.accept(visitor);
    }
    
    public static UpdateRenderer of(UpdateModel updateModel) {
        return new UpdateRenderer(updateModel);
    }
}
