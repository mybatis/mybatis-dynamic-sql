/*
 *    Copyright 2016-2022 the original author or authors.
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
package org.mybatis.dynamic.sql.select.render;

import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;

public abstract class AbstractQueryRendererBuilder<T extends AbstractQueryRendererBuilder<T>> {
    RenderingStrategy renderingStrategy;
    AtomicInteger sequence;
    TableAliasCalculator parentTableAliasCalculator;

    public T withRenderingStrategy(RenderingStrategy renderingStrategy) {
        this.renderingStrategy = renderingStrategy;
        return getThis();
    }

    public T withSequence(AtomicInteger sequence) {
        this.sequence = sequence;
        return getThis();
    }

    public T withParentTableAliasCalculator(TableAliasCalculator parentTableAliasCalculator) {
        this.parentTableAliasCalculator = parentTableAliasCalculator;
        return getThis();
    }

    abstract T getThis();
}
