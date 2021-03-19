package demo.security.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * url和对应的请求方法实体
 *
 */
@Slf4j
@Data
public class UrlAndMethod {
    private String url;
    private String method;
}
