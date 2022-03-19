package com.ruyuan.careerplan.inventory.Jedis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
/**
 * @author zhonghuashishan
 */
@ConfigurationProperties(prefix = "ruyuan.jedis")
@Component
public class JedisConfig {

    private Integer maxTotal;

    private Integer maxIdle;

    private String password;

    private Integer minIdle;

    private List<String> redisAddrs;

    public Integer getMaxTotal() {
        return maxTotal;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMaxTotal(Integer maxTotal) {
        this.maxTotal = maxTotal;
    }

    public Integer getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(Integer maxIdle) {
        this.maxIdle = maxIdle;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    public List<String> getRedisAddrs() {
        return redisAddrs;
    }

    public void setRedisAddrs(List<String> redisAddrs) {
        this.redisAddrs = redisAddrs;
    }
}