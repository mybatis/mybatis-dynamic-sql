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

import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.render.RenderingUtilities;
import org.mybatis.dynamic.sql.select.JoinColumnAndCondition;
import org.mybatis.dynamic.sql.select.JoinSpecification;
import org.mybatis.dynamic.sql.select.join.JoinModel;

public class JoinRenderer {
    private JoinModel joinModel;
    
    private JoinRenderer(JoinModel joinModel) {
        this.joinModel = joinModel;
    }
    
    public String render() {
        return joinModel.joinSpecifications()
                .map(this::render)
                .collect(Collectors.joining(" "));
    }
    
    private String render(JoinSpecification joinSpecification) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("join ");
        buffer.append(RenderingUtilities.nameIncludingAlias(joinSpecification.table()));
        buffer.append(render(joinSpecification.firstJoinColumnAndCondition()));
        return buffer.toString();
    }
    
    private String render(JoinColumnAndCondition<?> joinColumnAndCondition) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(" on ");
        buffer.append(RenderingUtilities.nameIncludingTableAlias(joinColumnAndCondition.column()));
        buffer.append(" = ");
        buffer.append(RenderingUtilities.nameIncludingTableAlias(joinColumnAndCondition.joinCondition().column()));
        return buffer.toString();
    }
    
    public static JoinRenderer of(JoinModel joinModel) {
        return new JoinRenderer(joinModel);
    }
}
