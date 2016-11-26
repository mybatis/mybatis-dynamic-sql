package org.mybatis.qbe.mybatis3;

import java.sql.JDBCType;
import java.util.Optional;

import org.mybatis.qbe.Renderer;
import org.mybatis.qbe.sql.SqlField;

/**
 * 
 * @author Jeff Butler
 *
 */
public class MyBatis3Field<T> extends SqlField<T> {

    protected String typeHandler;
    
    protected MyBatis3Field(String name, JDBCType jdbcType, String alias) {
        super(name, jdbcType, alias);
    }
    
    public Optional<String> typeHandler() {
        return Optional.ofNullable(typeHandler);
    }
    
    public <S> MyBatis3Field<S> withTypeHandler(String typeHandler) {
        MyBatis3Field<S> field = MyBatis3Field.of(name, jdbcType, alias);
        field.typeHandler = typeHandler;
        return field;
    }
    
    @Override
    public <S> MyBatis3Field<S> ignoringAlias() {
        MyBatis3Field<S> field = MyBatis3Field.of(name, jdbcType);
        field.typeHandler = typeHandler;
        return field;
    }
    
    @Override
    public <S> MyBatis3Field<S> withAlias(String alias) {
        MyBatis3Field<S> field = MyBatis3Field.of(name, jdbcType, alias);
        field.typeHandler = typeHandler;
        return field;
    }
    
    public static <T> MyBatis3Field<T> of(String name, JDBCType jdbcType) {
        return MyBatis3Field.of(name, jdbcType, null);
    }
    
    public static <T> MyBatis3Field<T> of(String name, JDBCType jdbcType, String alias) {
        return new MyBatis3Field<>(name, jdbcType, alias);
    }
    
    @Override
    public Renderer getParameterRenderer(int parameterNumber) {
        return new DefaultRenderer(this, parameterNumber);
    }
    
    public static class DefaultRenderer implements Renderer {
        
        private MyBatis3Field<?> field;
        private int parameterNumber;

        public DefaultRenderer(MyBatis3Field<?> field, int parameterNumber) {
            this.field = field;
            this.parameterNumber = parameterNumber;
        }
        
        @Override
        public String render() {
            StringBuilder buffer = new StringBuilder();
            buffer.append("#{parameters.p"); //$NON-NLS-1$
            buffer.append(parameterNumber);
            buffer.append(",jdbcType="); //$NON-NLS-1$
            buffer.append(field.jdbcType().getName());
            
            field.typeHandler().ifPresent(th -> {
                buffer.append(",typeHandler="); //$NON-NLS-1$
                buffer.append(th);
            });
            
            buffer.append('}');
            return buffer.toString();
        }
    }
}
