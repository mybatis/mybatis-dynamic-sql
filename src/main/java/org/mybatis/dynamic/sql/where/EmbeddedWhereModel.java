/*
 *    Copyright 2016-2024 the original author or authors.
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

import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionModel;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.where.render.WhereRenderer;

import java.util.Optional;

public class EmbeddedWhereModel extends AbstractBooleanExpressionModel {
    private EmbeddedWhereModel(Builder builder) {
        super(builder);
    }

    public Optional<FragmentAndParameters> render(RenderingContext renderingContext) {
        return WhereRenderer.withWhereModel(this)
                .withRenderingContext(renderingContext)
                .build()
                .render();
    }

    public static class Builder extends AbstractBuilder<Builder> {
        public EmbeddedWhereModel build() {
            return new EmbeddedWhereModel(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
