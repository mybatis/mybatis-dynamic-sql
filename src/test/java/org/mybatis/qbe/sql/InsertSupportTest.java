package org.mybatis.qbe.sql;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mybatis.qbe.sql.insert.InsertSupportBuilder.insertSupport;

import java.sql.JDBCType;

import org.junit.Test;
import org.mybatis.qbe.sql.SqlField;
import org.mybatis.qbe.sql.insert.InsertSupport;

public class InsertSupportTest {
    
    @Test
    public void testInsertSupportBuilderSql() {
        SqlField<Integer> id = SqlField.of("id", JDBCType.INTEGER);
        SqlField<String> firstName = SqlField.of("firstName", JDBCType.VARCHAR);
        SqlField<String> lastName = SqlField.of("lastName", JDBCType.VARCHAR);
        SqlField<String> occupation = SqlField.of("occupation", JDBCType.VARCHAR);
        
        InsertSupport insertSupport = insertSupport() 
                .withValue(firstName, "fred")
                .withValue(lastName, "jones")
                .withNullValue(id)
                .withValue(occupation, "dino driver")
                .build();
        
        String expectedFieldsPhrase = "(firstName, lastName, id, occupation)";
        assertThat(insertSupport.getFieldsPhrase(), is(expectedFieldsPhrase));

        String expectedValuesPhrase = "values (?, ?, ?, ?)";
        assertThat(insertSupport.getValuesPhrase(), is(expectedValuesPhrase));
        
        assertThat(insertSupport.getParameters().size(), is(4));
        assertThat(insertSupport.getParameters().get("p1"), is("fred"));
        assertThat(insertSupport.getParameters().get("p2"), is("jones"));
        assertThat(insertSupport.getParameters().get("p3"), is(nullValue()));
        assertThat(insertSupport.getParameters().get("p4"), is("dino driver"));
    }
}
