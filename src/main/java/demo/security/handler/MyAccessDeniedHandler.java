package demo.security.handler;


import com.alibaba.fastjson.JSON;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 没权限后的处理
 */
@Component
public class MyAccessDeniedHandler  implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
        //设置相应的状态码
        httpServletResponse.setHeader("Content-Type","application/json;charset=utf-8");
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpServletResponse.setHeader("Access-Control-Allow-Method", "POST,GET");
        PrintWriter pw =httpServletResponse.getWriter();
        Map<String,String> map = new HashMap<>();
        map.put("code","403");
        map.put("msg",e.getMessage());
        pw.write(JSON.toJSONString(map));
        pw.flush();
        pw.close();


    }
}
