/*
 *    Copyright 2016-2025 the original author or authors.
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
package org.mybatis.dynamic.sql;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public abstract class AbstractNoValueCondition<T> implements VisitableCondition<T> {

    protected <S extends AbstractNoValueCondition<?>> S filterSupport(BooleanSupplier booleanSupplier,
            Supplier<S> emptySupplier, S self) {
        if (isEmpty()) {
            return self;
        } else {
            return booleanSupplier.getAsBoolean() ? self : emptySupplier.get();
        }
    }

    public abstract String operator();

    @Override
    public FragmentAndParameters renderCondition(RenderingContext renderingContext, BindableColumn<T> leftColumn) {
        return FragmentAndParameters.fromFragment(operator());
    }
}
