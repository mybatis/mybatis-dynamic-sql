package org.mybatis.qbe.condition;

public abstract class BaseTwoValueCondition<T> implements TwoValueCondition<T> {

    private T value1;
    private T value2;
    
    protected BaseTwoValueCondition(T value1, T value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public T value1() {
        return value1;
    }

    @Override
    public T value2() {
        return value2;
    }
}
