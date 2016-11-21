package org.mybatis.qbe;

/**
 * 
 * @author Jeff Butler
 *
 * @param <T> - even though the type is not directly used in this class,
 *  it is used by the compiler to match fields with conditions so it should
 *  not be removed.
 */
public abstract class Field<T> {

    protected String name;
    
    protected Field(String name) {
        this.name = name;
    }
    
    public String name() {
        return name;
    }

    /**
     * This method provides an opportunity to alter the field name on the left side of the
     * expression if required for any reason (such as when there is an SQL table alias).
     * 
     * @return
     */
    public abstract String render();
    
    /**
     * This returns the value on the "right side" of the expression.
     * 
     * @param parameterNumber
     * @return
     */
    public abstract Renderer getParameterRenderer(int parameterNumber);
}
