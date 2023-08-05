package com.acproject.shiro;

import cn.hutool.core.bean.BeanUtil;
import com.acproject.entity.Roles;
import com.acproject.entity.User;
import com.acproject.service.RolesService;
import com.acproject.service.UserService;
import com.acproject.util.JwtUtils;
import com.acproject.websocket.WebSocketServer;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.jsonwebtoken.Claims;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountRealm extends AuthorizingRealm {

    private static Logger log = LoggerFactory.getLogger(AccountRealm.class);

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserService userService;

    @Autowired
    RolesService rolesService;

    //验证token是否为jwtToken
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        //强转为JwtToken
        JwtToken jwtToken = (JwtToken) token;
        //根据Token获取UserID
        String userId = jwtUtils.getClaimByToken((String) jwtToken.getPrincipal()).getSubject();
        //System.out.println("======获取userId为:"+userId+"======");
        User user = userService.getById(userId);
        if (user == null) {
            throw new UnknownAccountException("账户不存在");
        }
        if(user.getStatus().equals("N")){
            throw new LockedAccountException("账户未激活");
        }

        //System.out.println("======通过认证生成凭证(AccountProfile)======");
        //用户凭证(只含有用户Id，注册邮箱，用户名)
        AccountProfile profile = new AccountProfile();
        BeanUtil.copyProperties(user, profile);

        return new SimpleAuthenticationInfo(profile, jwtToken.getCredentials(), getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        log.info("进入授权方法");
        AccountProfile accountProfile = (AccountProfile) principals.getPrimaryPrincipal();
        log.info("进入授权方法的用户信息："+accountProfile);
        QueryWrapper<Roles> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid",accountProfile.getUid());
        List<Roles> rolesList = rolesService.list(queryWrapper);
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        for(Roles temp : rolesList){
            log.info("添加的用户角色："+temp.getRole());
            simpleAuthorizationInfo.addRole(temp.getRole());
        }
        return simpleAuthorizationInfo;
    }
}
