/*
 *    Copyright 2016-2021 the original author or authors.
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
package examples.kotlin.mybatis3.custom.render

import org.apache.ibatis.annotations.Arg
import org.apache.ibatis.annotations.ConstructorArgs
import org.apache.ibatis.annotations.SelectProvider
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter
import org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper

interface KJsonTestMapper :
    CommonDeleteMapper, CommonInsertMapper<KJsonTestRecord>, CommonSelectMapper, CommonUpdateMapper {
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    @ConstructorArgs(
        Arg(column = "id", id = true, javaType = Int::class),
        Arg(column = "description", javaType = String::class),
        Arg(column = "info", javaType = String::class)
    )
    fun selectMany(selectStatement: SelectStatementProvider): List<KJsonTestRecord>

    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    @ConstructorArgs(
        Arg(column = "id", id = true, javaType = Int::class),
        Arg(column = "description", javaType = String::class),
        Arg(column = "info", javaType = String::class)
    )
    fun selectOne(selectStatement: SelectStatementProvider): KJsonTestRecord?
}
