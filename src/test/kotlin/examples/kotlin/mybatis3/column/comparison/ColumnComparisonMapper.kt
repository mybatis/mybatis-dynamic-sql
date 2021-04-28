/*
 *    Copyright 2016-2021 the original author or authors.
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
package examples.kotlin.mybatis3.column.comparison

import org.apache.ibatis.annotations.Arg
import org.apache.ibatis.annotations.ConstructorArgs
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.SelectProvider
import org.apache.ibatis.type.JdbcType
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter

@Mapper
interface ColumnComparisonMapper {
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    @ConstructorArgs(
        Arg(column = "number1", javaType = Int::class, jdbcType = JdbcType.INTEGER, id = true),
        Arg(column = "number2", javaType = Int::class, jdbcType = JdbcType.INTEGER, id = true)
    )
    fun selectMany(selectStatement: SelectStatementProvider): List<ColumnComparisonRecord>
}
