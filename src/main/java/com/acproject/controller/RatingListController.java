package com.acproject.controller;

import com.acproject.common.dto.DescribeDto;
import com.acproject.common.dto.PieChartDto;
import com.acproject.common.dto.RadarChartDto;
import com.acproject.common.lang.Result;
import com.acproject.entity.Classifymainstatistics;
import com.acproject.entity.Ebook;
import com.acproject.entity.Ratinglist;
import com.acproject.mapper.RatingListMapper;
import com.acproject.mapper.UserMapper;
import com.acproject.service.ClassifyMainStatisticsService;
import com.acproject.service.EbookService;
import com.acproject.service.RatingListService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/ratinglist")
public class RatingListController {

    @Autowired
    RatingListService ratingListService;
    @Autowired
    EbookService ebookService;
    @Autowired
    ClassifyMainStatisticsService classifyMainStatisticsService;
    @Autowired
    RatingListMapper ratingListMapper;
    @Autowired
    UserMapper userMapper;

    /**
     * 根据eid获取用户评论接口
     * @param eid
     * @return
     */
    @GetMapping("/list")
    public Result list(
            @RequestParam(value = "eid")String eid,
            @RequestParam(value = "cpage")String cpage
        ){
        QueryWrapper<Ratinglist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("eid",eid);
        queryWrapper.orderByDesc("rdescribe");
        //参数1:当前页 默认值为1 (传过来的第一个参数就是当前页,已经在底层计算好了)
        //参数2:每页显示记录数 默认值为10
        IPage<Ratinglist> Ipage = new Page<>(Integer.valueOf(cpage),10);
        IPage<Ratinglist> ratinglistIPage = ratingListMapper.selectPage(Ipage,queryWrapper);
        List<Ratinglist> ratinglists = ratinglistIPage.getRecords();
        for(Ratinglist temp :ratinglists){
            String uid = temp.getUid();
            temp.setUser(userMapper.selectById(uid));
        }

        ratinglistIPage.setRecords(ratinglists);
        return Result.success(ratinglistIPage);
    }

    /**
     * 插入用户评论
     * @param describeDto
     * @return
     */
    @PostMapping("/insert")
    public Result insert(@RequestBody DescribeDto describeDto){
        Ratinglist ratinglist = new Ratinglist();
        QueryWrapper<Ratinglist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("eid",describeDto.getEid());
        queryWrapper.eq("uid",describeDto.getUid());

        Ratinglist isexistratinglist = ratingListService.getOne(queryWrapper);
        ratinglist.setRdescribe(describeDto.getUser_comment());
        ratinglist.setEid(describeDto.getEid());
        ratinglist.setRatingvalue(describeDto.getRate());
        ratinglist.setUid(describeDto.getUid());
        if(isexistratinglist == null) ratingListService.save(ratinglist);
        else ratingListService.update(ratinglist,queryWrapper);
        return Result.success("评论成功");
    }

    /**
     * 获取用户评论列表
     * @param uid
     * @return
     */
    @GetMapping("/listcomment")
    public Result listrecommend(@RequestParam(value = "uid")String uid){
        QueryWrapper<Ratinglist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid",uid);
        List<Ratinglist>  ratinglistList =ratingListService.list(queryWrapper);
        for(Ratinglist temp : ratinglistList){
            String eid = temp.getEid();
            temp.setEbook(ebookService.getById(eid));
        }
        return Result.success(ratinglistList);
    }

    /**
     * 删除指定评论
     * @param uid
     * @param eid
     * @return
     */
    @GetMapping("/deletecomment")
    public Result deleterecommend(@RequestParam(value = "uid")String uid,@RequestParam(value = "eid")String eid){
        QueryWrapper<Ratinglist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid",uid);
        queryWrapper.eq("eid",eid);
        ratingListService.remove(queryWrapper);

        return Result.success("删除成功");
    }

    /**
     * 南丁格尔图数据
     * @param eid
     * @return
     */
    @GetMapping("/piechart")
    public Result piechart(@RequestParam(value = "eid")String eid){
        List<PieChartDto> pieChartDto = new ArrayList<>();
        int[] countvalue = new int[]{0, 0, 0, 0, 0};
        String[] labels = {"较差","失望","一般","满意","惊喜"};
        QueryWrapper<Ratinglist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("eid",eid);
        List<Ratinglist> ratinglistList =  ratingListService.list(queryWrapper);
        for(Ratinglist temp : ratinglistList) {
            Integer value = temp.getRatingvalue().intValue();
            if (value == 5) {
                countvalue[4]++;
            } else if (value == 4) {
                countvalue[3]++;
            } else if (value == 3){
                countvalue[2]++;
            }else if (value == 2){
                countvalue[1]++;
            }else if (value == 1){
                countvalue[0]++;
            }
        }
        for(int i=0;i<5;i++) {
            PieChartDto temp = new PieChartDto();
            temp.setName(labels[i]);
            temp.setValue(countvalue[i]);
            pieChartDto.add(temp);
        }
        return Result.success(pieChartDto);
    }

    /**
     * 雷达图数据
     * @param eid
     * @return
     */
    @GetMapping("/radarchart")
    public Result radarchart(@RequestParam(value = "eid")String eid){
        List<RadarChartDto> radarChartDto = new ArrayList<>();
        //本书评分雷达图
        Integer[] countvalue = new Integer[]{0, 0, 0, 0, 0};
        QueryWrapper<Ratinglist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("eid",eid);
        List<Ratinglist> ratinglistList =  ratingListService.list(queryWrapper);
        for(Ratinglist temp : ratinglistList) {
            Integer value = temp.getRatingvalue().intValue();
            if (value == 5) {
                countvalue[4]++;
            } else if (value == 4) {
                countvalue[3]++;
            } else if (value == 3){
                countvalue[2]++;
            }else if (value == 2){
                countvalue[1]++;
            }else if (value == 1){
                countvalue[0]++;
            }
        }
        RadarChartDto thisbook = new RadarChartDto();
        thisbook.setName("本书各评分人数");
        thisbook.setValue(countvalue);
        radarChartDto.add(thisbook);

        //分类图书各评分均值
        Ebook book = ebookService.getById(eid);
        String book_classifymain = book.getClassifymain();
        QueryWrapper<Ebook> ebookQueryWrapper = new QueryWrapper<>();
        ebookQueryWrapper.eq("classifyMain",book_classifymain);
        //取总评分人数大于50的书籍纳入平均值分母计算
        ebookQueryWrapper.ge("reviewCount",50);
        int sum = ebookService.count(ebookQueryWrapper);

        Classifymainstatistics classifyMainStatistics = classifyMainStatisticsService.getById(book_classifymain);
        Integer avgReviewCount5 = classifyMainStatistics.getReviewcount5()/sum;
        Integer avgReviewCount4 = classifyMainStatistics.getReviewcount4()/sum;
        Integer avgReviewCount3 = classifyMainStatistics.getReviewcount3()/sum;
        Integer avgReviewCount2 = classifyMainStatistics.getReviewcount2()/sum;
        Integer avgReviewCount1 = classifyMainStatistics.getReviewcount1()/sum;



        Integer[] integers = new Integer[]{avgReviewCount1,avgReviewCount2,avgReviewCount3,avgReviewCount4,avgReviewCount5};
        RadarChartDto classifymain = new RadarChartDto();
        classifymain.setValue(integers);
        classifymain.setName("本分类中热门图书各评分人数均值");
        radarChartDto.add(classifymain);

        return Result.success(radarChartDto);
    }
}
