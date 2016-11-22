package examples.animal.data;

import org.mybatis.qbe.sql.where.render.RenderedWhereClause;

public class AnimalDataSqlProvider {

    public String selectByExample(RenderedWhereClause renderedWhereClause) {
        StringBuilder buffer = new StringBuilder();
        
        buffer.append("select a.id, a.animal_name as animalName, a.brain_weight as brainWeight, a.body_weight as bodyWeight");
        buffer.append(" from AnimalData a");
        if (renderedWhereClause != null) {
            buffer.append(' ');
            buffer.append(renderedWhereClause.getWhereClause());
        }
        buffer.append(" order by a.id");
        
        return buffer.toString();
    }

    public String deleteByExample(RenderedWhereClause renderedWhereClause) {
        StringBuilder buffer = new StringBuilder();
        
        buffer.append("delete from AnimalData");
        if (renderedWhereClause != null) {
            buffer.append(' ');
            buffer.append(renderedWhereClause.getWhereClause());
        }
        
        return buffer.toString();
    }
}
