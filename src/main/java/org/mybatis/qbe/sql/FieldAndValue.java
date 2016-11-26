package org.mybatis.qbe.sql;

/**
 * A field value pair used to render insert and update statements.
 * 
 * In an insert statement it is used in the field list and value clauses.  For
 * example:
 *   insert into Bar (foo) values(?)
 * 
 * In an update statement it is used to render the set clause.  For example: 
 *   set foo = ?
 * 
 * @author Jeff Butler
 *
 * @param <T>
 */
public class FieldAndValue<T> {
    private T value;
    private SqlField<T> field;
    
    private FieldAndValue() {
        super();
    }
    
    public T getValue() {
        return value;
    }

    public SqlField<T> getField() {
        return field;
    }
    
    public FieldAndValue<T> ignoringAlias() {
        return FieldAndValue.of(field.ignoringAlias(), value);
    }
    
    public static <S> FieldAndValue<S> of(SqlField<S> field, S value) {
        FieldAndValue<S> phrase = new FieldAndValue<>();
        phrase.value = value;
        phrase.field = field;
        return phrase;
    }

    public static <S> FieldAndValue<S> of(SqlField<S> field) {
        FieldAndValue<S> phrase = new FieldAndValue<>();
        phrase.field = field;
        return phrase;
    }
}
