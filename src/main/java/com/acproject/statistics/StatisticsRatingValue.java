package com.acproject.statistics;

import java.util.List;

import com.acproject.entity.Ebook;
import com.acproject.entity.Ratinglist;
import com.acproject.service.EbookService;
import com.acproject.service.RatingListService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/*
 * 图书评分信息统计：统计RatingValue表的数据并将统计结果写入到EBook表对应到图书上
 */
@Repository("statisticsRatingValue")
public class StatisticsRatingValue {

    @Autowired
    RatingListService ratingListService;

    @Autowired
    EbookService ebookService;
    /**
     * 获得用户评分的平均数(0~5)
     * @param eid
     * @return 精确到小数点后一位
     */
    public double statisticsRatingValue(String eid){
        QueryWrapper<Ratinglist> ratinglistQueryWrapper = new QueryWrapper<>();
        ratinglistQueryWrapper.eq("eid",eid);
        List<Ratinglist> list = ratingListService.list(ratinglistQueryWrapper);
        double sum = 0;
        for(Ratinglist r : list){
            sum += r.getRatingvalue();
        }
        return (double)(Math.round((sum / list.size())*10)/10.0);
    }

    /**
     * 批量修改图书评分与评分人数
     * @return 成功修改的数量
     */
    private int updateRatingValueAndReviewCount(){
        int num = 0;
        // 从数据库获取所有图书
        List<Ebook> eBookList = ebookService.list();
        System.out.println("共读取了" + eBookList.size() + "本书");
        for(int i = 0, j = 0; i < eBookList.size(); i++, j++){
            Ebook book = eBookList.get(i);
            QueryWrapper<Ratinglist> ratinglistQueryWrapper = new QueryWrapper<>();
            ratinglistQueryWrapper.eq("eid",book.getEid());
            List<Ratinglist> list = ratingListService.list(ratinglistQueryWrapper);
            double sum = 0;
            double avg = 0;
            for(Ratinglist r : list){
                sum += r.getRatingvalue();
            }
            if(list.size() >= 0){
                avg = 2.0*(double)(Math.round((sum / list.size())*10)/10.0);
                QueryWrapper<Ebook> ebookQueryWrapper = new QueryWrapper<>();
                ebookQueryWrapper.eq("eid",book.getEid());
                Ebook temp = ebookService.getOne(ebookQueryWrapper);
                temp.setRatingvalue(avg);
                temp.setReviewcount(list.size());
                if(ebookService.update(temp,ebookQueryWrapper)){
                    num++;
                }
            }
            // 进度指示器
            if(j == 20){
                j = 0;
                System.out.println("统计完成了" + (i+1) + "/" + eBookList.size() + ",完成百分比=" + ((i+1)*1.0/eBookList.size()) );
            }
        }
        return num;
    }

    public static void main(String[] args){
        StatisticsRatingValue statisticsRatingValue = new StatisticsRatingValue();

//		System.out.println(statisticsRatingValue.statisticsRatingValue("930946"));

        int sucessNum = statisticsRatingValue.updateRatingValueAndReviewCount();
        System.out.println("成功修改：" + sucessNum + "条数据");
    }
}