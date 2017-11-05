/**
 *    Copyright 2016-2017 the original author or authors.
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
package org.mybatis.dynamic.sql.select.render;

import java.util.Objects;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.join.JoinCriterion;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.select.join.JoinType;

public class JoinRenderer {
    private JoinModel joinModel;
    private SelectModel selectModel;
    
    private JoinRenderer(Builder builder) {
        joinModel = Objects.requireNonNull(builder.joinModel);
        selectModel = Objects.requireNonNull(builder.selectModel);
    }
    
    public String render() {
        return joinModel.mapJoinSpecifications(this::toRenderedString)
                .collect(Collectors.joining(" ")); //$NON-NLS-1$
    }
    
    private String toRenderedString(JoinSpecification joinSpecification) {
        return renderJoinType(joinSpecification.joinType())
                + "join " //$NON-NLS-1$
                + selectModel.calculateTableNameIncludingAlias(joinSpecification.table())
                + " " //$NON-NLS-1$
                + renderConditions(joinSpecification);
    }
    
    private String renderJoinType(JoinType joinType) {
        return joinType.shortType()
                .map(t -> t + " ") //$NON-NLS-1$
                .orElse(""); //$NON-NLS-1$
    }
    
    private String renderConditions(JoinSpecification joinSpecification) {
        return joinSpecification.mapJoinCriteria(this::renderCriterion)
                .collect(Collectors.joining(" ")); //$NON-NLS-1$
    }
    
    private String renderCriterion(JoinCriterion<?> joinCriterion) {
        return joinCriterion.connector()
                + " " //$NON-NLS-1$
                + applyTableAlias(joinCriterion.leftColumn())
                + " " //$NON-NLS-1$
                + joinCriterion.operator()
                + " " //$NON-NLS-1$
                + applyTableAlias(joinCriterion.rightColumn());
    }
    
    private String applyTableAlias(SqlColumn<?> column) {
        return column.applyTableAliasToName(selectModel.aliasMapForColumns());
    }
    
    public static class Builder {
        private JoinModel joinModel;
        private SelectModel selectModel;
        
        public Builder withJoinModel(JoinModel joinModel) {
            this.joinModel = joinModel;
            return this;
        }
        
        public Builder withSelectModel(SelectModel selectModel) {
            this.selectModel = selectModel;
            return this;
        }
        
        public JoinRenderer build() {
            return new JoinRenderer(this);
        }
    }
}
