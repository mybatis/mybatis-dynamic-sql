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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.AbstractColumnAndValue;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;
import org.mybatis.dynamic.sql.where.WhereModel;
import org.mybatis.dynamic.sql.where.render.WhereRenderer;
import org.mybatis.dynamic.sql.where.render.WhereSupport;

public class UpdateRenderer {
    private UpdateModel updateModel;
    private SetPhraseVisitor visitor;
    
    private UpdateRenderer(UpdateModel updateModel) {
        this.updateModel = updateModel;
    }
    
    public UpdateSupport render(RenderingStrategy renderingStrategy) {
        visitor = new SetPhraseVisitor(renderingStrategy);
        return updateModel.whereModel().map(wm ->renderWithWhereClause(wm, renderingStrategy))
                .orElse(renderWithoutWhereClause());
    }
    
    private UpdateSupport renderWithoutWhereClause() {
        FragmentCollector setValuesCollector = renderSetValues();
        return UpdateSupport.of(setValuesCollector.fragments().collect(Collectors.joining(", ", "set ", "")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                null, setValuesCollector.parameters(), updateModel.table());
        
    }
    
    private UpdateSupport renderWithWhereClause(WhereModel whereModel, RenderingStrategy renderingStrategy) {
        Map<String, Object> parameters = new HashMap<>();
        FragmentCollector setValuesCollector = renderSetValues();
        WhereSupport whereSupport = WhereRenderer.of(whereModel, renderingStrategy).renderCriteriaIgnoringTableAlias();
        parameters.putAll(setValuesCollector.parameters());
        parameters.putAll(whereSupport.getParameters());
        return UpdateSupport.of(setValuesCollector.fragments().collect(Collectors.joining(", ", "set ", "")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                whereSupport.getWhereClause(), parameters, updateModel.table());
    }
    
    private FragmentCollector renderSetValues() {
        return updateModel.columnValues()
                .map(this::transform)
                .collect(FragmentCollector.fragmentAndParameterCollector());
    }
    
    private FragmentAndParameters transform(AbstractColumnAndValue columnAndValue) {
        return columnAndValue.accept(visitor);
    }
    
    public static UpdateRenderer of(UpdateModel updateModel) {
        return new UpdateRenderer(updateModel);
    }
}
