package com.acproject.mapper;

import com.acproject.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository //代表Dao持久层
public interface UserMapper extends BaseMapper<User> {

}
