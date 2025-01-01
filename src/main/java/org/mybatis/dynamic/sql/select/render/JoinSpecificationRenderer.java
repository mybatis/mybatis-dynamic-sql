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
package org.mybatis.dynamic.sql.select.render;

import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionRenderer;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;

public class JoinSpecificationRenderer extends AbstractBooleanExpressionRenderer {
    private JoinSpecificationRenderer(Builder builder) {
        super("on", builder);  //$NON-NLS-1$
    }

    public static JoinSpecificationRenderer.Builder withJoinSpecification(JoinSpecification joinSpecification) {
        return new Builder(joinSpecification);
    }

    public static class Builder extends AbstractBuilder<Builder> {
        public Builder(JoinSpecification joinSpecification) {
            super(joinSpecification);
        }

        public JoinSpecificationRenderer build() {
            return new JoinSpecificationRenderer(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
