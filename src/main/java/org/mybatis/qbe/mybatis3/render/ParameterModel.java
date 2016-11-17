package org.mybatis.qbe.mybatis3.render;

import org.mybatis.qbe.condition.Renderable;
import org.mybatis.qbe.field.Field;

public class ParameterModel implements Renderable {

    private int number;
    private Field<?> field;
    
    private ParameterModel(int number, Field<?> field) {
        super();
        this.number = number;
        this.field = field;
    }
    
    @Override
    public String render() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("#{parameters.p"); //$NON-NLS-1$
        buffer.append(number);
        buffer.append(",jdbcType="); //$NON-NLS-1$
        buffer.append(field.jdbcType().getName());
        
        field.typeHandler().ifPresent(th -> {
            buffer.append(",typeHandler="); //$NON-NLS-1$
            buffer.append(th);
        });
        
        buffer.append('}');
        return buffer.toString();
    }
    
    public static ParameterModel of(int number, Field<?> field) {
        return new ParameterModel(number, field);
    }
}
