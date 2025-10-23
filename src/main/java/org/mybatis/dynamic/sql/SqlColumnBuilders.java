package org.mybatis.dynamic.sql;

import org.mybatis.dynamic.sql.render.RenderingStrategy;

public interface SqlColumnBuilders {
    <S> SqlColumnBuilders withTypeHandler(String typeHandler);

    <S> SqlColumnBuilders withRenderingStrategy(RenderingStrategy renderingStrategy);

    <S> SqlColumnBuilders withParameterTypeConverter(
            ParameterTypeConverter<S, ?> parameterTypeConverter);

    <S> SqlColumnBuilders withJavaType(Class<S> javaType);

    <S> SqlColumnBuilders withJavaProperty(String javaProperty);
}
