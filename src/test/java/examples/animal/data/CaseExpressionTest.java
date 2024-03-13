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
package examples.animal.data;

import static examples.animal.data.AnimalDataDynamicSqlSupport.animalData;
import static examples.animal.data.AnimalDataDynamicSqlSupport.animalName;
import static examples.animal.data.AnimalDataDynamicSqlSupport.brainWeight;
import static examples.animal.data.AnimalDataDynamicSqlSupport.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.entry;
import static org.mybatis.dynamic.sql.SqlBuilder.and;
import static org.mybatis.dynamic.sql.SqlBuilder.group;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualToWhenPresent;
import static org.mybatis.dynamic.sql.SqlBuilder.isIn;
import static org.mybatis.dynamic.sql.SqlBuilder.isLessThan;
import static org.mybatis.dynamic.sql.SqlBuilder.or;
import static org.mybatis.dynamic.sql.SqlBuilder.searchedCase;
import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.mybatis.dynamic.sql.SqlBuilder.simpleCase;

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
import org.mybatis.dynamic.sql.exception.InvalidSqlException;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.Messages;
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper;

class CaseExpressionTest {
    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/animal/data/CreateAnimalData.sql");
        assert is != null;
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }

        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(CommonSelectMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testSearchedCase() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(animalName, searchedCase()
                    .when(animalName, isEqualTo("Artic fox")).or(animalName, isEqualTo("Red fox")).then("'Fox'")
                    .when(animalName, isEqualTo("Little brown bat")).or(animalName, isEqualTo("Big brown bat")).then("'Bat'")
                    .else_("cast('Not a Fox or a bat' as varchar(25))").end().as("AnimalType"))
                    .from(animalData, "a")
                    .where(id, isIn(2, 3, 31, 32, 38, 39))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.animal_name, case " +
                    "when a.animal_name = #{parameters.p1,jdbcType=VARCHAR} or a.animal_name = #{parameters.p2,jdbcType=VARCHAR} then 'Fox' " +
                    "when a.animal_name = #{parameters.p3,jdbcType=VARCHAR} or a.animal_name = #{parameters.p4,jdbcType=VARCHAR} then 'Bat' " +
                    "else cast('Not a Fox or a bat' as varchar(25)) end as AnimalType " +
                    "from AnimalData a where a.id in (" +
                    "#{parameters.p5,jdbcType=INTEGER},#{parameters.p6,jdbcType=INTEGER}," +
                    "#{parameters.p7,jdbcType=INTEGER},#{parameters.p8,jdbcType=INTEGER},#{parameters.p9,jdbcType=INTEGER}," +
                    "#{parameters.p10,jdbcType=INTEGER}) " +
                    "order by id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            assertThat(selectStatement.getParameters()).containsOnly(
                    entry("p1", "Artic fox"),
                    entry("p2", "Red fox"),
                    entry("p3", "Little brown bat"),
                    entry("p4", "Big brown bat"),
                    entry("p5", 2),
                    entry("p6", 3),
                    entry("p7", 31),
                    entry("p8", 32),
                    entry("p9", 38),
                    entry("p10", 39)
            );

            List<Map<String, Object>> records = mapper.selectManyMappedRows(selectStatement);
            assertThat(records).hasSize(6);
            assertThat(records.get(0)).containsOnly(entry("ANIMAL_NAME", "Little brown bat"), entry("ANIMALTYPE", "Bat"));
            assertThat(records.get(1)).containsOnly(entry("ANIMAL_NAME", "Big brown bat"), entry("ANIMALTYPE", "Bat"));
            assertThat(records.get(2)).containsOnly(entry("ANIMAL_NAME", "Cat"), entry("ANIMALTYPE", "Not a Fox or a bat"));
            assertThat(records.get(3)).containsOnly(entry("ANIMAL_NAME", "Artic fox"), entry("ANIMALTYPE", "Fox"));
            assertThat(records.get(4)).containsOnly(entry("ANIMAL_NAME", "Red fox"), entry("ANIMALTYPE", "Fox"));
            assertThat(records.get(5)).containsOnly(entry("ANIMAL_NAME", "Raccoon"), entry("ANIMALTYPE", "Not a Fox or a bat"));
        }
    }

    @Test
    void testSearchedCaseNoElse() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(animalName, searchedCase()
                    .when(animalName, isEqualTo("Artic fox")).or(animalName, isEqualTo("Red fox")).then("'Fox'")
                    .when(animalName, isEqualTo("Little brown bat")).or(animalName, isEqualTo("Big brown bat")).then("'Bat'")
                    .end().as("AnimalType"))
                    .from(animalData, "a")
                    .where(id, isIn(2, 3, 31, 32, 38, 39))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.animal_name, case " +
                    "when a.animal_name = #{parameters.p1,jdbcType=VARCHAR} or a.animal_name = #{parameters.p2,jdbcType=VARCHAR} then 'Fox' " +
                    "when a.animal_name = #{parameters.p3,jdbcType=VARCHAR} or a.animal_name = #{parameters.p4,jdbcType=VARCHAR} then 'Bat' " +
                    "end as AnimalType " +
                    "from AnimalData a where a.id in (" +
                    "#{parameters.p5,jdbcType=INTEGER},#{parameters.p6,jdbcType=INTEGER}," +
                    "#{parameters.p7,jdbcType=INTEGER},#{parameters.p8,jdbcType=INTEGER},#{parameters.p9,jdbcType=INTEGER}," +
                    "#{parameters.p10,jdbcType=INTEGER}) " +
                    "order by id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            assertThat(selectStatement.getParameters()).containsOnly(
                    entry("p1", "Artic fox"),
                    entry("p2", "Red fox"),
                    entry("p3", "Little brown bat"),
                    entry("p4", "Big brown bat"),
                    entry("p5", 2),
                    entry("p6", 3),
                    entry("p7", 31),
                    entry("p8", 32),
                    entry("p9", 38),
                    entry("p10", 39)
            );

            List<Map<String, Object>> records = mapper.selectManyMappedRows(selectStatement);
            assertThat(records).hasSize(6);
            assertThat(records.get(0)).containsOnly(entry("ANIMAL_NAME", "Little brown bat"), entry("ANIMALTYPE", "Bat"));
            assertThat(records.get(1)).containsOnly(entry("ANIMAL_NAME", "Big brown bat"), entry("ANIMALTYPE", "Bat"));
            assertThat(records.get(2)).containsOnly(entry("ANIMAL_NAME", "Cat"));
            assertThat(records.get(3)).containsOnly(entry("ANIMAL_NAME", "Artic fox"), entry("ANIMALTYPE", "Fox"));
            assertThat(records.get(4)).containsOnly(entry("ANIMAL_NAME", "Red fox"), entry("ANIMALTYPE", "Fox"));
            assertThat(records.get(5)).containsOnly(entry("ANIMAL_NAME", "Raccoon"));
        }
    }

    @Test
    void testSearchedCaseWithGroup() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(animalName, searchedCase()
                    .when(animalName, isEqualTo("Artic fox")).or(animalName, isEqualTo("Red fox")).then("'Fox'")
                    .when(animalName, isEqualTo("Little brown bat")).or(animalName, isEqualTo("Big brown bat")).then("'Bat'")
                    .when(group(animalName, isEqualTo("Cat"), and(id, isEqualTo(31))), or(id, isEqualTo(39))).then("'Fred'")
                    .else_("cast('Not a Fox or a bat' as varchar(25))").end().as("AnimalType"))
                    .from(animalData, "a")
                    .where(id, isIn(2, 3, 4, 31, 32, 38, 39))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.animal_name, case " +
                    "when a.animal_name = #{parameters.p1,jdbcType=VARCHAR} or a.animal_name = #{parameters.p2,jdbcType=VARCHAR} then 'Fox' " +
                    "when a.animal_name = #{parameters.p3,jdbcType=VARCHAR} or a.animal_name = #{parameters.p4,jdbcType=VARCHAR} then 'Bat' " +
                    "when (a.animal_name = #{parameters.p5,jdbcType=VARCHAR} and a.id = #{parameters.p6,jdbcType=INTEGER}) or a.id = #{parameters.p7,jdbcType=INTEGER} then 'Fred' " +
                    "else cast('Not a Fox or a bat' as varchar(25)) end as AnimalType " +
                    "from AnimalData a where a.id in (" +
                    "#{parameters.p8,jdbcType=INTEGER},#{parameters.p9,jdbcType=INTEGER}," +
                    "#{parameters.p10,jdbcType=INTEGER},#{parameters.p11,jdbcType=INTEGER},#{parameters.p12,jdbcType=INTEGER}," +
                    "#{parameters.p13,jdbcType=INTEGER},#{parameters.p14,jdbcType=INTEGER}) " +
                    "order by id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            assertThat(selectStatement.getParameters()).containsOnly(
                    entry("p1", "Artic fox"),
                    entry("p2", "Red fox"),
                    entry("p3", "Little brown bat"),
                    entry("p4", "Big brown bat"),
                    entry("p5", "Cat"),
                    entry("p6", 31),
                    entry("p7", 39),
                    entry("p8", 2),
                    entry("p9", 3),
                    entry("p10", 4),
                    entry("p11", 31),
                    entry("p12", 32),
                    entry("p13", 38),
                    entry("p14", 39)
            );

            List<Map<String, Object>> records = mapper.selectManyMappedRows(selectStatement);
            assertThat(records).hasSize(7);
            assertThat(records.get(0)).containsOnly(entry("ANIMAL_NAME", "Little brown bat"), entry("ANIMALTYPE", "Bat"));
            assertThat(records.get(1)).containsOnly(entry("ANIMAL_NAME", "Big brown bat"), entry("ANIMALTYPE", "Bat"));
            assertThat(records.get(2)).containsOnly(entry("ANIMAL_NAME", "Mouse"), entry("ANIMALTYPE", "Not a Fox or a bat"));
            assertThat(records.get(3)).containsOnly(entry("ANIMAL_NAME", "Cat"), entry("ANIMALTYPE", "Fred"));
            assertThat(records.get(4)).containsOnly(entry("ANIMAL_NAME", "Artic fox"), entry("ANIMALTYPE", "Fox"));
            assertThat(records.get(5)).containsOnly(entry("ANIMAL_NAME", "Red fox"), entry("ANIMALTYPE", "Fox"));
            assertThat(records.get(6)).containsOnly(entry("ANIMAL_NAME", "Raccoon"), entry("ANIMALTYPE", "Fred"));
        }
    }

    @Test
    void testSimpleCassLessThan() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(animalName, simpleCase(brainWeight)
                    .when(isLessThan(4.0)).then("'small brain'")
                    .else_("'large brain'").end().as("brain_size"))
                    .from(animalData)
                    .where(id, isIn(31, 32, 38, 39))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select animal_name, case brain_weight " +
                    "when < #{parameters.p1,jdbcType=DOUBLE} then 'small brain' " +
                    "else 'large brain' end as brain_size " +
                    "from AnimalData where id in (" +
                    "#{parameters.p2,jdbcType=INTEGER},#{parameters.p3,jdbcType=INTEGER}," +
                    "#{parameters.p4,jdbcType=INTEGER},#{parameters.p5,jdbcType=INTEGER}) " +
                    "order by id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            List<Map<String, Object>> records = mapper.selectManyMappedRows(selectStatement);
            assertThat(records).hasSize(7);
        }
    }

    @Test
    void testSimpleCase() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(animalName, simpleCase(animalName)
                    .when(isEqualTo("Artic fox"), isEqualTo("Red fox")).then("'yes'")
                    .else_("cast('no' as VARCHAR(3))").end().as("IsAFox"))
                    .from(animalData)
                    .where(id, isIn(31, 32, 38, 39))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select animal_name, " +
                    "case animal_name when = #{parameters.p1,jdbcType=VARCHAR}, = #{parameters.p2,jdbcType=VARCHAR} then 'yes' else cast('no' as VARCHAR(3)) end " +
                    "as IsAFox from AnimalData where id in " +
                    "(#{parameters.p3,jdbcType=INTEGER},#{parameters.p4,jdbcType=INTEGER},#{parameters.p5,jdbcType=INTEGER},#{parameters.p6,jdbcType=INTEGER}) " +
                    "order by id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            assertThat(selectStatement.getParameters()).containsOnly(
                    entry("p1", "Artic fox"),
                    entry("p2", "Red fox"),
                    entry("p3", 31),
                    entry("p4", 32),
                    entry("p5", 38),
                    entry("p6", 39)
            );

            List<Map<String, Object>> records = mapper.selectManyMappedRows(selectStatement);
            assertThat(records).hasSize(4);
            assertThat(records.get(0)).containsOnly(entry("ANIMAL_NAME", "Cat"), entry("ISAFOX", "no"));
            assertThat(records.get(1)).containsOnly(entry("ANIMAL_NAME", "Artic fox"), entry("ISAFOX", "yes"));
            assertThat(records.get(2)).containsOnly(entry("ANIMAL_NAME", "Red fox"), entry("ISAFOX", "yes"));
            assertThat(records.get(3)).containsOnly(entry("ANIMAL_NAME", "Raccoon"), entry("ISAFOX", "no"));
        }
    }

    @Test
    void testSimpleCaseNoElse() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(animalName, simpleCase(animalName)
                    .when(isEqualTo("Artic fox"), isEqualTo("Red fox")).then("'yes'")
                    .end().as("IsAFox"))
                    .from(animalData)
                    .where(id, isIn(31, 32, 38, 39))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select animal_name, " +
                    "case animal_name when = #{parameters.p1,jdbcType=VARCHAR}, = #{parameters.p2,jdbcType=VARCHAR} then 'yes' end " +
                    "as IsAFox from AnimalData where id in " +
                    "(#{parameters.p3,jdbcType=INTEGER},#{parameters.p4,jdbcType=INTEGER},#{parameters.p5,jdbcType=INTEGER},#{parameters.p6,jdbcType=INTEGER}) " +
                    "order by id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            assertThat(selectStatement.getParameters()).containsOnly(
                    entry("p1", "Artic fox"),
                    entry("p2", "Red fox"),
                    entry("p3", 31),
                    entry("p4", 32),
                    entry("p5", 38),
                    entry("p6", 39)
            );

            List<Map<String, Object>> records = mapper.selectManyMappedRows(selectStatement);
            assertThat(records).hasSize(4);
            assertThat(records.get(0)).containsOnly(entry("ANIMAL_NAME", "Cat"));
            assertThat(records.get(1)).containsOnly(entry("ANIMAL_NAME", "Artic fox"), entry("ISAFOX", "yes"));
            assertThat(records.get(2)).containsOnly(entry("ANIMAL_NAME", "Red fox"), entry("ISAFOX", "yes"));
            assertThat(records.get(3)).containsOnly(entry("ANIMAL_NAME", "Raccoon"));
        }
    }

    @Test
    void testInvalidSearchedCaseNoConditionsRender() {
        SelectModel model = select(animalName, searchedCase()
                .when(animalName, isEqualToWhenPresent((String) null)).then("Fred").end())
                .from(animalData)
                .build();

        assertThatExceptionOfType(InvalidSqlException.class)
                .isThrownBy(() -> model.render(RenderingStrategies.MYBATIS3))
                .withMessage(Messages.getString("ERROR.39"));
    }

    @Test
    void testInvalidSimpleCaseNoConditionsRender() {
        SelectModel model = select(simpleCase(animalName)
                .when(isEqualToWhenPresent((String) null)).then("Fred").end())
                .from(animalData)
                .build();

        assertThatExceptionOfType(InvalidSqlException.class)
                .isThrownBy(() -> model.render(RenderingStrategies.MYBATIS3))
                .withMessage(Messages.getString("ERROR.39"));
    }

    @Test
    void testInvalidSearchedCaseNoWhenConditions() {
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(
                () -> searchedCase().end()
        ).withMessage(Messages.getString("ERROR.40"));
    }

    @Test
    void testInvalidSimpleCaseNoWhenConditions() {
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(
                () -> simpleCase(id).end()
        ).withMessage(Messages.getString("ERROR.40"));
    }
}
