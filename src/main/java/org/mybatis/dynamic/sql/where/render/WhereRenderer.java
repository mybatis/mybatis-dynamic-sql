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
package org.mybatis.dynamic.sql.where.render;

import java.util.Optional;

import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionRenderer;
import org.mybatis.dynamic.sql.exception.NonRenderingWhereClauseException;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.where.WhereModel;

public class WhereRenderer extends AbstractBooleanExpressionRenderer<WhereModel> {
    private WhereRenderer(Builder builder) {
        super("where", builder); //$NON-NLS-1$
    }

    @Override
    public Optional<FragmentAndParameters> render() {
        Optional<FragmentAndParameters> whereClause = super.render();

        if (whereClause.isPresent() || renderingContext.isNonRenderingClauseAllowed()) {
            return whereClause;
        } else {
            throw new NonRenderingWhereClauseException();
        }
    }

    public static Builder withWhereModel(WhereModel whereModel) {
        return new Builder(whereModel);
    }

    public static class Builder extends AbstractBuilder<WhereModel, Builder> {
        public Builder(WhereModel whereModel) {
            super(whereModel);
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public WhereRenderer build() {
            return new WhereRenderer(this);
        }
    }
}
