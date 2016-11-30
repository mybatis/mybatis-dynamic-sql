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
package org.mybatis.qbe;

import org.mybatis.qbe.sql.where.condition.IsLikeCaseInsensitive;
import org.mybatis.qbe.sql.where.render.CriterionRenderer;

@FunctionalInterface
public interface Condition<T> {
    void accept(ConditionVisitor<T> visitor);
    
    /**
     * This method allows a {@link Condition} to alter the field name before it
     * is rendered by the {@link CriterionRenderer}. In the vast majority of cases, the
     * field name should be returned as is back to the renderer.  However, some conditions
     * (like the {@link IsLikeCaseInsensitive} condition) need to alter the field name
     * before it is finally rendered.  This method allows a criterion to be written that effects
     * both the field and the condition.
     *    
     * @param fieldName the field name as calculated by the enclosing {@link Criterion}
     * @return the field name, possibly altered, as it should be finally rendered
     */
    default String renderCriterionField(String fieldName) {
        return fieldName;
    }
}
