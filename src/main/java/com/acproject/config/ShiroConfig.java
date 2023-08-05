package com.acproject.config;


import com.acproject.shiro.AccountRealm;
import com.acproject.shiro.JwtFilter;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * shiro启用注解拦截控制器
 */
@Configuration
public class ShiroConfig {

    @Autowired
    JwtFilter jwtFilter;

    @Bean
    public SessionManager sessionManager(RedisSessionDAO redisSessionDAO) {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();

        // inject redisSessionDAO
        sessionManager.setSessionDAO(redisSessionDAO);
        return sessionManager;
    }

    @Bean
    public DefaultWebSecurityManager securityManager(AccountRealm accountRealm,
                                                     SessionManager sessionManager,
                                                     RedisCacheManager redisCacheManager) {

        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager(accountRealm);

        //inject sessionManager
        securityManager.setSessionManager(sessionManager);

        // inject redisCacheManager
        //用于设置登出时的主键名，因为Redis+Shiro默认使用的id为主键，不修改会造成用户登出因为无法在Redis中找到指定数据而失败
        redisCacheManager.setPrincipalIdFieldName("uid");
        securityManager.setCacheManager(redisCacheManager);
        return securityManager;
    }

    /**
     * Shiro自定义的认证过滤链
     * @return
     */
    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();

        Map<String, String> filterMap = new LinkedHashMap<>();
        //即需要jwt认证方案通过才能进入，下一个方法(ShiroFilterFactoryBean)中生成自定义过滤方案的对象名
        filterMap.put("/**", "jwt");
        //anon表示匿名登录，无需jwt认证，以下是一些用于监控和调试的页面
        filterMap.put("/swagger-ui.html","anon");
        filterMap.put("/swagger/**","anon");
        filterMap.put("/webjars/**", "anon");
        filterMap.put("/swagger-resources/**","anon");
        filterMap.put("/v2/**","anon");
        filterMap.put("/druid/**","anon");

        chainDefinition.addPathDefinitions(filterMap);
        return chainDefinition;
    }

    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager,
                                                         ShiroFilterChainDefinition shiroFilterChainDefinition) {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        //放入自带的拦截器securityManager
        shiroFilter.setSecurityManager(securityManager);
        //注册自己的拦截方案
        Map<String, Filter> filters = new HashMap<>();
        filters.put("jwt", jwtFilter);
        shiroFilter.setFilters(filters);

        //生成过滤链
        Map<String, String> filterMap = shiroFilterChainDefinition.getFilterChainMap();

        //该filtermap在上一个方法(ShiroFilterChainDefinition)中可以修改拦截的地址
        shiroFilter.setFilterChainDefinitionMap(filterMap);
        return shiroFilter;
    }

}