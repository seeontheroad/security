package demo.security.filter;

import com.alibaba.fastjson.JSON;
import demo.security.config.MyAuthenticationException;
import demo.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * jwt登录授权过滤器
 */
@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    @Value("${tokenHeader}")
    private String tokenHeader;
    @Value("${tokenHead}")
    private String tokenHead;
    @Autowired
    JwtTokenUtil jwtTokenUtil;
    @Autowired
    UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws IOException, ServletException {
        String header = httpServletRequest.getHeader(tokenHeader);
        //存在token
        if (header != null && header.startsWith(tokenHead)) {
            String token = header.substring(tokenHead.length()+1);
            String username = null;
            try {
                username = jwtTokenUtil.getUsernameFromToken(token);
            //token存在,但是未登录
            if (username != null && null == SecurityContextHolder.getContext().getAuthentication()) {
                //登录
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtTokenUtil.validateToken(token)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
            }catch (MyAuthenticationException e){
                httpServletResponse.setHeader("Content-Type","application/json;charset=utf-8");
                httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
                httpServletResponse.setHeader("Access-Control-Allow-Method", "POST,GET");
                PrintWriter pw =httpServletResponse.getWriter();
                Map<String,String> map = new HashMap<>();
                map.put("code","1403");
                map.put("msg",e.getMessage());
                pw.write(JSON.toJSONString(map));
                pw.flush();
                pw.close();
                return;
            }catch (Exception e){
                httpServletResponse.setHeader("Content-Type","application/json;charset=utf-8");
                httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
                httpServletResponse.setHeader("Access-Control-Allow-Method", "POST,GET");
                PrintWriter pw =httpServletResponse.getWriter();
                Map<String,String> map = new HashMap<>();
                map.put("code","1403");
                map.put("msg","未知错误,请联系管理员!");
                pw.write(JSON.toJSONString(map));
                pw.flush();
                pw.close();
                return;
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
