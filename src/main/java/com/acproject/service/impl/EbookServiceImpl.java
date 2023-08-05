package com.acproject.service.impl;

import com.acproject.entity.Ebook;
import com.acproject.mapper.EbookMapper;
import com.acproject.service.EbookService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class EbookServiceImpl extends ServiceImpl<EbookMapper,Ebook> implements EbookService {

}
