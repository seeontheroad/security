package demo.security.config;

import demo.security.entity.UrlAndMethod;
import demo.security.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component("dynamicPermission")
@Slf4j
public class DynamicPermission {

    @Autowired
    private UserMapper userMapper;

    public boolean checkPermission(HttpServletRequest request,
                                   Authentication authentication) {

        log.info("request.getRequestURI():"+  request.getRequestURI());
        Object principal=SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        = authentication.getPrincipal();
        log.info("DynamicPermission principal = " + principal);

        if (principal instanceof UserDetails) {

            UserDetails userDetails = (UserDetails) principal;
            //得到当前的账号
            String username = userDetails.getUsername();
            //通过账号获取资源鉴权
            List<UrlAndMethod> urls = userMapper.getUrlByUserName(username);
            AntPathMatcher antPathMatcher = new AntPathMatcher();
            //当前访问路径
            String requestURI = request.getRequestURI();
            //提交类型
            String urlMethod = request.getMethod();
            //判断当前路径中是否在资源鉴权中
            boolean rs = urls.stream().anyMatch(item -> {
                //判断URL是否匹配
                boolean hashAntPath = antPathMatcher.match(item.getUrl(), requestURI);

                //判断请求方式是否和数据库中匹配（数据库存储：GET,POST,PUT,DELETE）
                String dbMethod = item.getMethod();
                //处理null，万一数据库存值
                dbMethod = (dbMethod == null) ? "" : dbMethod;
                int hasMethod = dbMethod.indexOf(urlMethod);
                //两者都成立，返回真，否则返回假
                return hashAntPath && (hasMethod != -1);
            });
            //返回
            if (rs) {
                return rs;
            }else {
                log.info("您没有访问该API的权限");
                throw  new AccessDeniedException("您没有访问该API的权限！");
            }
        } else {
            log.info("未登录");
            throw  new MyAuthenticationException("未登录!");
        }
    }
}
