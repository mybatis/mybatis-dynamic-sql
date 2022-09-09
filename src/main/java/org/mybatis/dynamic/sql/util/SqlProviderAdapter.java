/*
 *    Copyright 2016-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;

/**
 * Adapter for use with MyBatis SQL provider annotations.
 *
 * @author Jeff Butler
 */
public class SqlProviderAdapter {

    public String delete(DeleteStatementProvider deleteStatement) {
        return deleteStatement.getDeleteStatement();
    }

    public String generalInsert(GeneralInsertStatementProvider insertStatement) {
        return insertStatement.getInsertStatement();
    }

    public String insert(InsertStatementProvider<?> insertStatement) {
        return insertStatement.getInsertStatement();
    }

    public String insertMultiple(MultiRowInsertStatementProvider<?> insertStatement) {
        return insertStatement.getInsertStatement();
    }

    /**
     * This adapter method is intended for use with MyBatis' &#064;InsertProvider annotation when there are generated
     * values expected from executing the insert statement. The canonical method signature for using this adapter method
     * is as follows:
     *
     * <pre>
     * public interface FooMapper {
     *     &#064;InsertProvider(type=SqlProviderAdapter.class, method="insertMultipleWithGeneratedKeys")
     *     &#064;Options(useGeneratedKeys=true, keyProperty="records.id")
     *     int insertMultiple(String insertStatement, &#064;Param("records") List&lt;Foo&gt; records)
     * }
     * </pre>
     *
     * @param parameterMap
     *            The parameter map is automatically created by MyBatis when there are multiple parameters in the insert
     *            method.
     *
     * @return the SQL statement contained in the parameter map. This is assumed to be the one and only map entry of
     *         type String.
     */
    public String insertMultipleWithGeneratedKeys(Map<String, Object> parameterMap) {
        List<String> entries = parameterMap.entrySet().stream()
                .filter(e -> e.getKey().startsWith("param")) //$NON-NLS-1$
                .map(Map.Entry::getValue)
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.toList());

        if (entries.size() == 1) {
            return entries.get(0);
        } else {
            throw new IllegalArgumentException(Messages.getString("ERROR.30")); //$NON-NLS-1$
        }
    }

    public String insertSelect(InsertSelectStatementProvider insertStatement) {
        return insertStatement.getInsertStatement();
    }

    public String select(SelectStatementProvider selectStatement) {
        return selectStatement.getSelectStatement();
    }

    public String update(UpdateStatementProvider updateStatement) {
        return updateStatement.getUpdateStatement();
    }
}
