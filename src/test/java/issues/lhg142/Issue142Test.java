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

import static issues.lhg142.MyMarkDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

class Issue142Test {

    @Test
    void LimitWithSubqueries() {
        Page page = new Page(100L, 10L);
        SelectStatementProvider selectStatement = select(id, updateTime.as("mutime"), createTime.as("mctime")).from(myMark)
                .where(id, isLessThanOrEqualTo(
                        select(id).from(myMark)
                                .orderBy(updateTime.descending(), createTime.descending())
                                .limit(1L)
                                .offset(page.getOffset())
                )).orderBy(sortColumn("mutime").descending(), sortColumn("mctime").descending())
                .limit(page.getSize()).offset(0L).build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select id, update_time as mutime, create_time as mctime from my_mark" +
                " where id <= (select id from my_mark order by update_time DESC, create_time DESC limit #{parameters.p1} offset #{parameters.p2})" +
                " order by mutime DESC, mctime DESC limit #{parameters.p3} offset #{parameters.p4}";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 1L);
        assertThat(selectStatement.getParameters()).containsEntry("p2", 100L);
        assertThat(selectStatement.getParameters()).containsEntry("p3", 10L);
        assertThat(selectStatement.getParameters()).containsEntry("p4", 0L);
    }
}
