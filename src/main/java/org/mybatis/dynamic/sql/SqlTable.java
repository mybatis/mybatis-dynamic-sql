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
package org.mybatis.dynamic.sql;

import java.sql.JDBCType;
import java.util.Objects;

public class SqlTable {

    private String name;

    protected SqlTable(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String name() {
        return name;
    }
    
    public <T> SqlColumn<T> column(String name, JDBCType jdbcType) {
        return new SqlColumn.Builder()
                .withName(name)
                .withJdbcType(jdbcType)
                .withTable(this)
                .build();
    }

    public <T> SqlColumn<T> column(String name, JDBCType jdbcType, String typeHandler) {
        return new SqlColumn.Builder()
                .withName(name)
                .withJdbcType(jdbcType)
                .withTypeHandler(typeHandler)
                .withTable(this)
                .build();
    }
    
    public static SqlTable of(String name) {
        return new SqlTable(name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof SqlTable)) {
            return false;
        }

        SqlTable other = (SqlTable) obj;
        return Objects.equals(name, other.name);
    }
}
