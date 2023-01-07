/*
 *    Copyright 2016-2023 the original author or authors.
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
package examples.kotlin.spring.canonical

import java.sql.ResultSet

val personRowMapper: (ResultSet, Int) -> PersonRecord = { rs, _ ->
    PersonRecord(
        id = rs.getInt(1),
        firstName = rs.getString(2),
        lastName = rs.getString(3)?.let { LastName(it) },
        birthDate = rs.getTimestamp(4),
        employed = "Yes" == rs.getString(5),
        occupation = rs.getString(6),
        addressId = rs.getIntOrNull(7)
    )
}

private fun ResultSet.getIntOrNull(index: Int): Int? {
    getInt(index).let {
        return if (wasNull()) {
            null
        } else {
            it
        }
    }
}

val personWithAddressRowMapper: (ResultSet, Int) -> PersonWithAddress = { rs, _ ->
    PersonWithAddress(
        id = rs.getInt(1),
        firstName = rs.getString(2),
        lastName = rs.getString(3)?.let { LastName(it) },
        birthDate = rs.getTimestamp(4),
        employed = "Yes" == rs.getString(5),
        occupation = rs.getString(6),
        address = AddressRecord(
            id = rs.getInt(7),
            streetAddress = rs.getString(8),
            city = rs.getString(9),
            state = rs.getString(10),
        )
    )
}
