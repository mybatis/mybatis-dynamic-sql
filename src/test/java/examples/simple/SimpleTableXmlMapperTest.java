package examples.simple;

import static examples.simple.SimpleTableFields.*;
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
import java.util.Date;
import java.util.List;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.qbe.sql.where.render.WhereSupport;

public class SimpleTableXmlMapperTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver"; 
    
    private SqlSessionFactory sqlSessionFactory;
    
    @Before
    public void setup() throws Exception {
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
    public void testSelectByExample() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            
            WhereSupport whereSupport = where(id, isEqualTo(1))
                    .or(occupation, isNull())
                    .build();
            
            List<SimpleTableRecord> rows = mapper.selectByExample(whereSupport);
            
            assertThat(rows.size(), is(3));
        } finally {
            session.close();
        }
    }

    @Test
    public void testFirstNameIn() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            
            WhereSupport whereSupport = where(firstName, isIn("Fred", "Barney"))
                    .build();
            
            List<SimpleTableRecord> rows = mapper.selectByExample(whereSupport);
            
            assertThat(rows.size(), is(2));
        } finally {
            session.close();
        }
    }

    @Test
    public void testDeleteByExample() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            WhereSupport whereSupport = where(occupation, isNull()).buildIgnoringAlias();
            int rows = mapper.deleteByExample(whereSupport);
            
            assertThat(rows, is(2));
        } finally {
            session.close();
        }
    }

    @Test
    public void testInsert() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            record.setBirthDate(new Date());
            record.setOccupation("Developer");
            
            int rows = mapper.insert(buildInsert(record));
            
            assertThat(rows, is(1));
        } finally {
            session.close();
        }
    }

    @Test
    public void testInsertSelective() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            record.setBirthDate(new Date());
            
            int rows = mapper.insert(buildInsertSelective(record));
            
            assertThat(rows, is(1));
        } finally {
            session.close();
        }
    }
}
