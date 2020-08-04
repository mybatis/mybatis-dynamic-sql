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
package examples.kotlin.spring.canonical

import java.util.*

data class LastName(val name: String)

val lastNameConverter: (LastName?) -> String? = { it?.name }

val booleanToStringConverter: (Boolean?) -> String = { it?.let { if (it) "Yes" else "No" } ?: "No" }

data class PersonRecord(
    var id: Int? = null,
    var firstName: String? = null,
    var lastName: LastName? = null,
    var birthDate: Date? = null,
    var employed: Boolean? = null,
    var occupation: String? = null,
    var addressId: Int? = null
) {
    val lastNameAsString: String?
        get() = lastNameConverter(lastName)

    val employedAsString: String
        get() = booleanToStringConverter(employed)
}

data class PersonWithAddress(
    var id: Int? = null,
    var firstName: String? = null,
    var lastName: LastName? = null,
    var birthDate: Date? = null,
    var employed: Boolean? = null,
    var occupation: String? = null,
    var address: AddressRecord? = null
)

data class AddressRecord(
    var id: Int? = null,
    var streetAddress: String? = null,
    var city: String? = null,
    var state: String? = null
)

data class GeneratedAlwaysRecord(
    var id: Int? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var fullName: String? = null
)
