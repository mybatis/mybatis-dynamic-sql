/**
 *    Copyright 2016-2019 the original author or authors.
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
package issues.gh100;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectDSL;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

public class Issue100TestStartAfterJoin {

    @Test
    public void testSuccessiveBuild02() {
        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        builder.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
                
        SelectStatementProvider selectStatement = builder.build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expected = "select student.id, student.name, student.idcard" 
                + " from student join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    public void testSuccessiveBuild03() {
        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        SelectDSL<SelectModel> selectModel = builder.orderBy(StudentDynamicSqlSupport.id);
                
        SelectStatementProvider selectStatement = builder.build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expected = "select student.id, student.name, student.idcard" 
                + " from student join student_reg on student.id = student_reg.studentId"
                + " order by id";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        
        selectStatement = selectModel.build().render(RenderingStrategies.MYBATIS3);
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    public void testSuccessiveBuild04() {
        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        builder.limit(3);

        SelectStatementProvider selectStatement = builder.build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expected = "select student.id, student.name, student.idcard" 
                + " from student join student_reg on student.id = student_reg.studentId"
                + " limit #{parameters._limit}";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    public void testSuccessiveBuild05() {
        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        builder.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"))
        .orderBy(StudentDynamicSqlSupport.id)
        .limit(3)
        .offset(2);

        SelectStatementProvider selectStatement = builder.build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expected = "select student.id, student.name, student.idcard" 
                + " from student join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " limit #{parameters._limit}"
                + " offset #{parameters._offset}";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    public void testSuccessiveBuild06() {
        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        builder.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"))
        .orderBy(StudentDynamicSqlSupport.id)
        .offset(2);

        SelectStatementProvider selectStatement = builder.build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expected = "select student.id, student.name, student.idcard" 
                + " from student join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " offset #{parameters._offset} rows";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    public void testSuccessiveBuild07() {
        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        builder.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"))
        .orderBy(StudentDynamicSqlSupport.id)
        .offset(2)
        .fetchFirst(3).rowsOnly();

        SelectStatementProvider selectStatement = builder.build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expected = "select student.id, student.name, student.idcard" 
                + " from student join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " offset #{parameters._offset} rows"
                + " fetch first #{parameters._fetchFirstRows} rows only";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    public void testSuccessiveBuild08() {
        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        builder.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"))
        .orderBy(StudentDynamicSqlSupport.id)
        .fetchFirst(3).rowsOnly();

        SelectStatementProvider selectStatement = builder.build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expected = "select student.id, student.name, student.idcard" 
                + " from student join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " fetch first #{parameters._fetchFirstRows} rows only";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
}
