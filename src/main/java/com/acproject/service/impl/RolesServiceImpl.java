package com.acproject.service.impl;

import com.acproject.entity.Roles;
import com.acproject.mapper.RolesMapper;
import com.acproject.service.RolesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RolesServiceImpl extends ServiceImpl<RolesMapper, Roles> implements RolesService {
}
