package org.mybatis.qbe.mybatis3;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mybatis.qbe.sql.insert.InsertSupportBuilder.insertSupport;

import java.sql.JDBCType;

import org.junit.Test;
import org.mybatis.qbe.sql.insert.InsertSupport;

public class InsertSupportTest {
    
    @Test
    public void testInsertSupportBuilder() {
        MyBatis3Field<Integer> id = MyBatis3Field.of("id", JDBCType.INTEGER);
        MyBatis3Field<String> firstName = MyBatis3Field.of("firstName", JDBCType.VARCHAR);
        MyBatis3Field<String> lastName = MyBatis3Field.of("lastName", JDBCType.VARCHAR);
        MyBatis3Field<String> occupation = MyBatis3Field.of("occupation", JDBCType.VARCHAR);
        
        InsertSupport insertSupport = insertSupport() 
                .withNullValue(firstName)
                .withValue(lastName, "jones")
                .withNullValue(id)
                .withValue(occupation, "dino driver")
                .build();
        
        String expectedFieldsPhrase = "(firstName, lastName, id, occupation)";
        assertThat(insertSupport.getFieldsPhrase(), is(expectedFieldsPhrase));

        String expectedValuesPhrase = "values (#{parameters.p1,jdbcType=VARCHAR}, "
                + "#{parameters.p2,jdbcType=VARCHAR}, "
                + "#{parameters.p3,jdbcType=INTEGER}, "
                + "#{parameters.p4,jdbcType=VARCHAR})";
        assertThat(insertSupport.getValuesPhrase(), is(expectedValuesPhrase));
        
        assertThat(insertSupport.getParameters().size(), is(4));
        assertThat(insertSupport.getParameters().get("p1"), is(nullValue()));
        assertThat(insertSupport.getParameters().get("p2"), is("jones"));
        assertThat(insertSupport.getParameters().get("p3"), is(nullValue()));
        assertThat(insertSupport.getParameters().get("p4"), is("dino driver"));
    }
}
