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
package examples.kotlin.spring.canonical

import java.util.Date

data class LastName(val name: String)

val lastNameConverter: (LastName?) -> String? = { it?.name }

val booleanToStringConverter: (Boolean?) -> String = { it?.let { if (it) "Yes" else "No" } ?: "No" }

data class PersonRecord(
    var id: Int,
    var firstName: String?,
    var lastName: LastName?,
    var birthDate: Date?,
    var employed: Boolean?,
    var occupation: String?,
    var addressId: Int?
) {
    val lastNameAsString: String?
        get() = lastNameConverter(lastName)

    val employedAsString: String
        get() = booleanToStringConverter(employed)
}

data class PersonWithAddress(
    var id: Int,
    var firstName: String?,
    var lastName: LastName?,
    var birthDate: Date?,
    var employed: Boolean?,
    var occupation: String?,
    var address: AddressRecord?
)

data class AddressRecord(
    var id: Int,
    var streetAddress: String?,
    var city: String?,
    var state: String?
)

data class GeneratedAlwaysCommand(
    var firstName: String?,
    var lastName: String?
)

data class GeneratedAlwaysRecord(
    var id: Int,
    var firstName: String?,
    var lastName: String?,
    var fullName: String?
)
