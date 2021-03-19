package demo.security.controller;

import demo.security.entity.Admin;
import demo.security.entity.LoginUser;
import demo.security.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;
@Api(tags = "用户相关接口")
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @ApiOperation("获取登录人个人信息")
    @PostMapping("/user/info")
    public Admin getUser(Principal principal) {
        if (null == principal) {
            return null;
        }
        String username = principal.getName();
        return userService.findByName(username);
    }

    @ApiOperation("登录")
    @PostMapping("/login")
    public Map<String, Object> login( LoginUser loginUser) {
        return userService.login(loginUser.getUsername(), loginUser.getPassword());
    }

}
