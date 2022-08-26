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
package examples.type_conversion;

import java.sql.JDBCType;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class MyFilesDynamicSqlSupport {
    public static final MyFiles myfiles = new MyFiles();
    public static final SqlColumn<Integer> fileId = myfiles.fileId;
    public static final SqlColumn<byte[]> fileContents = myfiles.fileContents;

    public static final class MyFiles extends SqlTable {
        public final SqlColumn<Integer> fileId = column("file_id", JDBCType.INTEGER);
        public final SqlColumn<byte[]> fileContents = column("file_contents", JDBCType.LONGVARBINARY);

        public MyFiles() {
            super("MyFiles");
        }
    }
}
