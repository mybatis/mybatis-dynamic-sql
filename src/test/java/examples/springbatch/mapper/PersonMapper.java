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
package examples.springbatch.mapper;

import java.util.List;
import java.util.Map;

import examples.springbatch.common.PersonRecord;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;
import org.mybatis.dynamic.sql.util.mybatis3.CommonCountMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper;
import org.mybatis.dynamic.sql.util.springbatch.SpringBatchProviderAdapter;

@Mapper
public interface PersonMapper extends CommonCountMapper, CommonInsertMapper<PersonRecord>, CommonUpdateMapper {

    @SelectProvider(type=SpringBatchProviderAdapter.class, method="select")
    @Arg(column = "id", javaType = Integer.class, id = true)
    @Arg(column = "first_name", javaType = String.class)
    @Arg(column = "last_name", javaType = String.class)
    List<PersonRecord> selectMany(Map<String, Object> parameterValues);
}
