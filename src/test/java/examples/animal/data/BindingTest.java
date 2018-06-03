/**
 *    Copyright 2016-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package examples.animal.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

/**
 * Tests for understanding where bind parameters are allowed
 *
 */
public class BindingTest {
    
    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver"; 
    
    private SqlSessionFactory sqlSessionFactory;
    
    @BeforeEach
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
    public void testBindInSelectList() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            Connection connection = sqlSession.getConnection();
            
            PreparedStatement ps = connection.prepareStatement("select brain_weight + ? as calc from AnimalData where id = ?");
            ps.setDouble(1, 1.0);
            ps.setInt(2, 1);
            
            ResultSet rs = ps.executeQuery();
            double calculatedWeight = 0.0;
            if (rs.next()) {
                calculatedWeight = rs.getDouble("CALC");
            }
            
            rs.close();
            ps.close();
            
            assertThat(calculatedWeight).isEqualTo(1.005);
        } catch (SQLException e) {
            fail("SQL Exception", e);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testBindInWeirdWhere() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            Connection connection = sqlSession.getConnection();
            
            PreparedStatement ps = connection.prepareStatement("select brain_weight from AnimalData where brain_weight + ? > ? and id = ?");
            ps.setDouble(1, 1.0);
            ps.setDouble(2, 1.0);
            ps.setInt(3, 1);
            
            ResultSet rs = ps.executeQuery();
            double calculatedWeight = 0.0;
            if (rs.next()) {
                calculatedWeight = rs.getDouble("BRAIN_WEIGHT");
            }
            
            rs.close();
            ps.close();
            
            assertThat(calculatedWeight).isEqualTo(.005);
        } catch (SQLException e) {
            fail("SQL Exception", e);
        } finally {
            sqlSession.close();
        }
    }
}
