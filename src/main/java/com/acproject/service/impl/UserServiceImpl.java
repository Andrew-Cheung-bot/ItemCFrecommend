package com.acproject.service.impl;

import com.acproject.entity.User;
import com.acproject.mapper.UserMapper;
import com.acproject.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
