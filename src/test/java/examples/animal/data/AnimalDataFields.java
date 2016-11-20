package examples.animal.data;

import java.sql.JDBCType;

import org.mybatis.qbe.mybatis3.MyBatis3Field;

public interface AnimalDataFields {
    MyBatis3Field<Integer> id = MyBatis3Field.of("id", JDBCType.INTEGER, "a"); 
    MyBatis3Field<String> animalName = MyBatis3Field.of("animal_name", JDBCType.VARCHAR, "a");
    MyBatis3Field<Double> bodyWeight = MyBatis3Field.of("body_weight", JDBCType.DOUBLE, "a");
}
