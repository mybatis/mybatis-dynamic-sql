/*
 *    Copyright 2016-2025 the original author or authors.
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
package examples.custom_render;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.SelectProvider;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper;

public interface JsonTestMapper extends CommonDeleteMapper, CommonInsertMapper<JsonTestRecord>, CommonSelectMapper,
        CommonUpdateMapper {
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    @Arg(column = "id", javaType = int.class, id = true)
    @Arg(column = "description", javaType = String.class)
    @Arg(column = "info", javaType = String.class)
    List<JsonTestRecord> selectMany(SelectStatementProvider selectStatement);

    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    @Arg(column = "id", javaType = int.class, id = true)
    @Arg(column = "description", javaType = String.class)
    @Arg(column = "info", javaType = String.class)
    Optional<JsonTestRecord> selectOne(SelectStatementProvider selectStatement);
}
