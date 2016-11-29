/**
 *    Copyright 2016 the original author or authors.
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
package org.mybatis.qbe.sql.where.render;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.mybatis.qbe.sql.SqlCriterion;
import org.mybatis.qbe.sql.SqlField;

public class CriterionRenderer<T> {
    private StringBuilder buffer = new StringBuilder();
    private Map<String, Object> parameters = new HashMap<>();
    private SqlCriterion<T> criterion;
    private AtomicInteger sequence;
    private Function<SqlField<?>, String> nameFunction;
    
    protected CriterionRenderer(SqlCriterion<T> criterion, AtomicInteger sequence, Function<SqlField<?>, String> nameFunction) {
        this.criterion = criterion;
        this.sequence = sequence;
        this.nameFunction = nameFunction;
    }
    
    public RenderedCriterion render() {
        buffer.append(' ');
        
        renderConnector();

        if (criterion.hasSubCriteria()) {
            buffer.append('(');
            renderCriteria();
            buffer.append(')');
        } else {
            renderCriteria();
        }
        
        return RenderedCriterion.of(buffer.toString(), parameters);
    }
    
    private void renderConnector() {
        criterion.connector().ifPresent(c -> {
            buffer.append(c);
            buffer.append(' ');
        });
    }
    
    private void renderCriteria() {
        buffer.append(criterion.renderField(nameFunction));
        buffer.append(' ');
        renderCondition();
        criterion.visitSubCriteria(c -> handleSubCriterion(c, sequence));
    }

    private void renderCondition() {
        ConditionRenderer<T> visitor = ConditionRenderer.of(sequence, criterion.field());
        criterion.condition().accept(visitor);
        buffer.append(visitor.fragment());
        parameters.putAll(visitor.parameters());
    }

    private void handleSubCriterion(SqlCriterion<?> subCriterion, AtomicInteger sequence) {
        RenderedCriterion rc = new CriterionRenderer<>(subCriterion, sequence, nameFunction).render();
        buffer.append(rc.whereClauseFragment());
        parameters.putAll(rc.fragmentParameters());
    }
    
    public static <T> CriterionRenderer<T> of(SqlCriterion<T> criterion, AtomicInteger sequence, Function<SqlField<?>, String> nameFunction) {
        return new CriterionRenderer<>(criterion, sequence, nameFunction);
    }
}
