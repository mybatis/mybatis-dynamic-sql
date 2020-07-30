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
package examples.custom_render;

import java.sql.JDBCType;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public class JsonTestDynamicSqlSupport {
    public static JsonTest jsonTest = new JsonTest();
    public static SqlColumn<Integer> id = jsonTest.column("id", JDBCType.INTEGER);
    public static SqlColumn<String> description = jsonTest.column("description", JDBCType.VARCHAR);
    public static SqlColumn<String> info = jsonTest.column("info", JDBCType.VARCHAR)
            .withRenderingStrategy(new JsonRenderingStrategy());

    public static class JsonTest extends SqlTable {
        public JsonTest() {
            super("JsonTest");
        }
    }
}
