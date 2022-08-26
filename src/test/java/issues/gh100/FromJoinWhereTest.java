/*
 *    Copyright 2016-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
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

class FromJoinWhereTest {

    @Test
    void testNormalUsage() {
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
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFrom() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        String expected = "select id, name, idcard"
                + " from student";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testfromJoinWhereB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testfromJoinWhereB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testfromJoinWhereB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionB1() {
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

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionB2() {
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

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionB3() {
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

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionB4() {
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

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionUnionB1() {
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

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionUnionB2() {
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

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionUnionB3() {
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

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionUnionB4() {
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

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionUnionB5() {
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

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByB1() {
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

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByB2() {
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

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByB3() {
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

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByB4() {
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

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByB5() {
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

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByLimitB1() {
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
                + " limit #{parameters.p2}";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByLimitB2() {
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
                + " limit #{parameters.p2}";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByLimitB3() {
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
                + " limit #{parameters.p2}";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByLimitB4() {
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
                + " limit #{parameters.p2}";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByLimitB5() {
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
                + " limit #{parameters.p2}";

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByLimitB6() {
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
                + " limit #{parameters.p2}";

        SelectStatementProvider selectStatement = builder6.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByLimitOffsetB1() {
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

        builder6.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByLimitOffsetB2() {
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

        builder6.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByLimitOffsetB3() {
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

        builder6.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByLimitOffsetB4() {
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

        builder6.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByLimitOffsetB5() {
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

        builder6.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByLimitOffsetB6() {
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

        builder6.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder6.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByLimitOffsetB7() {
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
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder7.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByOffsetB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);

        builder5.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " offset #{parameters.p2} rows";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);

    }

    @Test
    void testFromJoinWhereUnionOrderByOffsetB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);

        builder5.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " offset #{parameters.p2} rows";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);

    }

    @Test
    void testFromJoinWhereUnionOrderByOffsetB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);

        builder5.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " offset #{parameters.p2} rows";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);

    }

    @Test
    void testFromJoinWhereUnionOrderByOffsetB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);

        builder5.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " offset #{parameters.p2} rows";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);

    }

    @Test
    void testFromJoinWhereUnionOrderByOffsetB5() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);

        builder5.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " offset #{parameters.p2} rows";

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);

    }

    @Test
    void testFromJoinWhereUnionOrderByOffsetB6() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder6 = builder5.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " offset #{parameters.p2} rows";

        SelectStatementProvider selectStatement = builder6.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);

    }

    @Test
    void testFromJoinWhereUnionOrderByOffsetFetchFirstB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder6 = builder5.offset(2);

        builder6.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByOffsetFetchFirstB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder6 = builder5.offset(2);

        builder6.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByOffsetFetchFirstB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder6 = builder5.offset(2);

        builder6.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByOffsetFetchFirstB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder6 = builder5.offset(2);

        builder6.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByOffsetFetchFirstB5() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder6 = builder5.offset(2);

        builder6.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByOffsetFetchFirstB6() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder6 = builder5.offset(2);

        builder6.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder6.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByOffsetFetchFirstB7() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder6 = builder5.offset(2);

        SelectDSL<SelectModel>.RowsOnlyFinisher builder7 = builder6.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder7.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByFetchFirstB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);

        builder5.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " fetch first #{parameters.p2} rows only";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByFetchFirstB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);

        builder5.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " fetch first #{parameters.p2} rows only";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByFetchFirstB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);

        builder5.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " fetch first #{parameters.p2} rows only";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByFetchFirstB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);

        builder5.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " fetch first #{parameters.p2} rows only";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByFetchFirstB5() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);

        builder5.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " fetch first #{parameters.p2} rows only";

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOrderByFetchFirstB6() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel> builder5 = builder4.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.RowsOnlyFinisher builder6 = builder5.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " fetch first #{parameters.p2} rows only";

        SelectStatementProvider selectStatement = builder6.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionLimitB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder4.limit(3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " limit #{parameters.p2}";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionLimitB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder4.limit(3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " limit #{parameters.p2}";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionLimitB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder4.limit(3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " limit #{parameters.p2}";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionLimitB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder4.limit(3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " limit #{parameters.p2}";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionLimitB5() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel>.LimitFinisher builder5 = builder4.limit(3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " limit #{parameters.p2}";

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionLimitOffsetB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel>.LimitFinisher builder5 = builder4.limit(3);

        builder5.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionLimitOffsetB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel>.LimitFinisher builder5 = builder4.limit(3);

        builder5.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionLimitOffsetB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel>.LimitFinisher builder5 = builder4.limit(3);

        builder5.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionLimitOffsetB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel>.LimitFinisher builder5 = builder4.limit(3);

        builder5.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionLimitOffsetB5() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel>.LimitFinisher builder5 = builder4.limit(3);

        builder5.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionLimitOffsetB6() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel>.LimitFinisher builder5 = builder4.limit(3);

        SelectDSL<SelectModel>.OffsetFinisher builder6 = builder5.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder6.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOffsetB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder4.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " offset #{parameters.p2} rows";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOffsetB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder4.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " offset #{parameters.p2} rows";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOffsetB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder4.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " offset #{parameters.p2} rows";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOffsetB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder4.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " offset #{parameters.p2} rows";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOffsetB5() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder5 = builder4.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " offset #{parameters.p2} rows";

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOffsetFetchFirstB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder5 = builder4.offset(2);

        builder5.fetchFirst(2).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOffsetFetchFirstB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder5 = builder4.offset(2);

        builder5.fetchFirst(2).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOffsetFetchFirstB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder5 = builder4.offset(2);

        builder5.fetchFirst(2).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOffsetFetchFirstB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder5 = builder4.offset(2);

        builder5.fetchFirst(2).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOffsetFetchFirstB5() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder5 = builder4.offset(2);

        builder5.fetchFirst(2).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionOffsetFetchFirstB6() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder5 = builder4.offset(2);

        SelectDSL<SelectModel>.RowsOnlyFinisher builder6 = builder5.fetchFirst(2).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder6.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionFetchFirstB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder4.fetchFirst(2).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " fetch first #{parameters.p2} rows only";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionFetchFirstB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder4.fetchFirst(2).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " fetch first #{parameters.p2} rows only";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionFetchFirstB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder4.fetchFirst(2).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " fetch first #{parameters.p2} rows only";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionFetchFirstB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder4.fetchFirst(2).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " fetch first #{parameters.p2} rows only";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereUnionFetchFirstB5() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        QueryExpressionDSL<SelectModel> builder4 = builder3.union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        SelectDSL<SelectModel>.RowsOnlyFinisher builder5 = builder4.fetchFirst(2).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " fetch first #{parameters.p2} rows only";

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        builder3.orderBy(StudentDynamicSqlSupport.id);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        builder3.orderBy(StudentDynamicSqlSupport.id);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        builder3.orderBy(StudentDynamicSqlSupport.id);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByLimitB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        builder4.limit(3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " limit #{parameters.p2}";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByLimitB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        builder4.limit(3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " limit #{parameters.p2}";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByLimitB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        builder4.limit(3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " limit #{parameters.p2}";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByLimitB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        builder4.limit(3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " limit #{parameters.p2}";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByLimitB5() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.LimitFinisher builder5 = builder4.limit(3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " limit #{parameters.p2}";

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByLimitOffsetB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.LimitFinisher builder5 = builder4.limit(3);

        builder5.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByLimitOffsetB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.LimitFinisher builder5 = builder4.limit(3);

        builder5.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByLimitOffsetB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.LimitFinisher builder5 = builder4.limit(3);

        builder5.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByLimitOffsetB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.LimitFinisher builder5 = builder4.limit(3);

        builder5.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByLimitOffsetB5() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.LimitFinisher builder5 = builder4.limit(3);

        builder5.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByLimitOffsetB6() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.LimitFinisher builder5 = builder4.limit(3);

        SelectDSL<SelectModel>.OffsetFinisher builder6 = builder5.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder6.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByOffsetB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        builder4.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " offset #{parameters.p2} rows";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByOffsetB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        builder4.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " offset #{parameters.p2} rows";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByOffsetB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        builder4.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " offset #{parameters.p2} rows";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByOffsetB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        builder4.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " offset #{parameters.p2} rows";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByOffsetB5() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder5 = builder4.offset(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " offset #{parameters.p2} rows";

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByOffsetFetchFirstB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder5 = builder4.offset(2);

        builder5.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByOffsetFetchFirstB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder5 = builder4.offset(2);

        builder5.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByOffsetFetchFirstB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder5 = builder4.offset(2);

        builder5.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByOffsetFetchFirstB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder5 = builder4.offset(2);

        builder5.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByOffsetFetchFirstB5() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder5 = builder4.offset(2);

        builder5.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByOffsetFetchFirstB6() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.OffsetFirstFinisher builder5 = builder4.offset(2);

        SelectDSL<SelectModel>.RowsOnlyFinisher builder6 = builder5.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder6.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByFetchFirstB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        builder4.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " fetch first #{parameters.p2} rows only";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByFetchFirstB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        builder4.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " fetch first #{parameters.p2} rows only";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByFetchFirstB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        builder4.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " fetch first #{parameters.p2} rows only";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByFetchFirstB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        builder4.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " fetch first #{parameters.p2} rows only";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOrderByFetchFirstB5() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel> builder4 = builder3.orderBy(StudentDynamicSqlSupport.id);

        SelectDSL<SelectModel>.RowsOnlyFinisher builder5 = builder4.fetchFirst(3).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " fetch first #{parameters.p2} rows only";

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereLimitB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        builder3.limit(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " limit #{parameters.p2}";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereLimitB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        builder3.limit(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " limit #{parameters.p2}";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereLimitB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        builder3.limit(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " limit #{parameters.p2}";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereLimitB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel>.LimitFinisher builder4 = builder3.limit(2);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " limit #{parameters.p2}";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereLimitOffsetB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel>.LimitFinisher builder4 = builder3.limit(2);

        builder4.offset(3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereLimitOffsetB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel>.LimitFinisher builder4 = builder3.limit(2);

        builder4.offset(3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereLimitOffsetB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel>.LimitFinisher builder4 = builder3.limit(2);

        builder4.offset(3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereLimitOffsetB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel>.LimitFinisher builder4 = builder3.limit(2);

        builder4.offset(3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereLimitOffsetB5() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel>.LimitFinisher builder4 = builder3.limit(2);

        SelectDSL<SelectModel>.OffsetFinisher builder5 = builder4.offset(3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOffsetB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        builder3.offset(3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " offset #{parameters.p2} rows";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOffsetB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        builder3.offset(3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " offset #{parameters.p2} rows";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOffsetB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        builder3.offset(3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " offset #{parameters.p2} rows";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOffsetB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel>.OffsetFirstFinisher builder4 = builder3.offset(3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " offset #{parameters.p2} rows";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOffsetFetchFirstB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel>.OffsetFirstFinisher builder4 = builder3.offset(3);

        builder4.fetchFirst(2).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOffsetFetchFirstB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel>.OffsetFirstFinisher builder4 = builder3.offset(3);

        builder4.fetchFirst(2).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOffsetFetchFirstB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel>.OffsetFirstFinisher builder4 = builder3.offset(3);

        builder4.fetchFirst(2).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOffsetFetchFirstB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel>.OffsetFirstFinisher builder4 = builder3.offset(3);

        builder4.fetchFirst(2).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereOffsetFetchFirstB5() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel>.OffsetFirstFinisher builder4 = builder3.offset(3);

        SelectDSL<SelectModel>.RowsOnlyFinisher builder5 = builder4.fetchFirst(2).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";

        SelectStatementProvider selectStatement = builder5.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereFetchFirstB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        builder3.fetchFirst(2).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " fetch first #{parameters.p2} rows only";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereFetchFirstB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        builder3.fetchFirst(2).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " fetch first #{parameters.p2} rows only";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereFetchFirstB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        builder3.fetchFirst(2).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " fetch first #{parameters.p2} rows only";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromJoinWhereFetchFirstB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher builder2 = builder1.join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid));

        QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder builder3 = builder2.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectDSL<SelectModel>.RowsOnlyFinisher builder4 = builder3.fetchFirst(2).rowsOnly();

        String expected = "select student.id, student.name, student.idcard"
                + " from student"
                + " join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " fetch first #{parameters.p2} rows only";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
}
