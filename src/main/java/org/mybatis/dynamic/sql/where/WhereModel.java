/**
 *    Copyright 2016-2018 the original author or authors.
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
package org.mybatis.dynamic.sql.where;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.where.render.WhereClauseProvider;
import org.mybatis.dynamic.sql.where.render.WhereRenderer;

public class WhereModel {
    private static final WhereClauseProvider EMPTY_WHERE_CLAUSE =
            new WhereClauseProvider.Builder().withWhereClause("").build(); //$NON-NLS-1$
    
    private List<SqlCriterion<?>> criteria = new ArrayList<>();
    
    private WhereModel(List<SqlCriterion<?>> criteria) {
        this.criteria.addAll(criteria);
    }
    
    public <R> Stream<R> mapCriteria(Function<SqlCriterion<?>, R> mapper) {
        return criteria.stream().map(mapper);
    }

    /**
     * Renders a where clause without table aliases.
     * 
     * @param renderingStrategy rendering strategy
     * @return rendered where clause
     */
    public WhereClauseProvider render(RenderingStrategy renderingStrategy) {
        return WhereRenderer.withWhereModel(this)
                .withRenderingStrategy(renderingStrategy)
                .withSequence(new AtomicInteger(1))
                .withTableAliasCalculator(TableAliasCalculator.empty())
                .build()
                .render()
                .orElse(EMPTY_WHERE_CLAUSE);
    }
    
    public WhereClauseProvider render(RenderingStrategy renderingStrategy,
            TableAliasCalculator tableAliasCalculator) {
        return WhereRenderer.withWhereModel(this)
                .withRenderingStrategy(renderingStrategy)
                .withSequence(new AtomicInteger(1))
                .withTableAliasCalculator(tableAliasCalculator)
                .build()
                .render()
                .orElse(EMPTY_WHERE_CLAUSE);
    }
    
    public WhereClauseProvider render(RenderingStrategy renderingStrategy,
            String parameterName) {
        return WhereRenderer.withWhereModel(this)
                .withRenderingStrategy(renderingStrategy)
                .withSequence(new AtomicInteger(1))
                .withTableAliasCalculator(TableAliasCalculator.empty())
                .withParameterName(parameterName)
                .build()
                .render()
                .orElse(EMPTY_WHERE_CLAUSE);
    }
    
    public WhereClauseProvider render(RenderingStrategy renderingStrategy,
            TableAliasCalculator tableAliasCalculator, String parameterName) {
        return WhereRenderer.withWhereModel(this)
                .withRenderingStrategy(renderingStrategy)
                .withSequence(new AtomicInteger(1))
                .withTableAliasCalculator(tableAliasCalculator)
                .withParameterName(parameterName)
                .build()
                .render()
                .orElse(EMPTY_WHERE_CLAUSE);
    }
    
    public static WhereModel of(List<SqlCriterion<?>> criteria) {
        return new WhereModel(criteria);
    }
}
