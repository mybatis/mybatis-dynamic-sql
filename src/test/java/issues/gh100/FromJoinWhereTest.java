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
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectDSL;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

public class FromJoinWhereTest {

    @Test
    public void testNormalUsage() {
        SelectStatementProvider selectStatement = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid))
                .where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"))
                .union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .orderBy(StudentDynamicSqlSupport.id)
                .limit(3)
                .offset(2)
                .build()
                .render(RenderingStrategy.MYBATIS3);
        
        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " limit #{parameters._limit}"
                + " offset #{parameters._offset}";
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    public void testFrom() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);
        
        String expected = "select id, name, idcard" 
                + " from student";
        
        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    public void testFromJoinB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId";
        
        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategy.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    public void testFromJoinB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId";
        
        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategy.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    public void testfromJoinWhereB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategy.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testfromJoinWhereB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategy.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testfromJoinWhereB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategy.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionUnionB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .where(StudentDynamicSqlSupport.id, isNotNull());

        builder4.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .where(StudentDynamicSqlSupport.id, isNull());
        
        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " where id is not null"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " where id is null";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionUnionB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .where(StudentDynamicSqlSupport.id, isNotNull());

        builder4.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .where(StudentDynamicSqlSupport.id, isNull());
        
        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " where id is not null"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " where id is null";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionUnionB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .where(StudentDynamicSqlSupport.id, isNotNull());

        builder4.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .where(StudentDynamicSqlSupport.id, isNull());
        
        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " where id is not null"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " where id is null";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionUnionB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .where(StudentDynamicSqlSupport.id, isNotNull());

        builder4.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .where(StudentDynamicSqlSupport.id, isNull());
        
        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " where id is not null"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " where id is null";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionUnionB5() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .where(StudentDynamicSqlSupport.id, isNotNull());

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder5 = builder4.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .where(StudentDynamicSqlSupport.id, isNull());
        
        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " where id is not null"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " where id is null";

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionOrderByB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);
        
        builder4.orderBy(StudentDynamicSqlSupport.id);

        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionOrderByB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);
        
        builder4.orderBy(StudentDynamicSqlSupport.id);

        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionOrderByB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);
        
        builder4.orderBy(StudentDynamicSqlSupport.id);

        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionOrderByB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);
        
        builder4.orderBy(StudentDynamicSqlSupport.id);

        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionOrderByB5() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);
        
        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);

        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id";

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionOrderByLimitB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);
        
        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);
        
        builder5.limit(3);

        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " limit #{parameters._limit}";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionOrderByLimitB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);
        
        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);
        
        builder5.limit(3);

        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " limit #{parameters._limit}";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionOrderByLimitB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);
        
        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);
        
        builder5.limit(3);

        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " limit #{parameters._limit}";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionOrderByLimitB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);
        
        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);
        
        builder5.limit(3);

        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " limit #{parameters._limit}";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionOrderByLimitB5() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);
        
        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);
        
        builder5.limit(3);

        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " limit #{parameters._limit}";

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionOrderByLimitB6() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);
        
        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);
        
        SelectDSL<SelectModel>.LimitFinisher builder6 = builder5.limit(3);

        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " limit #{parameters._limit}";

        SelectStatementProvider selectStatement = builder6.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionOrderByLimitOffset() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));
        
        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));
        
        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);
        
        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);
        
        SelectDSL<SelectModel>.LimitFinisher builder6 = builder5.limit(3);
        
        SelectDSL<SelectModel>.OffsetFinisher builder7 = builder6.offset(2);

        String expected = "select student.id, student.name, student.idcard" 
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " limit #{parameters._limit}"
                + " offset #{parameters._offset}";

        SelectStatementProvider selectStatement = builder7.build().render(RenderingStrategy.MYBATIS3);
        
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testFromJoinWhereUnionOrderByOffset() {
        
    }
    
    @Test
    public void testFromJoinWhereUnionOrderByOffsetFetchFirst() {
        
    }
    
    @Test
    public void testFromJoinWhereUnionOrderByFetchFirst() {
        
    }
    
    @Test
    public void testFromJoinWhereUnionLimit() {
        
    }
    
    @Test
    public void testFromJoinWhereUnionLimitOffset() {
        
    }
        
    @Test
    public void testFromJoinWhereUnionOffset() {
        
    }
    
    @Test
    public void testFromJoinWhereUnionOffsetFetchFirst() {
        
    }
        
    @Test
    public void testFromJoinWhereUnionFetchFirst() {
        
    }
    
    @Test
    public void testFromJoinWhereOrderBy() {
        
    }
        
    @Test
    public void testFromJoinWhereOrderByLimit() {
        
    }
           
    @Test
    public void testFromJoinWhereOrderByLimitOffset() {
        
    }
        
    @Test
    public void testFromJoinWhereOrderByOffset() {
        
    }
            
    @Test
    public void testFromJoinWhereOrderByOffsetFetchFirst() {
        
    }
        
    @Test
    public void testFromJoinWhereOrderByFetchFirst() {
        
    }
    
    @Test
    public void testFromJoinWhereLimit() {
        
    }
        
    @Test
    public void testFromJoinWhereLimitOffset() {
        
    }
    
    @Test
    public void testFromJoinWhereOffset() {
        
    }
        
    @Test
    public void testFromJoinWhereOffsetFetchFirst() {
        
    }
    
    @Test
    public void testFromJoinWhereFetchFirst() {
        
    }
}
