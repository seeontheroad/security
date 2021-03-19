package demo.security.service;

import demo.security.entity.Admin;
import demo.security.mapper.UserMapper;
import demo.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Value("${tokenHead}")
    private String tokenHead;
    @Override
    public Map<String, Object> login(String username, String password) {
        //登录
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Map<String,Object> map = new HashMap<>();
        if(userDetails == null ||passwordEncoder.matches(passwordEncoder.encode(password),userDetails.getPassword())){
            map.put("msg","用户名密码不正确");
            return map;
        }
        if(userDetails.isEnabled()){
            map.put("msg","账号被禁用");
            return map;
        }

        //登录成功后放入全文中 更新security登录用户对象
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        System.out.println( SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        //生成token
        String token = jwtTokenUtil.generateToken(userDetails);
        Map<String,String> data = new HashMap<>();
        data.put("username", userDetails.getUsername());
        data.put("token",token);
        data.put("tokenHead",tokenHead);
        map.put("data",data);
        map.put("msg","登录成功");
        map.put("code","0");
        return map;
    }

    @Override
    public Admin findByName(String username) {
        return userMapper.findByName(username);
    }
}
