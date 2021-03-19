package demo.security.config;

import demo.security.filter.JwtTokenFilter;
import demo.security.handler.MyAccessDeniedHandler;
import demo.security.handler.MyAuthenticationEntryPoint;
import demo.security.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    UserMapper userMapper;
    @Autowired
    MyAccessDeniedHandler myAccessDeniedHandler;

    @Autowired
    MyAuthenticationEntryPoint myAuthenticationEntryPoint;

    @Autowired
    DynamicPermission dynamicPermission;

    @Override
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            //查用户的基本信息
            log.info(username);
            return userMapper.findByName(username);
        };
    }

    @Bean
    public PasswordEncoder getPw() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtTokenFilter getJwtTokenFilter() {
        return new JwtTokenFilter();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //使用jwt 不需要csrf
        http.csrf().disable()
                //基于token 不需要session
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                //允许登录
                .antMatchers("/", "/login")
                .permitAll()
                .antMatchers("/swagger*//**").permitAll()
                .antMatchers( "/swagger-ui.html",
                        "/swagger-ui/*",
                        "/swagger-resources/**",
                        "/v2/api-docs",
                        "/v3/api-docs",
                        "/webjars/**").permitAll()
                //除了登录,其他拦截 ,动态加载资源
                .anyRequest()
                .access("@dynamicPermission.checkPermission(request,authentication)")
                .and()
                //不需要缓存
                .headers()
                .cacheControl();
//        添加jwt登录授权拦截器
        http.addFilterBefore(getJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        //添加未登录结果返回
        http.exceptionHandling().authenticationEntryPoint(myAuthenticationEntryPoint);
//        添加未授权结果返回
        http.exceptionHandling().accessDeniedHandler(myAccessDeniedHandler);
    }

    //认证
    //spring security 5.0+ 中 不能直接使用密码 需要进行加密
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(getPw());
    }


}
