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

import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.RenderingUtilities;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
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
        return model.criteria()
                .map(this::renderCriterionIncludingTableAlias)
                .collect(WhereFragmentCollector.toWhereSupport());
    }
    
    public WhereSupport renderCriteriaIgnoringTableAlias() {
        return model.criteria()
                .map(this::renderCriterionIgnoringTableAlias)
                .collect(WhereFragmentCollector.toWhereSupport());
    }
    
    private FragmentAndParameters renderCriterionIncludingTableAlias(SqlCriterion<?> criterion) {
        return CriterionRenderer.of(sequence, renderingStrategy, RenderingUtilities::nameIncludingTableAlias).render(criterion);
    }
    
    private FragmentAndParameters renderCriterionIgnoringTableAlias(SqlCriterion<?> criterion) {
        return CriterionRenderer.of(sequence, renderingStrategy, RenderingUtilities::nameIgnoringTableAlias).render(criterion);
    }
    
    public static WhereRenderer of(WhereModel model, RenderingStrategy renderingStrategy) {
        return new WhereRenderer(model, renderingStrategy);
    }
}
