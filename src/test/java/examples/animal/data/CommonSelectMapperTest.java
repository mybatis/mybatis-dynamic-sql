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
package examples.animal.data;

import static examples.animal.data.AnimalDataDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper;

class CommonSelectMapperTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/animal/data/CreateAnimalData.sql");
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

    private final Function<Map<String, Object>, AnimalData> rowMapper = map -> {
        AnimalData ad = new AnimalData();
        ad.setId((Integer) map.get("ID"));
        ad.setAnimalName((String) map.get("ANIMAL_NAME"));
        ad.setBodyWeight((Double) map.get("BODY_WEIGHT"));
        ad.setBrainWeight((Double) map.get("BRAIN_WEIGHT"));
        return ad;
    };

    @Test
    void testGeneralSelectOne() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(id.asCamelCase(), animalName.asCamelCase())
                    .from(animalData)
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            Map<String, Object> row = mapper.selectOneMappedRow(selectStatement);

            assertThat(row).containsEntry("id", 1);
            assertThat(row).containsEntry("animalName", "Lesser short-tailed shrew");
        }
    }

    @Test
    void testGeneralSelectOneWithRowMapper() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            AnimalData animal = mapper.selectOne(selectStatement, rowMapper);

            assertThat(animal.getId()).isEqualTo(1);
            assertThat(animal.getAnimalName()).isEqualTo("Lesser short-tailed shrew");
            assertThat(animal.getBodyWeight()).isEqualTo(0.14);
            assertThat(animal.getBrainWeight()).isEqualTo(0.005);
        }
    }

    @Test
    void testGeneralSelectMany() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName)
                    .from(animalData)
                    .where(id, isIn(1, 2))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(2);

            assertThat(rows.get(0)).containsEntry("ID", 1);
            assertThat(rows.get(0)).containsEntry("ANIMAL_NAME", "Lesser short-tailed shrew");
            assertThat(rows.get(1)).containsEntry("ID", 2);
            assertThat(rows.get(1)).containsEntry("ANIMAL_NAME", "Little brown bat");
        }
    }

    @Test
    void testGeneralSelectManyWithRowMapper() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isIn(1, 2))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> rows = mapper.selectMany(selectStatement, rowMapper);

            assertThat(rows).hasSize(2);

            assertThat(rows.get(0).getId()).isEqualTo(1);
            assertThat(rows.get(0).getAnimalName()).isEqualTo("Lesser short-tailed shrew");
            assertThat(rows.get(0).getBodyWeight()).isEqualTo(0.14);
            assertThat(rows.get(0).getBrainWeight()).isEqualTo(0.005);
            assertThat(rows.get(1).getId()).isEqualTo(2);
            assertThat(rows.get(1).getAnimalName()).isEqualTo("Little brown bat");
            assertThat(rows.get(1).getBodyWeight()).isEqualTo(0.25);
            assertThat(rows.get(1).getBrainWeight()).isEqualTo(0.01);
        }
    }

    @Test
    void testSelectOneBigDecimal() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(bodyWeight)
                    .from(animalData)
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            BigDecimal bodyWeight = mapper.selectOneBigDecimal(selectStatement);

            assertThat(bodyWeight).isCloseTo(new BigDecimal("0.14"), Percentage.withPercentage(0.01));
        }
    }

    @Test
    void testSelectOptionalBigDecimalPresent() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(bodyWeight)
                    .from(animalData)
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Optional<BigDecimal> bodyWeight = mapper.selectOptionalBigDecimal(selectStatement);

            assertThat(bodyWeight).hasValueSatisfying(bw -> assertThat(bw).isCloseTo(new BigDecimal("0.14"), Percentage.withPercentage(0.01)));
        }
    }

    @Test
    void testSelectOptionalBigDecimalMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(bodyWeight)
                    .from(animalData)
                    .where(id, isEqualTo(1000))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Optional<BigDecimal> bodyWeight = mapper.selectOptionalBigDecimal(selectStatement);

            assertThat(bodyWeight).isEmpty();
        }
    }

    @Test
    void testSelectManyBigDecimals() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(bodyWeight)
                    .from(animalData)
                    .where(id, isIn(1, 2))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<BigDecimal> bodyWeights = mapper.selectManyBigDecimals(selectStatement);

            assertThat(bodyWeights).hasSize(2);
            assertThat(bodyWeights.get(0)).isCloseTo(new BigDecimal("0.14"), Percentage.withPercentage(0.01));
            assertThat(bodyWeights.get(1)).isCloseTo(new BigDecimal("0.25"), Percentage.withPercentage(0.01));
        }
    }

    @Test
    void testSelectOneDouble() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(bodyWeight)
                    .from(animalData)
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Double bodyWeight = mapper.selectOneDouble(selectStatement);

            assertThat(bodyWeight).isEqualTo(0.14);
        }
    }

    @Test
    void testSelectOptionalDoublePresent() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(bodyWeight)
                    .from(animalData)
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Optional<Double> bodyWeight = mapper.selectOptionalDouble(selectStatement);

            assertThat(bodyWeight).hasValueSatisfying(bw -> assertThat(bw).isEqualTo(0.14));
        }
    }

    @Test
    void testSelectOptionalDoubleMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(bodyWeight)
                    .from(animalData)
                    .where(id, isEqualTo(1000))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Optional<Double> bodyWeight = mapper.selectOptionalDouble(selectStatement);

            assertThat(bodyWeight).isEmpty();
        }
    }

    @Test
    void testSelectManyDoubles() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(bodyWeight)
                    .from(animalData)
                    .where(id, isIn(1, 2))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<Double> bodyWeights = mapper.selectManyDoubles(selectStatement);

            assertThat(bodyWeights).hasSize(2);
            assertThat(bodyWeights.get(0)).isEqualTo(0.14);
            assertThat(bodyWeights.get(1)).isEqualTo(0.25);
        }
    }

    @Test
    void testSelectOneInteger() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(id)
                    .from(animalData)
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Integer id = mapper.selectOneInteger(selectStatement);

            assertThat(id).isEqualTo(1);
        }
    }

    @Test
    void testSelectOptionalIntegerPresent() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(id)
                    .from(animalData)
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Optional<Integer> id = mapper.selectOptionalInteger(selectStatement);

            assertThat(id).hasValueSatisfying(i -> assertThat(i).isEqualTo(1));
        }
    }

    @Test
    void testSelectOptionalIntegerMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(id)
                    .from(animalData)
                    .where(id, isEqualTo(1000))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Optional<Integer> id = mapper.selectOptionalInteger(selectStatement);

            assertThat(id).isEmpty();
        }
    }

    @Test
    void testSelectManyIntegers() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(id)
                    .from(animalData)
                    .where(id, isIn(1, 2))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<Integer> ids = mapper.selectManyIntegers(selectStatement);

            assertThat(ids).hasSize(2);
            assertThat(ids.get(0)).isEqualTo(1);
            assertThat(ids.get(1)).isEqualTo(2);
        }
    }

    @Test
    void testSelectOneLong() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(id)
                    .from(animalData)
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Long id = mapper.selectOneLong(selectStatement);

            assertThat(id).isEqualTo(1);
        }
    }

    @Test
    void testSelectOptionalLongPresent() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(id)
                    .from(animalData)
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Optional<Long> id = mapper.selectOptionalLong(selectStatement);

            assertThat(id).hasValueSatisfying(i -> assertThat(i).isEqualTo(1));
        }
    }

    @Test
    void testSelectOptionalLongMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(id)
                    .from(animalData)
                    .where(id, isEqualTo(1000))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Optional<Long> id = mapper.selectOptionalLong(selectStatement);

            assertThat(id).isEmpty();
        }
    }

    @Test
    void testSelectManyLongs() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(id)
                    .from(animalData)
                    .where(id, isIn(1, 2))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<Long> ids = mapper.selectManyLongs(selectStatement);

            assertThat(ids).hasSize(2);
            assertThat(ids.get(0)).isEqualTo(1);
            assertThat(ids.get(1)).isEqualTo(2);
        }
    }

    @Test
    void testSelectOneString() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(animalName)
                    .from(animalData)
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String name = mapper.selectOneString(selectStatement);

            assertThat(name).isEqualTo("Lesser short-tailed shrew");
        }
    }

    @Test
    void testSelectOptionalStringPresent() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(animalName)
                    .from(animalData)
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Optional<String> name = mapper.selectOptionalString(selectStatement);

            assertThat(name).hasValueSatisfying(n -> assertThat(n).isEqualTo("Lesser short-tailed shrew"));
        }
    }

    @Test
    void testSelectOptionalStringMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(animalName)
                    .from(animalData)
                    .where(id, isEqualTo(1000))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Optional<String> name = mapper.selectOptionalString(selectStatement);

            assertThat(name).isEmpty();
        }
    }

    @Test
    void testSelectManyStrings() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(animalName)
                    .from(animalData)
                    .where(id, isIn(1, 2))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<String> names = mapper.selectManyStrings(selectStatement);

            assertThat(names).hasSize(2);
            assertThat(names.get(0)).isEqualTo("Lesser short-tailed shrew");
            assertThat(names.get(1)).isEqualTo("Little brown bat");
        }
    }
}
