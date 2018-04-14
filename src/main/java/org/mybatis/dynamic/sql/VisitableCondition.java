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
package org.mybatis.dynamic.sql;

@FunctionalInterface
public interface VisitableCondition<T> {
    <R> R accept(ConditionVisitor<T,R> visitor);

    /**
     * Subclasses can override this to inform the renderer if the condition should not be included
     * in the rendered SQL.  For example, IsEqualWhenPresent will not render if the value is null.
     * 
     * @return true if the condition should render.
     */
    default boolean shouldRender() {
        return true;
    }
}
