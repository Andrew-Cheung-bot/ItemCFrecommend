package com.acproject.controller;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.map.MapUtil;
import com.acproject.common.dto.*;
import com.acproject.common.lang.Result;
import com.acproject.entity.Roles;
import com.acproject.entity.User;
import com.acproject.service.RolesService;
import com.acproject.service.UserService;
import com.acproject.shiro.AccountProfile;
import com.acproject.shiro.JwtFilter;
import com.acproject.util.JwtUtils;
import com.acproject.util.MailUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.management.relation.Role;
import javax.servlet.http.HttpServletResponse;

/**
 * @Api - 描述当前类型生成帮助文档的信息
 * 属性
 *   -tags : 给当前类型定义别名,可以有多个,定义几个别名,在视图中就显示几个控制器访问菜单
 *  (已经过时) -description : 给当前类型生成的帮助文档定义一个描述信息
 *
 * @ApiOperation -  用来描述具体某个方法的操作信息以及注释
 * 属性
 *  (必须) -value : 方法操作信息
 *   -notes : 方法注释描述
 *
 * @ApiParam - (在参数前)用来描述方法中的参数
 * 属性
 *  -name : 参数名
 *  -value : 参数描述
 *  (Boolean) -required : 参数是否是必要的
 *
 *  @ApiIgnore - 用来忽略当前注释的方法或类型,不生成Api帮助文档
 *
 * @ApiImplicitParam - (在方法上)用来描述方法中的参数
 * 属性
 *  -name : 参数名
 *  -value : 参数描述
 *  (Boolean) -required : 参数是否是必要的
 *  -paramType : 参数是什么类型
 *  -dataType : 数据是什么类型
 *
 */

@RestController
@RequestMapping("/account")
public class AccountController {

    private static Logger log = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    UserService userService;

    @Autowired
    RolesService rolesService;

    @Autowired
    MailUtil mailUtil;

    @Autowired
    JwtUtils jwtUtils;


    /**
     * 登录接口
     * @param loginDto
     * @param response
     * @return
     */
    @PostMapping("/login")
    public Result login(@Validated @RequestBody LoginDto loginDto, HttpServletResponse response){
        User user = userService.getById(loginDto.getUid());

        SimpleHash simpleHash = new SimpleHash("MD5",loginDto.getPassword());
        if(user == null){
            return Result.fail("用户不存在");
        }else if(!user.getPassword().equals(simpleHash.toHex())){
            return Result.fail("密码不正确");
        }else if(user.getStatus().equals("N")){
            return Result.fail("账号未激活");
        }

        String jwt = jwtUtils.generateToken(user.getUid());
        response.setHeader("Authorization",jwt);
        response.setHeader("Access-control-Expose-Headers","Authorization");

        return Result.success(MapUtil.builder()
                .put("userid",user.getUid())
                .put("username",user.getUname())
                .put("email",user.getEmail())
                .put("sex",user.getSex())
                .put("age",user.getAge())
                .map()
        );
    }

    /**
     * 检查用户权限
     * @return
     */
    @GetMapping("/checkroles")
    public Result checkroles(){
        Subject subject = SecurityUtils.getSubject();
        if(subject.hasRole("user")){
            AccountProfile accountProfile = (AccountProfile)subject.getPrincipal();
            User user = userService.getById(accountProfile.getUid());

            //修改登录状态字段为1(即为正在线上)
            user.setLogin(1);
            userService.updateById(user);

            return Result.success("权限检验通过");
        }
        return Result.fail("此账户没有授予用户权限");
    }

    /**
     * 注册接口
     * @param registDto
     * @return
     */
    @PostMapping("/regist")
    public Result regist(@Validated @RequestBody RegistDto registDto){
        User check = userService.getById(registDto.getUid());
        if(check != null){
            return Result.fail("用户ID已存在");
        }

        User user = new User();
        user.setUname(registDto.getUname());
        user.setEmail(registDto.getEmail());
        user.setAge(registDto.getAge());
        user.setSex(registDto.getSex());

        //Hash+MD5加密密码
        SimpleHash simpleHash = new SimpleHash("MD5",registDto.getPassword());
        user.setPassword(simpleHash.toString());
        user.setUid(registDto.getUid());

        //设置账户激活码以及激活状态为否
        user.setStatus("N");
        user.setCode(UUID.randomUUID().toString().replace("-",""));
        userService.save(user);

        //给账户授予权限
        Roles role = new Roles();
        role.setUid(user.getUid());
        role.setRole("user");
        rolesService.save(role);

        String content="<a href='http://127.0.0.1:8080/account/checkcode?code="+user.getCode()+"'>点击激活【ACProject账号】</a><br/>";
        content+="或复制链接到浏览器打开:http://127.0.0.1:8080/account/checkcode?code="+user.getCode();


        mailUtil.sendSimpleMail(registDto.getEmail(),content);
        return Result.success("注册成功");
    }

    /**
     * 用户激活接口
     * @param code
     * @return
     */
    @GetMapping("/checkcode")
    public Result checkcode(@RequestParam(value = "code") String code){
        QueryWrapper<User> queryWrapper = new QueryWrapper();
        queryWrapper.eq("code",code);
        User user = userService.getOne(queryWrapper);
        if(user == null){
            return Result.fail("用户不存在");
        }else{
            user.setStatus("Y");
            userService.update(user,queryWrapper.eq("code",code));
            return Result.success("用户激活成功");
        }
    }

    /**
     * 用户忘记密码接口
     * @param changePwdDto
     * @return
     */
    //SendEmail
    @PostMapping("/changepassword")
    public Result changepassword(@Validated @RequestBody ChangePwdDto changePwdDto){
        User user = userService.getById(changePwdDto.getUid());
        if(user == null){
            return Result.fail("用户不存在!");
        }else if(!user.getEmail().equals(changePwdDto.getEmail())){
            return Result.fail("注册邮箱不正确");
        }

        String content="<a href='http://localhost:8000/#/changepwd?code="+user.getCode()+"'>点击修改邮箱旗下:"+user.getUid()+"账号的密码【ACProject账号】</a><br/>";
        content+="或复制链接到浏览器打开:http://localhost:8000/#/changepwd?code="+user.getCode();
        mailUtil.sendSimpleMail(user.getEmail(),content);

        return  Result.success("已经发修改邮件");
    }

    /**
     * 用户写入新密码接口
     * @param newPwdDto
     * @param code
     * @return
     */
    @PostMapping("/change")
    public Result change(@Validated @RequestBody NewPwdDto newPwdDto,@RequestParam(value = "code",required = true) String code){
        QueryWrapper<User> queryWrapper = new QueryWrapper();
        queryWrapper.eq("code",code);
        User user = userService.getOne(queryWrapper);

        //Hash+MD5加密密码
        SimpleHash simpleHash = new SimpleHash("MD5",newPwdDto.getPassword());
        user.setPassword(simpleHash.toString());

        userService.update(user,queryWrapper);

        return Result.success("修改成功");
    }

    /**
     * 登录用户修改密码
     * @param changeNewPwdDto
     * @return
     */
    @PostMapping("/changenewpwd")
    public Result changenewpwd(@RequestBody ChangeNewPwdDto changeNewPwdDto){
        User user =  userService.getById(changeNewPwdDto.getUid());

        //Hash+MD5加密密码
        SimpleHash simpleHash = new SimpleHash("MD5",changeNewPwdDto.getPass());
        user.setPassword(simpleHash.toString());

        user.setAge(changeNewPwdDto.getAge());
        user.setSex(changeNewPwdDto.getSex());

        userService.updateById(user);

        return  Result.success("信息修改成功");
    }

    /**
     * 获取用户个人信息
     * @param uid
     * @return
     */
    @GetMapping("/getuserinfo")
    public Result getuserinfo(@RequestParam("uid") String uid){
        User user = userService.getById(uid);

        Integer age = user.getAge();
        String sex =  user.getSex();

        return Result.success(MapUtil.builder()
                                .put("age",age)
                                .put("sex",sex)
                                .map());
    }

    /**
     * 用户登出接口
     * @return
     */
    @RequiresAuthentication
    @GetMapping("/loginout")
    public Result LoginOut(){
        log.info("登出Subject(即UserId)为："+SecurityUtils.getSubject().getPrincipal());
        SecurityUtils.getSubject().logout();
        return Result.success(null);
    }

    @GetMapping("/signloginout")
    public Result signLoginOut(@RequestParam("uid") String uid){
        User user = userService.getById(uid);
        //修改登录状态字段为1(即为正在线上)
        user.setLogin(0);
        userService.updateById(user);
        return Result.success("下线成功");
    }
}
