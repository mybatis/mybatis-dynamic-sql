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
package org.mybatis.dynamic.sql.where.render;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;
import org.mybatis.dynamic.sql.where.WhereModel;

public class WhereRenderer {
    private WhereModel model;
    private AtomicInteger sequence = new AtomicInteger(1);
    private RenderingStrategy renderingStrategy;
    
    private WhereRenderer(WhereModel model, RenderingStrategy renderingStrategy) {
        this.model = model;
        this.renderingStrategy = renderingStrategy;
    }
    
    public WhereSupport renderCriteriaIncludingTableAlias() {
        return renderCriteria(model.criteria().map(this::renderCriterionIncludingTableAlias));
    }
    
    public WhereSupport renderCriteriaIgnoringTableAlias() {
        return renderCriteria(model.criteria().map(this::renderCriterionIgnoringTableAlias));
    }
    
    private WhereSupport renderCriteria(Stream<FragmentAndParameters> stream) {
        FragmentCollector fc = stream.collect(FragmentCollector.fragmentAndParameterCollector());

        return WhereSupport.of(fc.fragments().collect(Collectors.joining(" ", "where ", "")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                fc.parameters());
    }
    
    private FragmentAndParameters renderCriterionIncludingTableAlias(SqlCriterion<?> criterion) {
        return CriterionRenderer.newRendererIncludingTableAlias(sequence, renderingStrategy).render(criterion);
    }
    
    private FragmentAndParameters renderCriterionIgnoringTableAlias(SqlCriterion<?> criterion) {
        return CriterionRenderer.newRendererIgnoringTableAlias(sequence, renderingStrategy).render(criterion);
    }
    
    public static WhereRenderer of(WhereModel model, RenderingStrategy renderingStrategy) {
        return new WhereRenderer(model, renderingStrategy);
    }
}
