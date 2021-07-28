package examples.simple;

import org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper;

public interface AddressMapper extends CommonInsertMapper<AddressRecord>, CommonSelectMapper {
}
