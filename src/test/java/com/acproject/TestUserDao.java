package com.acproject;

import com.acproject.entity.User;
import com.acproject.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class TestUserDao {

    @Autowired
    private UserMapper userMapper;

    //查询所有
    @Test
    public void testFindAll(){
        List<User> users = userMapper.selectList(null);
        users.forEach(System.out::println);
    }

    //根据主键查询一个
    @Test
    public void testFindById(){
        User user = userMapper.selectById("Thomation");
        System.out.println("user" + user);
    }

    //条件查询
    @Test
    public void testFind(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //查询所有昵称为 浅浅 的用户
        queryWrapper.eq("uname","浅浅");
        //eg 等于
        //lt 小于     gt 大于
        //le 小于等于  ge 大于等于
        List<User> users = userMapper.selectList(queryWrapper);
        users.forEach(System.out::println);
    }

    //模糊查询
    @Test
    public void testFindLike(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //like %?% left %?  (其中?指的关键词,%指其他字符)
        queryWrapper.like("uid","testaccount");
        List<User> users = userMapper.selectList(queryWrapper);
        users.forEach(System.out::println);
    }

    //保存
    @Test
    public void testSave(){
        User entity = new User();
        entity.setUname("Iamrobot").setSex("male").setPassword("123456").setAge(18).setUid("testaccount");
        userMapper.insert(entity);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uname","Iamrobot3");
        List<User> users = userMapper.selectList(queryWrapper);
        users.forEach(System.out::println);
    }

    //修改
    //基于主键id进行数据的修改
    @Test
    public void testUpdateById(){
        User user = userMapper.selectById("testaccount");
        user.setUname("Iamrobot2");
        userMapper.updateById(user);
    }

    //批量修改
    //基于条件的修改
    @Test
    public void testUpdate(){
        User user = new User();
        user.setUname("Iamrobot4");
        //批量修改性别为 male(男) 的昵称为Iamrobot3
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sex","male");
        userMapper.update(user,queryWrapper);
    }

    //基于id删除一个
    @Test
    public void testDeleteById(){
        userMapper.deleteById("testaccount");
    }

    //基于条件进行删除
    @Test
    public void testDelete(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sex","male");
        userMapper.delete(queryWrapper);
    }

    //分页查询使用
    @Test
    public void testFindPage(){
        //参数1:当前页 默认值为1 (传过来的第一个参数就是当前页,已经在底层计算好了)
        //参数2:每页显示记录数 默认值为10
        IPage<User> page = new Page<>(2,10);
        IPage<User> userIPage = userMapper.selectPage(page,null);
        long total = userIPage.getTotal();
        System.out.println("总记录数: " + total);
        userIPage.getRecords().forEach(System.out::println);
    }

    //分页 条件 查询使用
    @Test
    public void testFindPageBycondition(){

        //通过设置条件Wrapper来实现条件查询,该实例中实现查询年龄为23的数据进行分页
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("age",23);

        //参数1:当前页 默认值为1 (传过来的第一个参数就是当前页,已经在底层计算好了)
        //参数2:每页显示记录数 默认值为10
        IPage<User> page = new Page<>(2,10);

        IPage<User> userIPage = userMapper.selectPage(page,queryWrapper);
        long total = userIPage.getTotal();
        System.out.println("总记录数: " + total);
        userIPage.getRecords().forEach(System.out::println);
    }

}
