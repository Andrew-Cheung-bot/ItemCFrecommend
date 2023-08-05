package com.acproject.service.impl;

import com.acproject.entity.Ratinglist;
import com.acproject.entity.User;
import com.acproject.mapper.RatingListMapper;
import com.acproject.mapper.UserMapper;
import com.acproject.service.RatingListService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingListServiceImpl extends ServiceImpl<RatingListMapper, Ratinglist> implements RatingListService {
}
