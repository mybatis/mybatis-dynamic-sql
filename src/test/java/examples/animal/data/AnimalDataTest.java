package examples.animal.data;

import static examples.animal.data.AnimalDataFields.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mybatis.qbe.sql.SqlConditions.*;
import static org.mybatis.qbe.sql.render.RenderingShortcut.where;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.qbe.sql.render.RenderedWhereClause;

public class AnimalDataTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver"; 
    
    private SqlSessionFactory sqlSessionFactory;
    
    @Before
    public void setup() throws Exception {
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
        config.addMapper(AnimalDataMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }
    
    @Test
    public void testSelectAllRows() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> animals = mapper.selectByExample(null);
            assertThat(animals.size(), is(65));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testSelectRowsLessThan20() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            RenderedWhereClause renderedWhereClause = where(id, isLessThan(20)).render();
            
            List<AnimalData> animals = mapper.selectByExample(renderedWhereClause);
            assertThat(animals.size(), is(19));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testSelectRowsBetween30And40() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            RenderedWhereClause renderedWhereClause = where(id, isBetween(30).and(40)).render();

            List<AnimalData> animals = mapper.selectByExample(renderedWhereClause);
            assertThat(animals.size(), is(11));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testSelectRowsNotBetween() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            RenderedWhereClause renderedWhereClause = where(id, isNotBetween(10).and(60)).render();

            List<AnimalData> animals = mapper.selectByExample(renderedWhereClause);
            assertThat(animals.size(), is(14));
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testIsEqualCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            RenderedWhereClause renderedWhereClause = where(id, isEqualTo(5)).render();

            List<AnimalData> animals = mapper.selectByExample(renderedWhereClause);
            assertThat(animals.size(), is(1));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testIsNotEqualCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            RenderedWhereClause renderedWhereClause = where(id, isNotEqualTo(5)).render();

            List<AnimalData> animals = mapper.selectByExample(renderedWhereClause);
            assertThat(animals.size(), is(64));
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testIsGreaterThanOrEqualToCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            RenderedWhereClause renderedWhereClause = where(id, isGreaterThanOrEqualTo(60)).render();

            List<AnimalData> animals = mapper.selectByExample(renderedWhereClause);
            assertThat(animals.size(), is(6));
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testIsLessThanOrEqualToCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            RenderedWhereClause renderedWhereClause = where(id, isLessThanOrEqualTo(10)).render();

            List<AnimalData> animals = mapper.selectByExample(renderedWhereClause);
            assertThat(animals.size(), is(10));
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testInCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            RenderedWhereClause renderedWhereClause = where(id, isIn(5, 8, 10)).render();

            List<AnimalData> animals = mapper.selectByExample(renderedWhereClause);
            assertThat(animals.size(), is(3));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testNotInCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            RenderedWhereClause renderedWhereClause = where(id, isNotIn(5, 8, 10)).render();

            List<AnimalData> animals = mapper.selectByExample(renderedWhereClause);
            assertThat(animals.size(), is(62));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testLikeCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            RenderedWhereClause renderedWhereClause = where(animalName, isLike("%squirrel")).render();

            List<AnimalData> animals = mapper.selectByExample(renderedWhereClause);
            assertThat(animals.size(), is(2));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testNotLikeCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            RenderedWhereClause renderedWhereClause = where(animalName, isNotLike("%squirrel")).render();

            List<AnimalData> animals = mapper.selectByExample(renderedWhereClause);
            assertThat(animals.size(), is(63));
        } finally {
            sqlSession.close();
        }
    }

//    @Test
//    public void testDeleteThreeRows() {
//        SqlSession sqlSession = sqlSessionFactory.openSession();
//        try {
//            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
//            
//            RenderedWhereClause myExample = where(id, isIn(5, 8, 10)).renderWithoutTableAlias();
//
//            int rowCount = mapper.deleteByExample(myExample);
//            assertThat(rowCount, is(3));
//        } finally {
//            sqlSession.close();
//        }
//    }
    
    @Test
    public void testIsNullCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            RenderedWhereClause renderedWhereClause = where(id, isNull()).render();

            List<AnimalData> animals = mapper.selectByExample(renderedWhereClause);
            assertThat(animals.size(), is(0));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testIsNotNullCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            RenderedWhereClause renderedWhereClause = where(id, isNotNull()).render();

            List<AnimalData> animals = mapper.selectByExample(renderedWhereClause);
            assertThat(animals.size(), is(65));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testComplexCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            RenderedWhereClause renderedWhereClause = where(id, isIn(1, 5, 7))
                    .or(id, isIn(2, 6, 8), and(animalName, isLike("%bat")))
                    .or(id, isGreaterThan(60))
                    .and(bodyWeight, isBetween(1.0).and(3.0))
                    .render();

            List<AnimalData> animals = mapper.selectByExample(renderedWhereClause);
            assertThat(animals.size(), is(4));
        } finally {
            sqlSession.close();
        }
    }
}
