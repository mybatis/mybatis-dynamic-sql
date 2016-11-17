package simple.example;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mybatis.qbe.condition.Conditions.*;
import static org.mybatis.qbe.mybatis3.RenderedWhereClause.where;
import static simple.example.SimpleTableFields.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.qbe.mybatis3.RenderedWhereClause;

public class SimpleTableTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver"; 
    
    private SqlSessionFactory sqlSessionFactory;
    
    @Before
    public void setup() throws Exception{
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/simple/example/CreateSimpleDB.sql");
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }
        
        is = getClass().getResourceAsStream("/simple/example/MapperConfig.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
    }
    
    @Test
    public void testSelectByExampleInXML() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableMapper mapper = session.getMapper(SimpleTableMapper.class);
            
            RenderedWhereClause renderedWhereClause = where(id, isEqualTo(1))
                    .or(occupation, isNull())
                    .render();
            
            List<SimpleTable> rows = mapper.selectByExample(renderedWhereClause);
            
            assertThat(rows.size(), is(3));
        } finally {
            session.close();
        }
    }

    @Test
    public void testFirstNameIn() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableMapper mapper = session.getMapper(SimpleTableMapper.class);
            
            RenderedWhereClause renderedWhereClause = where(firstName, isIn("Fred", "Barney"))
                    .render();
            
            List<SimpleTable> rows = mapper.selectByExample(renderedWhereClause);
            
            assertThat(rows.size(), is(2));
        } finally {
            session.close();
        }
    }

    @Test
    public void testDeleteByExampleInXML() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableMapper mapper = session.getMapper(SimpleTableMapper.class);
            RenderedWhereClause renderedWhereClause = where(occupation, isNull()).renderWithoutTableAlias();
            int rows = mapper.deleteByExample(renderedWhereClause);
            
            assertThat(rows, is(2));
        } finally {
            session.close();
        }
    }

    @Test
    public void testSelectByExampleInProvider() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableMapper mapper = session.getMapper(SimpleTableMapper.class);
            RenderedWhereClause renderedWhereClause = where(id, isEqualTo(1)).render();
            List<SimpleTable> rows = mapper.selectByExampleWithProvider(renderedWhereClause);
            
            assertThat(rows.size(), is(1));
        } finally {
            session.close();
        }
    }

    @Test
    public void testDeleteByExampleInProvider() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableMapper mapper = session.getMapper(SimpleTableMapper.class);
            RenderedWhereClause renderedWhereClause = where(occupation, isNull()).renderWithoutTableAlias();
            int rows = mapper.deleteByExampleWithProvider(renderedWhereClause);
            
            assertThat(rows, is(2));
        } finally {
            session.close();
        }
    }
}
