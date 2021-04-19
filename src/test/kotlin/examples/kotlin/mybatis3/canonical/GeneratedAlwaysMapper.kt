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
package examples.kotlin.mybatis3.canonical

import org.apache.ibatis.annotations.InsertProvider
import org.apache.ibatis.annotations.Options
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter
import examples.kotlin.mybatis3.canonical.GeneratedAlwaysDynamicSqlSupport.generatedAlways
import examples.kotlin.mybatis3.canonical.GeneratedAlwaysDynamicSqlSupport.firstName
import examples.kotlin.mybatis3.canonical.GeneratedAlwaysDynamicSqlSupport.lastName
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.insert

interface GeneratedAlwaysMapper {
    @InsertProvider(type = SqlProviderAdapter::class, method = "insert")
    @Options(useGeneratedKeys = true, keyProperty="record.id,record.fullName", keyColumn = "id,full_name")
    fun insert(insertStatement: InsertStatementProvider<GeneratedAlwaysRecord>): Int

    @InsertProvider(type = SqlProviderAdapter::class, method = "generalInsert")
    fun generalInsert(insertStatement: GeneralInsertStatementProvider): Int
}

fun GeneratedAlwaysMapper.insert(record: GeneratedAlwaysRecord): Int {
    return insert(this::insert, record, generatedAlways) {
        map(firstName).toProperty("firstName")
        map(lastName).toProperty("lastName")
    }
}
