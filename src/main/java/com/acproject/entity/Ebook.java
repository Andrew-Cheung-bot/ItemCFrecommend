package com.acproject.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)
public class Ebook {
    /**
     * 主键,网址里面提取的https://read.douban.com/ebook/958945/
     * eg:958945
     */
    @TableId(type = IdType.INPUT)
    private String eid;
    /**
     * ISBN号
     * eg:9787229042066
     */
    private String isbn;
    /**
     * 书名
     * eg:三体全集
     */
    private String ename;
    /**
     * 作者
     * eg:刘慈欣
     */
    private String author;
    /**
     * 译者(可为空)
     * eg:
     */
    private String translator;
    /**
     * 图书类别
     * eg:图书 / 虚构
     */
    private String category;
    /**
     * 出版社
     * eg:重庆出版社 / 2012-01
     */
    private String publishinghouse;
    /**
     * 提供方
     * eg:云雷文化
     */
    private String provider;
    /**
     * 字数
     * eg:867000
     */
    private Integer words;
    /**
     * 图书封面图片地址
     * eg:https://img3.doubanio.com/view/ark_article_cover/retina/public/958945.jpg?v=1418894099.0
     */
    private String imgaddress;
    /**
     * 豆瓣图书详情页地址
     * eg:https://read.douban.com/ebook/958945/
     */
    private String infoaddress;
    /**
     * 评分列表地址
     * eg:https://book.douban.com/subject/6518605/collections
     */
    private String scoreaddress;
    /**
     * 导言
     * eg:第73届雨果奖最佳长篇故事获奖作品。三体全集 (《三体》《三体Ⅱ·黑暗森林》《三体Ⅲ·死神永生》) ，原名“地球往事三部曲”，是中国著名科幻作家刘慈欣的首个长篇系列，由科幻世界杂志社策划制作，重庆出版集团出版。
     小说讲述了文革期间一次偶然的星际通讯引发的三体世界对地球的入侵以及之后人类文明与三体文明三百多年的恩怨情仇。三体三部曲出版后十分畅销，并深受读者和主流媒体好评，被普遍认为是中国科幻文学的里程碑之作，为中国科幻确立了一个新高度。
     刘慈欣，祖籍河南，长于山西，中国科普作家协会会员，山西省作家协会会员，高级工程师。
     自1999年处女作《鲸歌》问世以来，发表短篇科幻小说三十余篇，出版长篇科幻小说七部，并创下连续八年荣获中国科幻最高奖“银河奖”的纪录。其气势恢宏的“地球往事”系列第一部《三体》开创了《科幻世界》月刊连载原创作品之先河，一举成为2006年度最受关注、最畅销的科幻小说。2008年，“地球往事”系列第二部《黑暗森林》面世，再次成为当年最畅销的科幻小说。2010年，“地球往事”系列第三部《死神永生》出版，一举拿下该年度中国科幻“银河奖”特别奖、全球华语科幻“星云奖”最佳长篇奖。刘慈欣亦因此被评论界誉为“21世纪中国文坛最值得关注的作家”。2015年8月凭借科幻小说《三体》获最佳长篇故事奖，这是亚洲人首次获得雨果奖，也是中国科幻小说第一次真正上升到世界的高度。
     刘慈欣的作品恢弘大气、想象绚丽，既注意极端空灵与厚重现实的结合，也讲求科学的内涵与人文的美感，具有浓郁的中国特色和鲜明的个人风格，为中国科幻确立了一个新高度。
     */
    private String description;
    /**
     * 10分好评数
     */
    @TableField(exist = false)
    private Integer bestrating;
    /**
     * 0分差评数
     */
    @TableField(exist = false)
    private Integer worstrating;
    /**
     * 评分
     */
    private Double ratingvalue;
    /**
     * 参与评分人数
     */
    private Integer reviewcount;
    /**
     * 主分类
     * eg:小说
     */
    private String classifymain;
}
