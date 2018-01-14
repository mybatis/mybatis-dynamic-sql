/**
 *    Copyright 2016-2018 the original author or authors.
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
package org.mybatis.dynamic.sql.util;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.select.SelectModel;

public class SelectMapping extends AbstractColumnMapping implements UpdateMapping {

    private SelectModel selectModel;
    
    private SelectMapping(SqlColumn<?> column, Buildable<SelectModel> selectModelBuilder) {
        super(column);
        selectModel = selectModelBuilder.build();
    }
    
    public SelectModel selectModel() {
        return selectModel;
    }

    @Override
    public <R> R accept(UpdateMappingVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public static SelectMapping of(SqlColumn<?> column, Buildable<SelectModel> selectModelBuilder) {
        return new SelectMapping(column, selectModelBuilder);
    }
}
