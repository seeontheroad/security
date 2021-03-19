package demo.security.mapper;

import demo.security.entity.Admin;
import demo.security.entity.UrlAndMethod;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface UserMapper {


    @Select("select username,password,enabled from user  where username = #{username} ")
    Admin findByName(@Param("username") String userName);

    @Select("select rolename from role a join user_role b on a.id = b.role_id join user c on b.user_id = c.id where c.username =#{username}")
    List<String> getRolesByUserName(@Param("username") String username);

    @Select("select url,method from url a join role_url b on a.id = b.url_id join  user_role c on b.role_id = c.role_id join user d on c.user_id = d.id where d.username = #{username}")
    List<UrlAndMethod> getUrlByUserName(@Param("username") String username);
}
