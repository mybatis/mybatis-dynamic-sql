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
package examples.joins;

import static examples.joins.ItemMasterDynamicSQLSupport.itemMaster;
import static examples.joins.OrderLineDynamicSQLSupport.orderLine;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper;

class ExistsTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/joins/CreateJoinDB.sql");
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }

        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(JoinMapper.class);
        config.addMapper(CommonDeleteMapper.class);
        config.addMapper(CommonSelectMapper.class);
        config.addMapper(CommonUpdateMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testExists() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(itemMaster.allColumns())
                    .from(itemMaster, "im")
                    .where(exists(
                            select(orderLine.allColumns())
                                    .from(orderLine, "ol")
                                    .where(orderLine.itemId, isEqualTo(itemMaster.itemId.qualifiedWith("im")))
                    ))
                    .orderBy(itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select im.* from ItemMaster im"
                    + " where exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)"
                    + " order by item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(3);
            Map<String, Object> row = rows.get(0);
            assertThat(row).containsEntry("ITEM_ID", 22);
            assertThat(row).containsEntry("DESCRIPTION", "Helmet");

            row = rows.get(1);
            assertThat(row).containsEntry("ITEM_ID", 33);
            assertThat(row).containsEntry("DESCRIPTION", "First Base Glove");

            row = rows.get(2);
            assertThat(row).containsEntry("ITEM_ID", 44);
            assertThat(row).containsEntry("DESCRIPTION", "Outfield Glove");
        }
    }

    @Test
    void testNotExists() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(itemMaster.allColumns())
                    .from(itemMaster, "im")
                    .where(notExists(
                            select(orderLine.allColumns())
                                    .from(orderLine, "ol")
                                    .where(orderLine.itemId, isEqualTo(itemMaster.itemId.qualifiedWith("im")))
                    ))
                    .orderBy(itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select im.* from ItemMaster im"
                    + " where not exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)"
                    + " order by item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(1);
            Map<String, Object> row = rows.get(0);
            assertThat(row).containsEntry("ITEM_ID", 55);
            assertThat(row).containsEntry("DESCRIPTION", "Catcher Glove");
        }
    }

    @Test
    void testNotExistsWithNotCriterion() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(itemMaster.allColumns())
                    .from(itemMaster, "im")
                    .where(not(exists(
                            select(orderLine.allColumns())
                                    .from(orderLine, "ol")
                                    .where(orderLine.itemId, isEqualTo(itemMaster.itemId.qualifiedWith("im")))
                    )))
                    .orderBy(itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select im.* from ItemMaster im"
                    + " where not exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)"
                    + " order by item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(1);
            Map<String, Object> row = rows.get(0);
            assertThat(row).containsEntry("ITEM_ID", 55);
            assertThat(row).containsEntry("DESCRIPTION", "Catcher Glove");
        }
    }

    @Test
    void testAndExists() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(itemMaster.allColumns())
                    .from(itemMaster, "im")
                    .where(itemMaster.itemId, isEqualTo(22))
                    .and(exists(
                            select(orderLine.allColumns())
                                    .from(orderLine, "ol")
                                    .where(orderLine.itemId, isEqualTo(itemMaster.itemId.qualifiedWith("im")))
                    ))
                    .orderBy(itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select im.* from ItemMaster im"
                    + " where im.item_id = #{parameters.p1,jdbcType=INTEGER}"
                    + " and exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)"
                    + " order by item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(1);
            Map<String, Object> row = rows.get(0);
            assertThat(row).containsEntry("ITEM_ID", 22);
            assertThat(row).containsEntry("DESCRIPTION", "Helmet");
        }
    }

    @Test
    void testAndExistsAnd() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(itemMaster.allColumns())
                    .from(itemMaster, "im")
                    .where(itemMaster.itemId, isEqualTo(22))
                    .and(exists(
                            select(orderLine.allColumns())
                                    .from(orderLine, "ol")
                                    .where(orderLine.itemId, isEqualTo(itemMaster.itemId.qualifiedWith("im")))
                    ), and(itemMaster.itemId, isGreaterThan(2)))
                    .orderBy(itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select im.* from ItemMaster im"
                    + " where im.item_id = #{parameters.p1,jdbcType=INTEGER}"
                    + " and (exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)"
                    + " and im.item_id > #{parameters.p2,jdbcType=INTEGER})"
                    + " order by item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(1);
            Map<String, Object> row = rows.get(0);
            assertThat(row).containsEntry("ITEM_ID", 22);
            assertThat(row).containsEntry("DESCRIPTION", "Helmet");
        }
    }

    @Test
    void testOrExists() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(itemMaster.allColumns())
                    .from(itemMaster, "im")
                    .where(itemMaster.itemId, isEqualTo(22))
                    .or(exists(
                            select(orderLine.allColumns())
                                    .from(orderLine, "ol")
                                    .where(orderLine.itemId, isEqualTo(itemMaster.itemId.qualifiedWith("im")))
                    ))
                    .orderBy(itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select im.* from ItemMaster im"
                    + " where im.item_id = #{parameters.p1,jdbcType=INTEGER}"
                    + " or exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)"
                    + " order by item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(3);
            Map<String, Object> row = rows.get(0);
            assertThat(row).containsEntry("ITEM_ID", 22);
            assertThat(row).containsEntry("DESCRIPTION", "Helmet");

            row = rows.get(1);
            assertThat(row).containsEntry("ITEM_ID", 33);
            assertThat(row).containsEntry("DESCRIPTION", "First Base Glove");

            row = rows.get(2);
            assertThat(row).containsEntry("ITEM_ID", 44);
            assertThat(row).containsEntry("DESCRIPTION", "Outfield Glove");
        }
    }

    @Test
    void testOrExistsAnd() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(itemMaster.allColumns())
                    .from(itemMaster, "im")
                    .where(itemMaster.itemId, isEqualTo(22))
                    .or(exists(
                            select(orderLine.allColumns())
                                    .from(orderLine, "ol")
                                    .where(orderLine.itemId, isEqualTo(itemMaster.itemId.qualifiedWith("im")))
                    ), and(itemMaster.itemId, isGreaterThan(2)))
                    .orderBy(itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select im.* from ItemMaster im"
                    + " where im.item_id = #{parameters.p1,jdbcType=INTEGER}"
                    + " or (exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)"
                    + " and im.item_id > #{parameters.p2,jdbcType=INTEGER})"
                    + " order by item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(3);
            Map<String, Object> row = rows.get(0);
            assertThat(row).containsEntry("ITEM_ID", 22);
            assertThat(row).containsEntry("DESCRIPTION", "Helmet");

            row = rows.get(1);
            assertThat(row).containsEntry("ITEM_ID", 33);
            assertThat(row).containsEntry("DESCRIPTION", "First Base Glove");

            row = rows.get(2);
            assertThat(row).containsEntry("ITEM_ID", 44);
            assertThat(row).containsEntry("DESCRIPTION", "Outfield Glove");
        }
    }

    @Test
    void testWhereExistsOr() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(itemMaster.allColumns())
                    .from(itemMaster, "im")
                    .where(exists(
                            select(orderLine.allColumns())
                                    .from(orderLine, "ol")
                                    .where(orderLine.itemId, isEqualTo(itemMaster.itemId.qualifiedWith("im")))
                    ), or(itemMaster.itemId, isEqualTo(22)))
                    .orderBy(itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select im.* from ItemMaster im"
                    + " where (exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)"
                    + " or im.item_id = #{parameters.p1,jdbcType=INTEGER})"
                    + " order by item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(3);
            Map<String, Object> row = rows.get(0);
            assertThat(row).containsEntry("ITEM_ID", 22);
            assertThat(row).containsEntry("DESCRIPTION", "Helmet");

            row = rows.get(1);
            assertThat(row).containsEntry("ITEM_ID", 33);
            assertThat(row).containsEntry("DESCRIPTION", "First Base Glove");

            row = rows.get(2);
            assertThat(row).containsEntry("ITEM_ID", 44);
            assertThat(row).containsEntry("DESCRIPTION", "Outfield Glove");
        }
    }

    @Test
    void testWhereExistsAnd() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(itemMaster.allColumns())
                    .from(itemMaster, "im")
                    .where(exists(
                            select(orderLine.allColumns())
                                    .from(orderLine, "ol")
                                    .where(orderLine.itemId, isEqualTo(itemMaster.itemId.qualifiedWith("im")))
                    ), and(itemMaster.itemId, isEqualTo(22)))
                    .orderBy(itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select im.* from ItemMaster im"
                    + " where (exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)"
                    + " and im.item_id = #{parameters.p1,jdbcType=INTEGER})"
                    + " order by item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(1);
            Map<String, Object> row = rows.get(0);
            assertThat(row).containsEntry("ITEM_ID", 22);
            assertThat(row).containsEntry("DESCRIPTION", "Helmet");
        }
    }

    @Test
    void testDeleteWithHardAlias() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonDeleteMapper mapper = session.getMapper(CommonDeleteMapper.class);

            ItemMasterDynamicSQLSupport.ItemMaster im = itemMaster.withAlias("im");

            DeleteStatementProvider deleteStatement = deleteFrom(im)
                    .where(notExists(select(orderLine.allColumns())
                            .from(orderLine, "ol")
                            .where(orderLine.itemId, isEqualTo(im.itemId)))
                    )
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "delete from ItemMaster im where not exists "
                    + "(select ol.* from OrderLine ol where ol.item_id = im.item_id)";
            assertThat(deleteStatement.getDeleteStatement()).isEqualTo(expectedStatement);

            int rows = mapper.delete(deleteStatement);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    void testDeleteWithSoftAlias() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonDeleteMapper mapper = session.getMapper(CommonDeleteMapper.class);

            DeleteStatementProvider deleteStatement = deleteFrom(itemMaster, "im")
                    .where(notExists(select(orderLine.allColumns())
                            .from(orderLine, "ol")
                            .where(orderLine.itemId, isEqualTo(itemMaster.itemId)))
                    )
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "delete from ItemMaster im where not exists "
                    + "(select ol.* from OrderLine ol where ol.item_id = im.item_id)";
            assertThat(deleteStatement.getDeleteStatement()).isEqualTo(expectedStatement);

            int rows = mapper.delete(deleteStatement);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    void testUpdateWithHardAlias() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonUpdateMapper mapper = session.getMapper(CommonUpdateMapper.class);

            ItemMasterDynamicSQLSupport.ItemMaster im = itemMaster.withAlias("im");

            UpdateStatementProvider updateStatement = update(im)
                    .set(im.description).equalTo("No Orders")
                    .where(notExists(select(orderLine.allColumns())
                            .from(orderLine, "ol")
                            .where(orderLine.itemId, isEqualTo(im.itemId)))
                    )
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "update ItemMaster im "
                    + "set im.description = #{parameters.p1,jdbcType=VARCHAR} "
                    + "where not exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)";
            assertThat(updateStatement.getUpdateStatement()).isEqualTo(expectedStatement);

            int rows = mapper.update(updateStatement);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    void testUpdateWithSoftAlias() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonUpdateMapper mapper = session.getMapper(CommonUpdateMapper.class);

            UpdateStatementProvider updateStatement = update(itemMaster, "im")
                    .set(itemMaster.description).equalTo("No Orders")
                    .where(notExists(select(orderLine.allColumns())
                            .from(orderLine, "ol")
                            .where(orderLine.itemId, isEqualTo(itemMaster.itemId)))
                    )
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "update ItemMaster im "
                    + "set im.description = #{parameters.p1,jdbcType=VARCHAR} "
                    + "where not exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)";
            assertThat(updateStatement.getUpdateStatement()).isEqualTo(expectedStatement);

            int rows = mapper.update(updateStatement);
            assertThat(rows).isEqualTo(1);
        }
    }
}
