package com.acproject.controller;

import com.acproject.common.lang.Result;
import com.acproject.entity.Favorite;
import com.acproject.entity.User;
import com.acproject.service.FavoriteService;
import com.acproject.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    FavoriteService favoriteService;

    @Autowired
    UserService userService;

    private final String[] areaNameList= { "小说", "文学", "人文社科", "经济管理", "科技科普",
            "计算机与互联网", "成功励志", "生活", "少儿", "艺术设计", "漫画绘本", "教育考试", "杂志" };

    /**
     * 新增用户喜好分类
     * @param uid
     * @param classifyMain
     * @return
     */
    @GetMapping("/add")
    public Result addfavorite(
            @RequestParam(value = "uid")String uid,
            @RequestParam(value = "classifyMain")String classifyMain){

        User user = userService.getById(uid);
        boolean exist = false;
        for(String name:areaNameList){
            if(name.equals(classifyMain)){
                exist = true;
                break;
            }
        }

        if(uid == null){
            return Result.fail("需要输入用户id参数");
        }else if(user==null){
            return Result.fail("用户id不存在");
        }else if(exist==false){
            return Result.fail("输入分类不存在");
        }

        QueryWrapper<Favorite> queryWrapper = new QueryWrapper();
        queryWrapper.eq("uid",uid);
        queryWrapper.eq("classifyMain",classifyMain);
        Favorite isfavorite = favoriteService.getOne(queryWrapper);
        if(isfavorite!= null){
            return Result.fail("该用户喜好分类已存在");
        }

        Favorite favorite =new Favorite();
        favorite.setUid(uid);
        favorite.setClassifymain(classifyMain);
        favoriteService.save(favorite);

        return Result.success("新增成功");
    }

    /**
     * 根据用户id查询用户喜爱的分类
     * @param uid
     * @return
     */
    @GetMapping("/query")
    public Result queryFavoriteByUid(@RequestParam(value = "uid")String uid){

        QueryWrapper<Favorite> queryWrapper = new QueryWrapper();
        queryWrapper.eq("uid",uid);
        List<Favorite> favoriteList = favoriteService.list(queryWrapper);

        return Result.success(favoriteList);
    }

    /**
     * 根据用户id删除指定喜好分类
     * @param uid
     * @param classifyMain
     * @return
     */
    @GetMapping("/delete")
    public Result deleteFavorite(
            @RequestParam(value = "uid")String uid,
            @RequestParam(value = "classifyMain")String classifyMain){

        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid",uid);
        queryWrapper.eq("classifyMain",classifyMain);
        favoriteService.remove(queryWrapper);


        return Result.success("删除成功");
    }


}
