/**
 *    Copyright 2016 the original author or authors.
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
