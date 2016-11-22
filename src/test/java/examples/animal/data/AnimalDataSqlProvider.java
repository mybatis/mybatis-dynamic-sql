package examples.animal.data;

import org.mybatis.qbe.sql.where.render.WhereSupport;

public class AnimalDataSqlProvider {

    public String selectByExample(WhereSupport whereSupport) {
        StringBuilder buffer = new StringBuilder();
        
        buffer.append("select a.id, a.animal_name as animalName, a.brain_weight as brainWeight, a.body_weight as bodyWeight");
        buffer.append(" from AnimalData a");
        if (whereSupport != null) {
            buffer.append(' ');
            buffer.append(whereSupport.getWhereClause());
        }
        buffer.append(" order by a.id");
        
        return buffer.toString();
    }

    public String deleteByExample(WhereSupport whereSupport) {
        StringBuilder buffer = new StringBuilder();
        
        buffer.append("delete from AnimalData");
        if (whereSupport != null) {
            buffer.append(' ');
            buffer.append(whereSupport.getWhereClause());
        }
        
        return buffer.toString();
    }
}
