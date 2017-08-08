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

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.where.WhereModel;

public class WhereRenderer {
    private WhereModel model;
    private AtomicInteger sequence = new AtomicInteger(1);
    private RenderingStrategy renderingStrategy;
    private Map<SqlTable, String> tableAliases;
    
    private WhereRenderer(WhereModel model, RenderingStrategy renderingStrategy, Map<SqlTable, String> tableAliases) {
        this.model = model;
        this.renderingStrategy = renderingStrategy;
        this.tableAliases = tableAliases;
    }
    
    public WhereSupport render() {
        return model.criteria()
                .map(this::render)
                .collect(WhereFragmentCollector.toWhereSupport());
    }
    
    private FragmentAndParameters render(SqlCriterion<?> criterion) {
        return CriterionRenderer.of(sequence, renderingStrategy, tableAliases)
                .render(criterion);
    }
    
    public static WhereRenderer of(WhereModel model, RenderingStrategy renderingStrategy, Map<SqlTable, String> tableAliases) {
        return new WhereRenderer(model, renderingStrategy, tableAliases);
    }
}
