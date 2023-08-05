package com.acproject.service.impl;

import com.acproject.entity.Favorite;
import com.acproject.mapper.FavoriteMapper;
import com.acproject.service.FavoriteService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper,Favorite> implements FavoriteService {
}
