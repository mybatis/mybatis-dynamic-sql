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
package org.mybatis.qbe.sql.where;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import org.mybatis.qbe.sql.SqlCriterion;
import org.mybatis.qbe.sql.SqlField;
import org.mybatis.qbe.sql.where.render.CriterionRenderer;
import org.mybatis.qbe.sql.where.render.RenderedCriterion;

public class WhereSupport {
    private AtomicInteger sequence = new AtomicInteger(1);
    private Map<String, Object> parameters = new HashMap<>();
    private StringBuilder buffer = new StringBuilder("where"); //$NON-NLS-1$
    private Function<SqlField<?>, String> nameFunction;

    private WhereSupport(Function<SqlField<?>, String> nameFunction) {
        this.nameFunction = nameFunction;
    }
    
    private void addCriterion(SqlCriterion<?> criterion) {
        RenderedCriterion rc = CriterionRenderer.of(criterion, sequence, nameFunction).render();
        buffer.append(rc.whereClauseFragment());
        parameters.putAll(rc.fragmentParameters());
    }
    
    public Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }
    
    public String getWhereClause() {
        return buffer.toString();
    }
    
    public static WhereSupport of(Function<SqlField<?>, String> nameFunction, Stream<SqlCriterion<?>> criteria) {
        WhereSupport whereSupport = new WhereSupport(nameFunction);
        criteria.forEach(whereSupport::addCriterion);
        return whereSupport;
    }
}
