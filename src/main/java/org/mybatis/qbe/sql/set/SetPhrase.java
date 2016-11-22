package org.mybatis.qbe.sql.set;

import org.mybatis.qbe.sql.where.SqlField;

public class SetPhrase<T> {
    private T value;
    private SqlField<T> field;
    
    private SetPhrase() {
        super();
    }
    
    public T getValue() {
        return value;
    }

    public SqlField<T> getField() {
        return field;
    }
    
    public static <S> SetPhrase<S> of(SqlField<S> field, S value) {
        SetPhrase<S> phrase = new SetPhrase<>();
        phrase.value = value;
        phrase.field = field;
        return phrase;
    }

    public static <S> SetPhrase<S> of(SqlField<S> field) {
        SetPhrase<S> phrase = new SetPhrase<>();
        phrase.field = field;
        return phrase;
    }
}
