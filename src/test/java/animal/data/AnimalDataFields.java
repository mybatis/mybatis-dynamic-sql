package animal.data;

import java.sql.JDBCType;

import org.mybatis.qbe.field.Field;

public interface AnimalDataFields {
    Field<Integer> id = Field.of("id", JDBCType.INTEGER).withAlias("a"); 
    Field<String> animalName = Field.of("animal_name", JDBCType.VARCHAR).withAlias("a");
    Field<Double> bodyWeight = Field.of("body_weight", JDBCType.DOUBLE).withAlias("a");
}
