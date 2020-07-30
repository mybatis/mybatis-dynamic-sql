/**
 *    Copyright 2016-2020 the original author or authors.
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
package org.mybatis.dynamic.sql.util.spring;

import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.GeneralInsertModel;
import org.mybatis.dynamic.sql.insert.InsertModel;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.Buildable;

public class SpringUtils {
    private SpringUtils() {}
    
    public static DeleteStatementProvider buildDelete(Buildable<DeleteModel> deleteStatement) {
        return deleteStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER);
    }
    
    public static GeneralInsertStatementProvider buildGeneralInsert(Buildable<GeneralInsertModel> insertStatement) {
        return insertStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER);
    }
    
    public static <T> InsertStatementProvider<T> buildInsert(Buildable<InsertModel<T>> insertStatement) {
        return insertStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER);
    }
    
    public static SelectStatementProvider buildSelect(Buildable<SelectModel> selectStatement) {
        return selectStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER);
    }
    
    public static UpdateStatementProvider buildUpdate(Buildable<UpdateModel> updateStatement) {
        return updateStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER);
    }
}
