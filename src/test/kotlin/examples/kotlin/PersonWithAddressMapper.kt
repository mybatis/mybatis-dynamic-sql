/**
 *    Copyright 2016-2019 the original author or authors.
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
package examples.kotlin

import org.apache.ibatis.annotations.Result
import org.apache.ibatis.annotations.Results
import org.apache.ibatis.annotations.SelectProvider
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter

interface PersonWithAddressMapper {
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    @Results(id = "PersonWithAddressResult", value = [
        Result(column = "id", property = "id"),
        Result(column = "first_name", property = "firstName"),
        Result(column = "last_name", property = "lastName"),
        Result(column = "birth_date", property = "birthDate"),
        Result(column = "employed", property = "employed", typeHandler = YesNoTypeHandler::class),
        Result(column = "occupation", property = "occupation"),
        Result(column = "address_id", property = "address.id"),
        Result(column = "street_address", property = "address.streetAddress"),
        Result(column = "city", property = "address.city"),
        Result(column = "state", property = "address.state")
    ])
    fun selectMany(selectStatement: SelectStatementProvider): List<PersonWithAddress>
}
