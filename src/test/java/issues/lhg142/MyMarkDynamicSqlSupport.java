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
package issues.lhg142;

import java.time.LocalDateTime;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class MyMarkDynamicSqlSupport {
    public static final MyMark myMark = new MyMark();
    public static final SqlColumn<Long> id = myMark.id;
    public static final SqlColumn<LocalDateTime> createTime = myMark.createTime;
    public static final SqlColumn<LocalDateTime> updateTime = myMark.updateTime;

    public static final class MyMark extends SqlTable {
        public final SqlColumn<Long> id = column("id");
        public final SqlColumn<LocalDateTime> createTime = column("create_time");
        public final SqlColumn<LocalDateTime> updateTime = column("update_time");

        public MyMark() {
            super("my_mark");
        }
    }
}
