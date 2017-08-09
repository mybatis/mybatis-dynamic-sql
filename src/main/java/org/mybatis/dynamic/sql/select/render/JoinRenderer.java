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

import java.util.Map;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingUtilities;
import org.mybatis.dynamic.sql.select.JoinColumnAndCondition;
import org.mybatis.dynamic.sql.select.JoinSpecification;
import org.mybatis.dynamic.sql.select.join.JoinModel;

public class JoinRenderer {
    private JoinModel joinModel;
    private Map<SqlTable, String> tableAliases;
    
    private JoinRenderer(JoinModel joinModel, Map<SqlTable, String> tableAliases) {
        this.joinModel = joinModel;
        this.tableAliases = tableAliases;
    }
    
    public String render() {
        return joinModel.joinSpecifications()
                .map(this::render)
                .collect(Collectors.joining(" "));
    }
    
    private String render(JoinSpecification joinSpecification) {
        return "join "
                + RenderingUtilities.tableNameIncludingAlias(joinSpecification.table())
                + render(joinSpecification.firstJoinColumnAndCondition());
    }
    
    private String render(JoinColumnAndCondition<?> joinColumnAndCondition) {
        return " on "
                + RenderingUtilities.nameIncludingTableAlias(joinColumnAndCondition.column())
                + " "
                + joinColumnAndCondition.joinCondition().operator()
                + " "
                + RenderingUtilities.nameIncludingTableAlias(joinColumnAndCondition.joinCondition().column());
    }
    
    public static JoinRenderer of(JoinModel joinModel, Map<SqlTable, String> tableAliases) {
        return new JoinRenderer(joinModel, tableAliases);
    }
}
