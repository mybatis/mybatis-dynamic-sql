/*
 *    Copyright 2016-2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.where;

import java.util.function.Consumer;

import org.mybatis.dynamic.sql.dsl.AbstractBooleanOperationsFinisher;

@FunctionalInterface
public interface WhereApplier {

    void accept(AbstractBooleanOperationsFinisher<?> whereFinisher);

    /**
     * Return a composed where applier that performs this operation followed by the after operation.
     *
     * @param after the operation to perform after this operation
     * @return a composed where applier that performs this operation followed by the after operation.
     */
    default WhereApplier andThen(Consumer<AbstractBooleanOperationsFinisher<?>> after) {
        return t -> {
            accept(t);
            after.accept(t);
        };
    }
}
