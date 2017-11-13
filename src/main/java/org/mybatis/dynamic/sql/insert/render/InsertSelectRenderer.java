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
package org.mybatis.dynamic.sql.insert.render;

import java.util.Objects;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.insert.InsertSelectModel;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectSupport;

public class InsertSelectRenderer {

    private InsertSelectModel model;
    
    private InsertSelectRenderer(InsertSelectModel model) {
        this.model = Objects.requireNonNull(model);
    }
    
    public InsertSelectSupport render(RenderingStrategy renderingStrategy) {
        SelectSupport selectSupport = model.selectModel().render(renderingStrategy);
        
        return new InsertSelectSupport.Builder()
                .withTableName(model.table().name())
                .withColumnsPhrase(calculateColumnsPhrase())
                .withSelectStatement(selectSupport.getFullSelectStatement())
                .withParameters(selectSupport.getParameters())
                .build();
    }
    
    private String calculateColumnsPhrase() {
        return model.mapColumns(SqlColumn::name)
                .collect(Collectors.joining(", ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    public static InsertSelectRenderer of(InsertSelectModel model) {
        return new InsertSelectRenderer(model);
    }
}
