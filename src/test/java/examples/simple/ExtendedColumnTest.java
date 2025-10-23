/*
 *    Copyright 2016-2025 the original author or authors.
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
package examples.simple;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.ParameterTypeConverter;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategies;

class ExtendedColumnTest {

    private final SqlTable table = SqlTable.of("foo");
    private final PrimaryKeyColumn<Integer> bar = new PrimaryKeyColumn.Builder<Integer>()
            .withName("first_name")
            .withTable(table)
            .isPrimaryKeyColumn(true)
            .build();
    private final ParameterTypeConverter<Integer, String> ptc = Object::toString;

    @Test
    void testPropagatedDescending() {
        var baz = bar.descending();

        assertThat(baz.isPrimaryKeyColumn()).isTrue();
    }

    @Test
    void testPropagatedAlias() {
        var baz = bar.as("fred");

        assertThat(baz.alias()).hasValue("fred");
        assertThat(baz.isPrimaryKeyColumn()).isTrue();
    }

    @Test
    void testPropagatedQualifiedWith() {
        var baz = bar.qualifiedWith("fred");

        assertThat(baz.isPrimaryKeyColumn()).isTrue();
    }

    @Test
    void testPropagatedAsCamelCase() {
        var baz = bar.asCamelCase();

        assertThat(baz.isPrimaryKeyColumn()).isTrue();
    }

    @Test
    void testPropagatedWithTypeHandler() {
        var baz = bar.withTypeHandler("barney");

        assertThat(baz.typeHandler()).hasValue("barney");
        assertThat(baz.isPrimaryKeyColumn()).isTrue();
    }

    @Test
    void testPropagatedRenderingStrategy() {
        var baz = bar.withRenderingStrategy(RenderingStrategies.MYBATIS3);

        assertThat(baz.renderingStrategy()).hasValue(RenderingStrategies.MYBATIS3);
        assertThat(baz.isPrimaryKeyColumn()).isTrue();
    }

    @Test
    void testPropagatedParameterTypeConverter() {
        var baz = bar.withParameterTypeConverter(ptc);

        assertThat(baz.convertParameterType(11)).isEqualTo("11");
        assertThat(baz.isPrimaryKeyColumn()).isTrue();
    }

    @Test
    void testPropagatedJavaType() {
        var baz = bar.withJavaType(Integer.class);

        assertThat(baz.javaType()).hasValue(Integer.class);
        assertThat(baz.isPrimaryKeyColumn()).isTrue();
    }

    @Test
    void testPropagatedJavaProperty() {
        var baz = bar.withJavaProperty("id");

        assertThat(baz.javaProperty()).hasValue("id");
        assertThat(baz.isPrimaryKeyColumn()).isTrue();
    }

    @Test
    void testAll() {
        PrimaryKeyColumn<Integer> baz = bar.descending()
                .as("fred")
                .qualifiedWith("fred")
                .asCamelCase()
                .withTypeHandler("barney")
                .withRenderingStrategy(RenderingStrategies.MYBATIS3)
                .withParameterTypeConverter(ptc)
                .withJavaType(Integer.class)
                .withJavaProperty("id");

        assertThat(baz.alias()).hasValue("\"firstName\"");
        assertThat(baz.typeHandler()).hasValue("barney");
        assertThat(baz.renderingStrategy()).hasValue(RenderingStrategies.MYBATIS3);
        assertThat(baz.convertParameterType(11)).isEqualTo("11");
        assertThat(baz.javaType()).hasValue(Integer.class);
        assertThat(baz.javaProperty()).hasValue("id");
        assertThat(baz.isPrimaryKeyColumn()).isTrue();
    }
}
