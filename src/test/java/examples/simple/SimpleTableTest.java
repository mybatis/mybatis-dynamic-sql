package examples.simple;

import static examples.simple.SimpleTableFields.firstName;
import static examples.simple.SimpleTableFields.id;
import static examples.simple.SimpleTableFields.occupation;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mybatis.qbe.sql.where.SqlConditions.isEqualTo;
import static org.mybatis.qbe.sql.where.SqlConditions.isIn;
import static org.mybatis.qbe.sql.where.SqlConditions.isNull;
import static org.mybatis.qbe.sql.where.render.WhereClauseShortcut.*;

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
import org.mybatis.qbe.sql.where.render.WhereSupport;

public class SimpleTableTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver"; 
    
    private SqlSessionFactory sqlSessionFactory;
    
    @Before
    public void setup() throws Exception{
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/simple/CreateSimpleDB.sql");
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }
        
        is = getClass().getResourceAsStream("/examples/simple/MapperConfig.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
    }
    
    @Test
    public void testSelectByExampleInXML() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableMapper mapper = session.getMapper(SimpleTableMapper.class);
            
            WhereSupport whereSupport = where(id, isEqualTo(1))
                    .or(occupation, isNull())
                    .build();
            
            List<SimpleTable> rows = mapper.selectByExample(whereSupport);
            
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
            
            WhereSupport whereSupport = where(firstName, isIn("Fred", "Barney"))
                    .build();
            
            List<SimpleTable> rows = mapper.selectByExample(whereSupport);
            
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
            WhereSupport whereSupport = where(occupation, isNull()).buildIgnoringAlias();
            int rows = mapper.deleteByExample(whereSupport);
            
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
            WhereSupport whereSupport = where(id, isEqualTo(1)).build();
            List<SimpleTable> rows = mapper.selectByExampleWithProvider(whereSupport);
            
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
            WhereSupport whereSupport = where(occupation, isNull()).buildIgnoringAlias();
            int rows = mapper.deleteByExampleWithProvider(whereSupport);
            
            assertThat(rows, is(2));
        } finally {
            session.close();
        }
    }
}
