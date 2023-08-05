package com.acproject.controller;

import com.acproject.common.dto.UpdateBookDto;
import com.acproject.common.lang.Result;
import com.acproject.entity.*;
import com.acproject.mapper.EbookMapper;
import com.acproject.mapper.MatrixCMapper;
import com.acproject.mapper.SubclassifyMapper;
import com.acproject.service.EbookService;
import com.acproject.service.FavoriteService;
import com.acproject.service.RatingListService;
import com.acproject.util.RandomNumUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/book")
public class EbookController {

    @Autowired
    EbookService ebookService;

    @Autowired
    FavoriteService favoriteService;

    @Autowired
    RatingListService ratingListService;

    @Autowired
    EbookMapper ebookMapper;

    @Autowired
    MatrixCMapper matrixCMapper;

    @Autowired
    SubclassifyMapper subclassifyMapper;

    /**
     * 获取图书详情页
     * @param eid
     * @return
     */
    @GetMapping("/getinfo")
    public Result getInfo(@RequestParam(value = "eid") String eid){
        Ebook ebook = ebookService.getById(eid);
        ebook = initEBookImgAddress(ebook);
        return Result.success(ebook);
    }

    /**
     * 首页轮播推荐书籍接口
     * @param eid
     * @return
     */
    @GetMapping("/recommendhomepage")
    public Result recommendHomePage(@RequestParam(value = "eid") String eid){
        Ebook ebook = ebookService.getById(eid);
        ebook = initEBookImgAddress(ebook);
        return Result.success(ebook);
    }

    /**
     *
     * @param classifyMain
     * @param subclassify
     * @return
     */
    @GetMapping("/queryfamouslist")
    public Result queryfamouslist(@RequestParam(value = "classifyMain") String classifyMain,
                                   @RequestParam(value = "subclassify") String subclassify){
        QueryWrapper<Ebook> ebookQueryWrapper = new QueryWrapper<>();
        QueryWrapper<Subclassify> subclassifyQueryWrapper = new QueryWrapper<>();
        subclassifyQueryWrapper.eq("classifySub",subclassify);
        List<Subclassify> subclassifyList = subclassifyMapper.selectList(subclassifyQueryWrapper);
        ebookQueryWrapper.eq("classifyMain",classifyMain);
        ebookQueryWrapper.ge("reviewCount",10);
        ebookQueryWrapper.ge("ratingValue",8);
        ebookQueryWrapper.and(ebookQueryWrapper1 -> {
            for(Subclassify temp : subclassifyList){
                ebookQueryWrapper1.eq("eid",temp.getEid()).or();
            }
        });

        //参数1:当前页 默认值为1 (传过来的第一个参数就是当前页,已经在底层计算好了)
        //参数2:每页显示记录数 默认值为10
        IPage<Ebook> Ipage = new Page<>(1,5);
        IPage<Ebook> userIPage = ebookMapper.selectPage(Ipage,ebookQueryWrapper);
        List<Ebook> list = initEBookImgAddress(userIPage.getRecords());
        userIPage.setRecords(list);

        return Result.success(userIPage);
    }

    /**
     * 按书类型分类列出电子书
     * @param classifyMain
     * @param page
     * @param orderCondition //eg:按照评分order
     * @param order
     * @return
     */
    @GetMapping(value = "/listbycfm",produces = "application/json;charset=UTF-8")
    public Result queryBookLimitByClassifyMain(
            @RequestParam(value = "classifyMain") String classifyMain,
            @RequestParam(value = "subclassify") String subclassify,
            @RequestParam(value = "page") String page,
            @RequestParam(value = "orderCondition", required = false) String orderCondition,
            @RequestParam(value = "order", required = false) String order
            ){
        QueryWrapper<Ebook> ebookQueryWrapper = new QueryWrapper<>();
        QueryWrapper<Subclassify> subclassifyQueryWrapper = new QueryWrapper<>();
        subclassifyQueryWrapper.eq("classifySub",subclassify);
        List<Subclassify> subclassifyList = subclassifyMapper.selectList(subclassifyQueryWrapper);
        ebookQueryWrapper.eq("classifyMain",classifyMain);
        ebookQueryWrapper.and(ebookQueryWrapper1 -> {
            for(Subclassify temp : subclassifyList){
                ebookQueryWrapper1.eq("eid",temp.getEid()).or();
            }
        });

        if(order == null){

        }else if(order.equals("DESC")) ebookQueryWrapper.orderBy(true,false,orderCondition);
        else if(order.equals("ASC")) ebookQueryWrapper.orderBy(true,true,orderCondition);

        //参数1:当前页 默认值为1 (传过来的第一个参数就是当前页,已经在底层计算好了)
        //参数2:每页显示记录数 默认值为10
        IPage<Ebook> Ipage = new Page<>(Integer.valueOf(page),10);
        IPage<Ebook> userIPage = ebookMapper.selectPage(Ipage,ebookQueryWrapper);
        List<Ebook> list = initEBookImgAddress(userIPage.getRecords());
        userIPage.setRecords(list);

        return Result.success(userIPage);
    }

    /**
     * 根据uid随机从喜好分类里推荐书籍
     * @param orderCondition //eg:按照评分order
     * @param order
     * @param uid
     * @return
     */
    @GetMapping(value = "/getlistbyuid",produces = "application/json;charset=UTF-8")
    public Result getBookLimitByClassifyMain(
            @RequestParam(value = "orderCondition", required = false) String orderCondition,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "uid") String uid){
        //按照用户感兴趣的分类来随机返回图书
        //通过uid获取用户感兴趣分类
        QueryWrapper<Favorite> favoriteQueryWrapper = new QueryWrapper();
        favoriteQueryWrapper.eq("uid",uid);
        List<Favorite> favoriteList = favoriteService.list(favoriteQueryWrapper);
        try {
            String choose_classifymain = favoriteList.get(RandomNumUtil.randomNum(0, favoriteList.size() - 1)).getClassifymain();
            //随机选中一个感兴趣分类后获取5本图书
            QueryWrapper<Ebook> ebookQueryWrapper = new QueryWrapper<>();
            ebookQueryWrapper.eq("classifyMain",choose_classifymain);
            int num = ebookService.count(ebookQueryWrapper)/10;

            if(order == null){

            }else if(order.equals("DESC")) ebookQueryWrapper.orderBy(true,false,orderCondition);
            else if(order.equals("ASC")) ebookQueryWrapper.orderBy(true,true,orderCondition);

            //参数1:当前页 默认值为1 (传过来的第一个参数就是当前页,已经在底层计算好了)
            //参数2:每页显示记录数 默认值为10
            IPage<Ebook> Ipage = new Page<>(RandomNumUtil.randomNum(1,num),10);
            IPage<Ebook> userIPage = ebookMapper.selectPage(Ipage,ebookQueryWrapper);
            List<Ebook> list = initEBookImgAddress(userIPage.getRecords());

            //获取随机图书
            List<Ebook> resultlist = new ArrayList<>();
            int[] randomNum = RandomNumUtil.randomCommon(0,10,5);
            for(int i=0;i<randomNum.length;i++){
                resultlist.add(list.get(randomNum[i]));
            }
            userIPage.setRecords(resultlist);

            return Result.success(userIPage);
        }catch (Exception e){
            return Result.success("用户没有喜爱分类");
        }
    }

    /**
     * 新书推荐接口(也就是推荐参与评分人数为0的书籍)
     * @param classifyMain
     * @param orderCondition
     * @param order
     * @return
     */
    @GetMapping(value = "coldbooksrecommend",produces = "application/json;charset=UTF-8")
    public Result coldBooksRecommend(
            @RequestParam(value = "classifyMain",required = false) String classifyMain,
            @RequestParam(value = "orderCondition",required = false) String orderCondition,
            @RequestParam(value = "order",required = false) String order
    ){
        String[] areaNameList = { "小说", "文学", "人文社科", "经济管理", "科技科普",
                "计算机与互联网", "成功励志", "生活", "少儿", "艺术设计", "漫画绘本", "教育考试", "杂志" };

        if(classifyMain == null){
            classifyMain = areaNameList[RandomNumUtil.randomNum(0,areaNameList.length-1)];
        }
        QueryWrapper<Ebook> queryWrapper = new QueryWrapper();
        queryWrapper.eq("classifyMain",classifyMain);
        queryWrapper.eq("reviewCount",0);
        int num = ebookService.count(queryWrapper)/10;
        if(order == null){

        }else if(order.equals("DESC")) queryWrapper.orderBy(true,false,orderCondition);
        else if(order.equals("ASC")) queryWrapper.orderBy(true,true,orderCondition);

        //参数1:当前页 默认值为1 (传过来的第一个参数就是当前页,已经在底层计算好了)
        //参数2:每页显示记录数 默认值为10
        IPage<Ebook> Ipage = new Page<>(RandomNumUtil.randomNum(1,num),10);
        IPage<Ebook> userIPage = ebookMapper.selectPage(Ipage,queryWrapper);
        List<Ebook> list = initEBookImgAddress(userIPage.getRecords());

        //获取随机图书
        List<Ebook> resultlist = new ArrayList<>();
        int[] randomNum = RandomNumUtil.randomCommon(0,10,5);
        for(int i=0;i<randomNum.length;i++){
            resultlist.add(list.get(randomNum[i]));
        }
        userIPage.setRecords(resultlist);

        return Result.success(userIPage);
    }

    /**
     * 根据关键词搜索接口(或条件)
     * @param keyword
     * @param page
     * @param orderCondition
     * @param order
     * @return
     */
    @GetMapping(value = "/searchkeyword",produces = "application/json;charset=UTF-8")
    public Result queryBookByKeyword(
            @RequestParam(value = "keyword") String keyword,
            @RequestParam(value = "page") String page,
            @RequestParam(value = "orderCondition", required = false) String orderCondition,
            @RequestParam(value = "order", required = false) String order
            ){
        QueryWrapper<Ebook> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("ename",keyword).or().
                like("author",keyword).or().
                like("publishingHouse",keyword).or().
                like("provider",keyword);

        if(order == null){

        }else if(order.equals("DESC")) queryWrapper.orderBy(true,false,orderCondition);
        else if(order.equals("ASC")) queryWrapper.orderBy(true,true,orderCondition);

        //参数1:当前页 默认值为1 (传过来的第一个参数就是当前页,已经在底层计算好了)
        //参数2:每页显示记录数 默认值为10
        IPage<Ebook> Ipage = new Page<>(Integer.valueOf(page),10);
        IPage<Ebook> userIPage = ebookMapper.selectPage(Ipage,queryWrapper);
        List<Ebook> list = initEBookImgAddress(userIPage.getRecords());
        userIPage.setRecords(list);

        return Result.success(userIPage);
    }

    /**
     * 图书高级搜索接口(且)
     * @param eid
     * @param ISBN
     * @param ename
     * @param author
     * @param translator
     * @param publishingHouse
     * @param provider
     * @param classifyMain
     * @param orderCondition
     * @param order
     * @return
     */
    @GetMapping(value = "/search",produces = "application/json;charset=UTF-8")
    public Result queryBookByCondition(
            @RequestParam(value="eid", required=false)String eid,
            @RequestParam(value="ISBN", required=false)String ISBN,
            @RequestParam(value="ename", required=false)String ename,
            @RequestParam(value="author", required=false)String author,
            @RequestParam(value="translator", required=false)String translator,
            @RequestParam(value="publishingHouse", required=false)String publishingHouse,
            @RequestParam(value="provider", required=false)String provider,
            @RequestParam(value="classifyMain", required=false)String classifyMain,
            @RequestParam(value="orderCondition", required=false)String orderCondition,
            @RequestParam(value="order", required=false)String order,
            @RequestParam(value = "page") String page
        ){
        QueryWrapper<Ebook> queryWrapper = new QueryWrapper<>();
        if(eid != null) queryWrapper.eq("eid",eid);
        if(ISBN != null) queryWrapper.eq("ISBN",ISBN);
        if(ename != null) queryWrapper.like("ename",ename);
        if(author != null) queryWrapper.like("author",author);
        if(translator != null) queryWrapper.like("translator",translator);
        if(publishingHouse != null) queryWrapper.like("publishingHouse",publishingHouse);
        if(provider != null) queryWrapper.like("provider",provider);
        if(classifyMain != null) queryWrapper.like("classifyMain",classifyMain);

        if(order == null){

        }else if(order.equals("DESC")) queryWrapper.orderBy(true,false,orderCondition);
        else if(order.equals("ASC")) queryWrapper.orderBy(true,true,orderCondition);

        //参数1:当前页 默认值为1 (传过来的第一个参数就是当前页,已经在底层计算好了)
        //参数2:每页显示记录数 默认值为10
        IPage<Ebook> Ipage = new Page<>(Integer.valueOf(page),10);
        IPage<Ebook> userIPage = ebookMapper.selectPage(Ipage,queryWrapper);
        List<Ebook> list = initEBookImgAddress(userIPage.getRecords());
        userIPage.setRecords(list);

        return Result.success(userIPage);
    }

    /**
     * 根据用户行为与物品相似度推荐图书接口
     * @param uid
     * @return
     */
    @GetMapping("/recommendbook")
    public Result recommendbook(@RequestParam(value = "uid")String uid){
        //根据uid获取用户喜爱图书
        QueryWrapper<Ratinglist> ratinglistQueryWrapper = new QueryWrapper<>();
        ratinglistQueryWrapper.eq("uid",uid);
        ratinglistQueryWrapper.ge("ratingValue",4);
        List<Ratinglist> likelist = ratingListService.list(ratinglistQueryWrapper);

        //定义计算矩阵
        //矩阵的列——>用户喜爱的图书
        //矩阵的行——>同现的图书
        List<Item> matrix = new ArrayList<>();

        for(int i=0;i<likelist.size();i++){
            Ratinglist temp = likelist.get(i);
            //获取共现的图书
            QueryWrapper<Matrixc> matrixcQueryWrapper = new QueryWrapper<>();
            matrixcQueryWrapper.eq("eida",temp.getEid()).or().eq("eidb",temp.getEid());
            List<Matrixc> itemlist = matrixCMapper.selectList(matrixcQueryWrapper);
            for(int j=0;j<itemlist.size();j++){
                //提取共现的图书
                Matrixc temp_book = itemlist.get(j);
                String seid = null;
                if(temp_book.getEida().equals(temp.getEid())){
                    seid = temp_book.getEidb();
                }else{
                    seid = temp_book.getEida();
                }
                //在行中查询是否存在共现图书
                if(matrix.indexOf(seid) == -1){ //若不存在
                    double[] col = new double[likelist.size()];
                    //共现图书所在行对应的数据设为对应的余弦相似度*用户评分喜好程度（4——>1，5——>2）
                    col[likelist.indexOf(temp)] = temp_book.getCossimilarity()*(temp.getRatingvalue()-3);
                    matrix.add(new Item(seid,col));
                }else{
                    matrix.get(matrix.indexOf(seid)).col[likelist.indexOf(temp)] = temp_book.getCossimilarity()*(temp.getRatingvalue()-3);
                }
            }
        }

        //计算预测兴趣度
        for(int i=0;i<matrix.size();i++){
            Item item = matrix.get(i);
            double interestValue = 0;
            for(int j=0;j<item.col.length;j++){
                interestValue += item.col[j];
            }
            matrix.get(i).setInterestValue(interestValue);
        }


        //依据兴趣度从高到低排序
        Collections.sort(matrix);
        for(int i=0;i<matrix.size();i++){
            System.out.println(matrix.get(i));
        }



        //返回前10本相关度最高的书
        List<Ebook> recommendbook = new ArrayList<>();
        if(matrix.size()<10){
            for(int i=0;i<matrix.size();i++){
                String book_id = matrix.get(i).getEid();
                recommendbook.add(ebookService.getById(book_id));
            }
            initEBookImgAddress(recommendbook);
        }else{
            for(int i=0;i<10;i++){
                String book_id = matrix.get(i).getEid();
                recommendbook.add(ebookService.getById(book_id));
            }
            initEBookImgAddress(recommendbook);
        }


        return Result.success(recommendbook);
    }



    /**
     * 相似图书推荐接口
     * @param eid
     * @return
     */
    @GetMapping("/similaritybook")
    public Result similarityBooks(@RequestParam(value = "eid") String eid){
        QueryWrapper<Matrixc> matrixcQueryWrapper = new QueryWrapper<>();
        matrixcQueryWrapper.eq("eida",eid).or().eq("eidb",eid);
        List<Matrixc> matrixcList = matrixCMapper.selectList(matrixcQueryWrapper);

        if(!matrixcList.isEmpty()){
            //根据相似度进行排序
            Collections.sort(matrixcList);

            //选出前10的图书
            List<Matrixc> similaritybook_matrix = null;
            System.out.println(matrixcList.size());
            if(matrixcList.size()>5){
                similaritybook_matrix =  matrixcList.subList(0,5);
            }else{
                similaritybook_matrix = matrixcList.subList(0,matrixcList.size());
            }

            //将共现的图书id提取出来
            List<Ebook> ebookList = new ArrayList<>();
            for(Matrixc temp:similaritybook_matrix){
                if(temp.getEida().equals(eid)){
                    ebookList.add(ebookService.getById(temp.getEidb()));
                }else{
                    ebookList.add(ebookService.getById(temp.getEida()));
                }
            }
            initEBookImgAddress(ebookList);
            return Result.success(ebookList);
        }

        return Result.success("没有相似的图书");
    }

    /**
     *
     */
    @PostMapping("/updatebook")
    public Result getnewbookimg(@RequestBody UpdateBookDto updateBookDto){
        Ebook ebook = ebookService.getById(updateBookDto.getEid());
        ebook.setEname(updateBookDto.getEname());
        ebook.setAuthor(updateBookDto.getAuthor());
        ebook.setClassifymain(updateBookDto.getClassifyMain());
        ebook.setIsbn(updateBookDto.getIsbn());
        ebook.setRatingvalue(updateBookDto.getRatingvalue());
        ebook.setWords(updateBookDto.getWords());
        ebook.setProvider(updateBookDto.getProvider());
        ebook.setPublishinghouse(updateBookDto.getPublishinghouse());
        ebookService.updateById(ebook);
        return Result.success("修改成功");
    }

    /**
     * 新增书籍
     * @param eid
     * @param ename
     * @param isbn
     * @param author
     * @param translator
     * @param publishinghouse
     * @param provider
     * @param classifyMain
     * @param subClassify
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/getnewbookimg")
    public Result getnewbookimg(@RequestParam("eid") String eid,
                                @RequestParam("ename") String ename,
                                @RequestParam("isbn") String isbn,
                                @RequestParam("author") String author,
                                @RequestParam("translator") String translator,
                                @RequestParam("publishinghouse") String publishinghouse,
                                @RequestParam("provider") String provider,
                                @RequestParam("classifyMain") String classifyMain,
                                @RequestParam("subClassify") String subClassify,
                                @RequestParam("description") String description,
                                @RequestParam("picture") MultipartFile file) throws IOException {
        String filename = eid + ".jpg";
        String path = "J:\\finaltest\\EMANImgs";
        File dest = new File(new File(path).getAbsolutePath()+ "/" + filename);
        System.out.println(dest.getAbsolutePath());
        Ebook newbook = new Ebook();
        newbook.setEid(eid).setEname(ename).setIsbn(isbn).setAuthor(author).setTranslator(translator).setPublishinghouse(publishinghouse).setProvider(provider)
                .setClassifymain(classifyMain).setDescription(description).setImgaddress("public/"+ filename +"?v=");
        ebookService.save(newbook);

        Subclassify subclassify = new Subclassify();
        subclassify.setClassifysub(subClassify);
        subclassify.setEid(eid);
        System.out.println("新增书籍副分类测试");
        subclassifyMapper.insert(subclassify);

        //保存图片至nginx服务器
        file.transferTo(dest);
        return Result.success("上传图片成功");
    }

    /**
     * 根据书籍Id删除电子书，并删除图书封面
     * @param eid
     * @return
     */
    @GetMapping("/deletebook")
    public Result deletebook(@RequestParam("eid") String eid){
        QueryWrapper<Subclassify> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("eid",eid);
        ebookService.removeById(eid);
        subclassifyMapper.delete(queryWrapper);
        String filename = eid + ".jpg";
        String path = "J:\\finaltest\\EMANImgs";
        File dest = new File(new File(path).getAbsolutePath()+ "/" + filename);
        FileSystemUtils.deleteRecursively(dest);
        return Result.success("删除成功");
    }





    /**
     * 处理图书的图片地址为可用
     * @param list
     * @return
     */
    public static List<Ebook> initEBookImgAddress(List<Ebook> list){
        for(Ebook book : list){
            if(book.getImgaddress()!=null){
                int beg = book.getImgaddress().indexOf("public/");
                int end = book.getImgaddress().indexOf("?v=");
                if(end==-1)
                    continue;
                String tString = book.getImgaddress().substring(beg + "public/".length(), end);
                book.setImgaddress(tString);
            }

        }
        return list;
    }

    /**
     * 处理图书的图片地址为可用
     * @param book
     * @return https://img3.doubanio.com/view/ark_article_cover/retina/public/10000464.jpg?v=1432873975.0
     */
    public static Ebook initEBookImgAddress(Ebook book){
        if(book.getImgaddress()!=null){
            int beg = book.getImgaddress().indexOf("public/");
            int end = book.getImgaddress().indexOf("?v=");
            if(end==-1){

            }else{
                String tString = book.getImgaddress().substring(beg + "public/".length(), end);
                book.setImgaddress(tString);
            }
        }
        return book;
    }
}
