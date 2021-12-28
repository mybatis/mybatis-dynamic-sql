/*
 *    Copyright 2016-2021 the original author or authors.
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

import java.util.List;

/**
 * Group sub criteria within parentheses.
 *
 * @author <a href="mailto:daonan.zhan@gmail.com">Joshua</a>
 */
public class CriterionGroup extends SqlCriterion {

    private CriterionGroup(Builder builder) {
        super(builder);
    }

    @Override
    public <R> R accept(SqlCriterionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public List<SqlCriterion> getSubCriteria() {
        return subCriteria;
    }

    public static Builder withConnector(String connector) {
        return new Builder().withConnector(connector);
    }

    public static class Builder extends AbstractBuilder<Builder> {

        @Override
        protected Builder getThis() {
            return this;
        }

        public CriterionGroup build() {
            return new CriterionGroup(this);
        }
    }
}
