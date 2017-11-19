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
package org.mybatis.dynamic.sql.util;

import org.mybatis.dynamic.sql.delete.render.DeleteStatement;
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatement;
import org.mybatis.dynamic.sql.insert.render.InsertStatement;
import org.mybatis.dynamic.sql.select.render.SelectStatement;
import org.mybatis.dynamic.sql.update.render.UpdateStatement;

/**
 * Adapter for use with MyBatis SQL provider annotations.
 * 
 * @author Jeff Butler
 *
 */
public class SqlProviderAdapter {

    public String delete(DeleteStatement deleteStatement) {
        return deleteStatement.getDeleteStatement();
    }
    
    public String insert(InsertStatement<?> insertStatement) {
        return insertStatement.getInsertStatement();
    }
    
    public String insertSelect(InsertSelectStatement insertStatement) {
        return insertStatement.getInsertStatement();
    }
    
    public String select(SelectStatement selectStatement) {
        return selectStatement.getSelectStatement();
    }

    public String update(UpdateStatement updateStatement) {
        return updateStatement.getUpdateStatement();
    }
}
