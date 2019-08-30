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
package examples.kotlin.canonical

import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.TypeHandler
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet

class LastNameTypeHandler : TypeHandler<LastName> {

    override fun setParameter(ps: PreparedStatement, i: Int, parameter: LastName, jdbcType: JdbcType) =
        ps.setString(i, parameter.name)

    override fun getResult(rs: ResultSet, columnName: String) =
        toLastName(rs.getString(columnName))

    override fun getResult(rs: ResultSet, columnIndex: Int) =
        toLastName(rs.getString(columnIndex))

    override fun getResult(cs: CallableStatement, columnIndex: Int) =
        toLastName(cs.getString(columnIndex))

    private fun toLastName(s: String?) =
        if (s == null) null else LastName(s)
}
