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
package examples.springbatch;

import static examples.springbatch.mapper.PersonDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.isLike;
import static org.mybatis.dynamic.sql.SqlBuilder.select;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.util.springbatch.SpringBatchUtility;

class SpringBatchRenderingTest {

    @Test
    void renderLimit() {
        var selectStatement = select(person.allColumns())
                .from(person)
                .where(firstName, isLike("%f%"))
                .limit(SpringBatchUtility.MYBATIS_SPRING_BATCH_PAGESIZE)
                .offset(SpringBatchUtility.MYBATIS_SPRING_BATCH_SKIPROWS)
                .build()
                .render(SpringBatchUtility.SPRING_BATCH_PAGING_ITEM_READER_RENDERING_STRATEGY);

        assertThat(selectStatement.getSelectStatement())
                .isEqualTo("""
                     select * \
                     from person \
                     where first_name like #{parameters.p1,jdbcType=VARCHAR} \
                     limit #{_pagesize} \
                     offset #{_skiprows}""");
    }

    @Test
    void renderFetchFirst() {
        var selectStatement = select(person.allColumns())
                .from(person)
                .where(firstName, isLike("%f%"))
                .offset(SpringBatchUtility.MYBATIS_SPRING_BATCH_SKIPROWS)
                .fetchFirst(SpringBatchUtility.MYBATIS_SPRING_BATCH_PAGESIZE).rowsOnly()
                .build()
                .render(SpringBatchUtility.SPRING_BATCH_PAGING_ITEM_READER_RENDERING_STRATEGY);

        assertThat(selectStatement.getSelectStatement())
                .isEqualTo("""
                     select * \
                     from person \
                     where first_name like #{parameters.p1,jdbcType=VARCHAR} \
                     offset #{_skiprows} rows \
                     fetch first #{_pagesize} rows only""");
    }
}
