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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingUtilities;
import org.mybatis.dynamic.sql.select.join.JoinCriterion;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;

public class JoinRenderer {
    private JoinModel joinModel;
    private Map<SqlTable, String> tableAliases = new HashMap<>();
    
    private JoinRenderer(JoinModel joinModel, Map<SqlTable, String> tableAliases) {
        this.joinModel = Objects.requireNonNull(joinModel);
        this.tableAliases.putAll(tableAliases);
    }
    
    public String render() {
        return joinModel.joinSpecifications()
                .map(this::render)
                .collect(Collectors.joining(" ")); //$NON-NLS-1$
    }
    
    private String render(JoinSpecification joinSpecification) {
        return "join " //$NON-NLS-1$
                + RenderingUtilities.tableNameIncludingAlias(joinSpecification.table(), tableAliases)
                + " " //$NON-NLS-1$
                + renderConditions(joinSpecification);
    }
    
    private String renderConditions(JoinSpecification joinSpecification) {
        return joinSpecification.mapJoinCriteria(this::renderCriterion)
                .collect(Collectors.joining(" ")); //$NON-NLS-1$
    }
    
    private String renderCriterion(JoinCriterion<?> joinCriterion) {
        return joinCriterion.connector()
                + " " //$NON-NLS-1$
                + RenderingUtilities.columnNameIncludingTableAlias(joinCriterion.leftColumn(), tableAliases)
                + " " //$NON-NLS-1$
                + joinCriterion.operator()
                + " " //$NON-NLS-1$
                + RenderingUtilities.columnNameIncludingTableAlias(joinCriterion.rightColumn(), tableAliases);
    }
    
    public static JoinRenderer of(JoinModel joinModel, Map<SqlTable, String> tableAliases) {
        return new JoinRenderer(joinModel, tableAliases);
    }
}
