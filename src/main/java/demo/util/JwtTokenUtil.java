package demo.util;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import demo.security.config.MyAuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtTokenUtil {

    private static final String PRIVATE_KEY = "HiOp4*1+Gr2Az=-";
    /**
     * token 设置过期时间
     */
    public static final int CALENDAR_FIELD = Calendar.HOUR;
    public static final int CALENDAR_INTERVAL = 10;

    /**
     * 生成令牌
     *
     * @param map
     * @return
     */
    public String getToken(Map<String, String> map) {

        JWTCreator.Builder builder = JWT.create();
        map.forEach(builder::withClaim);

        builder.withExpiresAt(getDateOfTomorrow());

        return builder.sign(Algorithm.HMAC256(PRIVATE_KEY));
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, String> map = new HashMap<>();
        map.put("username", userDetails.getUsername());
        return getToken(map);
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) throws MyAuthenticationException {
        try {
            DecodedJWT verify = verify(token);
            return verify.getClaim("username").asString();
        }catch (TokenExpiredException e){
            throw new MyAuthenticationException("登录过期,请重新登录!");
        }

    }

    /**
     * 判断令牌是否过期
     *
     * @param token 令牌
     * @return 是否过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            DecodedJWT verify = verify(token);
            Date expiration = verify.getExpiresAt();
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("token verify error", e);
        }
        return true;
    }

    /**
     * 刷新令牌
     *
     * @param token 原令牌
     * @return 新令牌
     */
    public String refreshToken(String token) {
        DecodedJWT verify = verify(token);
        Map<String, Claim> claims = verify.getClaims();
        Map<String, String> dataMap = new HashMap<>();
        claims.forEach((key, value) -> dataMap.put(key, value.asString()));
        return getToken(dataMap);
    }

    /**
     * 验证令牌
     *
     * @param token 令牌
     * @return 是否有效
     */
    public Boolean validateToken(String token) throws MyAuthenticationException {
        try {
            DecodedJWT verify = verify(token);
            verify.getClaim("username").asString();
            return true;
        } catch (TokenExpiredException e) {
            log.error("token过期", e);
            throw new MyAuthenticationException("登录过期,请重新登录!");
        } catch (AlgorithmMismatchException e) {
            log.error("token算法不一致", e);
            throw new MyAuthenticationException("token算法不一致");
        } catch (Exception e) {
            log.error("无效签名", e);
            throw new MyAuthenticationException("无效签名");
        }
    }

    /**
     * 从数据声明生成令牌
     *
     * @param map 数据声明
     * @return 令牌
     */
    private String generateToken(Map<String, String> map) {
        JWTCreator.Builder builder = JWT.create();
        map.forEach(builder::withClaim);

        builder.withExpiresAt(getDateOfTomorrow());

        return builder.sign(Algorithm.HMAC256(PRIVATE_KEY));
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    private Map<String, Claim> getClaimsFromToken(String token) {
        DecodedJWT verify = verify(token);
        return verify.getClaims();
    }

    public DecodedJWT verify(String token) {
        return JWT.require(Algorithm.HMAC256(PRIVATE_KEY)).build().verify(token);
    }

    private Date getDateOfTomorrow() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(CALENDAR_FIELD, CALENDAR_INTERVAL);
        return calendar.getTime();
    }


}
