package com.acproject;

import com.acproject.entity.Matrixc;
import com.acproject.mapper.MatrixCMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@SpringBootTest
class AcProjectApplicationTests {

    @Autowired
    MatrixCMapper matrixCMapper;

    @Test
    @RequestMapping("/admin/test")
    public void contextLoads() {
        /*
        QueryWrapper<Matrixc> queryWrapper= new QueryWrapper<>();
        queryWrapper.eq("cossimilarity",0.06);
        List<Matrixc> t = matrixCMapper.selectList(queryWrapper);
        */
        System.out.println("test");
    }

}
