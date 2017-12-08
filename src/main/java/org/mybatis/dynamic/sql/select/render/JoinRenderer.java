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

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceAfter;
import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.Objects;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.select.QueryExpressionModel;
import org.mybatis.dynamic.sql.select.join.JoinCriterion;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;

public class JoinRenderer {
    private JoinModel joinModel;
    private QueryExpressionModel queryExpression;
    
    private JoinRenderer(Builder builder) {
        joinModel = Objects.requireNonNull(builder.joinModel);
        queryExpression = Objects.requireNonNull(builder.queryExpression);
    }
    
    public String render() {
        return joinModel.mapJoinSpecifications(this::toRenderedString)
                .collect(Collectors.joining(" ")); //$NON-NLS-1$
    }
    
    private String toRenderedString(JoinSpecification joinSpecification) {
        return spaceAfter(joinSpecification.joinType().shortType())
                + "join" //$NON-NLS-1$
                + spaceBefore(queryExpression.calculateTableNameIncludingAlias(joinSpecification.table()))
                + spaceBefore(renderConditions(joinSpecification));
    }
    
    private String renderConditions(JoinSpecification joinSpecification) {
        return joinSpecification.mapJoinCriteria(this::renderCriterion)
                .collect(Collectors.joining(" ")); //$NON-NLS-1$
    }
    
    private String renderCriterion(JoinCriterion joinCriterion) {
        return joinCriterion.connector()
                + spaceBefore(applyTableAlias(joinCriterion.leftColumn()))
                + spaceBefore(joinCriterion.operator())
                + spaceBefore(applyTableAlias(joinCriterion.rightColumn()));
    }
    
    private String applyTableAlias(BasicColumn column) {
        return column.renderWithTableAlias(queryExpression.tableAliasCalculator());
    }
    
    public static Builder withJoinModel(JoinModel joinModel) {
        return new Builder().withJoinModel(joinModel);
    }
    
    public static class Builder {
        private JoinModel joinModel;
        private QueryExpressionModel queryExpression;
        
        public Builder withJoinModel(JoinModel joinModel) {
            this.joinModel = joinModel;
            return this;
        }
        
        public Builder withQueryExpression(QueryExpressionModel queryExpression) {
            this.queryExpression = queryExpression;
            return this;
        }
        
        public JoinRenderer build() {
            return new JoinRenderer(this);
        }
    }
}
