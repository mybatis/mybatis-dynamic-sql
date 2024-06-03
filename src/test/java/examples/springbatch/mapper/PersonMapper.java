/*
 *    Copyright 2016-2024 the original author or authors.
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

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.mybatis.dynamic.sql.util.mybatis3.CommonCountMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper;
import org.mybatis.dynamic.sql.util.springbatch.SpringBatchProviderAdapter;

import examples.springbatch.common.PersonRecord;

@Mapper
public interface PersonMapper extends CommonCountMapper, CommonInsertMapper<PersonRecord>, CommonUpdateMapper {

    @SelectProvider(type=SpringBatchProviderAdapter.class, method="select")
    @Results({
        @Result(column="id", property="id", id=true),
        @Result(column="first_name", property="firstName"),
        @Result(column="last_name", property="lastName")
    })
    List<PersonRecord> selectMany(Map<String, Object> parameterValues);
}
