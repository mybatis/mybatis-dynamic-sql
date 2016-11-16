package org.mybatis.qbe.field;

import java.sql.JDBCType;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * 
 * @author Jeff Butler
 *
 * @param <T> - even though the type is not directly used in this class,
 *  it is used by the compiler to match fields with conditions so it should
 *  not be removed.
 */
public class Field<T> {

    private String name;
    private JDBCType jdbcType;
    private String alias;
    private String typeHandler;
    private BiFunction<Field<?>, Boolean, String> customRenderer;
    
    private Field(String name, JDBCType jdbcType) {
        this.name = name;
        this.jdbcType = jdbcType;
    }
    
    public String name() {
        return name;
    }
    
    public JDBCType jdbcType() {
        return jdbcType;
    }
    
    public Optional<String> alias() {
        return Optional.ofNullable(alias);
    }
    
    public Optional<String> typeHandler() {
        return Optional.ofNullable(typeHandler);
    }
    
    public Optional<BiFunction<Field<?>, Boolean, String>> customRenderer() {
        return Optional.ofNullable(customRenderer);
    }
    
    public <S> Field<S> withAlias(String alias) {
        Field<S> answer = new Field<>(name, jdbcType);
        answer.alias = alias;
        answer.typeHandler = typeHandler;
        answer.customRenderer = customRenderer;
        return answer;
    }

    public <S> Field<S> withTypeHandler(String typeHandler) {
        Field<S> answer = new Field<>(name, jdbcType);
        answer.alias = alias;
        answer.typeHandler = typeHandler;
        answer.customRenderer = customRenderer;
        return answer;
    }
    
    public <S> Field<S> withCustomRenderer(BiFunction<Field<?>, Boolean, String> customRenderer) {
        Field<S> answer = new Field<>(name, jdbcType);
        answer.alias = alias;
        answer.typeHandler = typeHandler;
        answer.customRenderer = customRenderer;
        return answer;
    }
    
    public static <T> Field<T> of(String name, JDBCType jdbcType) {
        return new Field<>(name, jdbcType);
    }
}
