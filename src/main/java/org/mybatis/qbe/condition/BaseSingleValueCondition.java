package org.mybatis.qbe.condition;

public abstract class BaseSingleValueCondition<T> implements SingleValueCondition<T> {

    private T value;
    
    protected BaseSingleValueCondition(T value) {
        this.value = value;
    }

    @Override
    public T value() {
        return value;
    }
}
