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
package org.mybatis.dynamic.sql.subselect;

import java.sql.JDBCType;
import java.util.Date;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public class FooDynamicSqlSupport {
    public static final Foo foo = new Foo();
    public static final SqlColumn<Date> column1 = foo.column1;
    public static final SqlColumn<Integer> column2 = foo.column2;

    public static class Foo extends SqlTable {
        public final SqlColumn<Date> column1 = column("column1", JDBCType.DATE);
        public final SqlColumn<Integer> column2 = column("column2", JDBCType.INTEGER);

        public Foo() {
            super("foo");
        }
    }
}
