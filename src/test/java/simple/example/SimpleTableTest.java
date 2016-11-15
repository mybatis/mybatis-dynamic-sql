package simple.example;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.assertThat;
import static simple.example.SimpleTableFields.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import static org.mybatis.qbe.condition.Conditions.*;
import static org.mybatis.qbe.mybatis3.WhereClauseAndParameters.where;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.qbe.mybatis3.WhereClauseAndParameters;

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
            
            WhereClauseAndParameters whereClauseAndParameters = where(id, isEqualTo(1))
                    .or(occupation, isNull())
                    .build();
            
            List<SimpleTable> rows = mapper.selectByExample(whereClauseAndParameters);
            
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
            
            WhereClauseAndParameters whereClauseAndParameters = where(firstName, isIn("Fred", "Barney"))
                    .build();
            
            List<SimpleTable> rows = mapper.selectByExample(whereClauseAndParameters);
            
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
            WhereClauseAndParameters whereClauseAndParameters = where(occupation, isNull()).buildWithoutTableAlias();
            int rows = mapper.deleteByExample(whereClauseAndParameters);
            
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
            WhereClauseAndParameters whereClauseAndParameters = where(id, isEqualTo(1)).build();
            List<SimpleTable> rows = mapper.selectByExampleWithProvider(whereClauseAndParameters);
            
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
            WhereClauseAndParameters whereClauseAndParameters = where(occupation, isNull()).buildWithoutTableAlias();
            int rows = mapper.deleteByExampleWithProvider(whereClauseAndParameters);
            
            assertThat(rows, is(2));
        } finally {
            session.close();
        }
    }
}
