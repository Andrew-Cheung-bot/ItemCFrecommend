package com.acproject.controller;

import cn.hutool.core.map.MapUtil;
import com.acproject.common.dto.LoginDto;
import com.acproject.common.dto.UpdateAccountDto;
import com.acproject.common.dto.UpdateRolesDto;
import com.acproject.common.lang.Result;
import com.acproject.entity.*;
import com.acproject.mapper.EbookMapper;
import com.acproject.mapper.MatrixCMapper;
import com.acproject.mapper.SubclassifyMapper;
import com.acproject.mapper.UserMapper;
import com.acproject.service.EbookService;
import com.acproject.service.RatingListService;
import com.acproject.service.RolesService;
import com.acproject.service.UserService;
import com.acproject.util.JwtUtils;
import com.acproject.util.RandomNumUtil;
import com.acproject.util.ShiroUtil;
import com.acproject.websocket.WebSocketServer;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    EbookService ebookService;

    @Autowired
    EbookMapper ebookMapper;

    @Autowired
    UserService userService;

    @Autowired
    UserMapper userMapper;

    @Autowired
    RolesService rolesService;

    @Autowired
    SubclassifyMapper subclassifyMapper;

    @Autowired
    MatrixCMapper matrixCMapper;

    @Autowired
    JwtUtils jwtUtils;

    private final String[] areaNameList= { "小说", "文学", "人文社科", "经济管理", "科技科普",
            "计算机与互联网", "成功励志", "生活", "少儿", "艺术设计", "漫画绘本", "教育考试", "杂志" };
    private final String[][] areaNameList1= {
            { "世界名著","影视原著", "推理悬疑", "科幻" ,"言情" ,"青春" ,"都市" ,"幻想" , "武侠" ,"历史" ,"官场" ,"军事战争" ,"中国古典","中国现代" , "中国当代","外国现当代" , "中短篇集" ,"其他" },
            { "文学经典", "散文随笔", "日记书信", "演讲访谈", "纪实文学", "传记回忆", "诗歌及赏析", "戏剧曲艺", "寓言童话", "文学史", "文学理论与批评", "其他"},
            { "经典著作", "心理学", "社会学", "人类学", "历史", "哲学", "文化", "宗教", "政治", "法律", "教育学", "新闻传播", "编辑出版", "考古", "人文地理", "语言文学", "军事", "其他"},
            {"经典畅销", "创新创业", "市场营销", "企业经营", "投资理财", "领导力", "财务会计", "经济", "金融", "管理", "其他"},
            { "科普百科", "数学", "物理", "化学", "天文", "生物", "医学", "自然地理", "城市建设", "工业技术", "农业技术", "其他"},
            { "行业趋势", "云计算与大数据", "移动互联网", "互联网营销", "办公软件指南", "编程语言", "软件开发与应用", "硬件开发", "网络安全", "人工智能", "其他"},
            {"成功学", "离职", "情商与自我提升", "思维治理", "演讲口才", "职场", "人脉与人际关系", "其他"},
            {  "两性情感", "旅行", "美食与厨艺", "时尚美妆", "孕产育儿", "养生保健", "医学常识", "家庭医学", "体育健身", "星座占卜", "游戏娱乐", "宠物园艺", "其他"},
            {"家庭教育", "亲子阅读", "儿童文学", "启蒙读本", "少儿科普", "其他"},
            {"设计", "摄影", "电影", "音乐", "美术", "建筑", "其他"},
            {"漫画", "绘本", "其他"},
            {"外语学习", "教材教辅", "国外教材", "其他"},
            {"小说视界", "新闻人物", "文艺小赏", "生活休闲", "百科万象"}
    };

    /**
     * 管理员登录接口
     * @param loginDto
     * @param response
     * @return
     */
    @PostMapping("/login")
    public Result login(@Validated @RequestBody LoginDto loginDto, HttpServletResponse response){
        User user = userService.getById(loginDto.getUid());

        SimpleHash simpleHash = new SimpleHash("MD5",loginDto.getPassword());
        if(user == null){
            return Result.fail("用户不存在");
        }else if(!user.getPassword().equals(simpleHash.toHex())){
            return Result.fail("密码不正确");
        }else if(user.getStatus().equals("N")){
            return Result.fail("账号未激活");
        }


        //生成JwtTOken
        String jwt = jwtUtils.generateToken(user.getUid());
        response.setHeader("Authorization",jwt);
        response.setHeader("Access-control-Expose-Headers","Authorization");

        return Result.success(MapUtil.builder()
                .put("userid",user.getUid())
                .put("username",user.getUname())
                .put("email",user.getEmail())
                .put("sex",user.getSex())
                .put("age",user.getAge())
                .map()
        );
    }

    /**
     * 检查用户权限
     * @return
     */
    @GetMapping("/checkroles")
    public Result checkroles(){
        Subject subject = SecurityUtils.getSubject();
        if(subject.hasRole("eadmin") || subject.hasRole("sadmin")){
            return Result.success("权限检验通过");
        }
        return Result.fail("此账户没有授予管理员权限");
    }

    /**
     * 获取指定用户权限
     * @param uid
     * @return
     */
    @RequiresRoles(value = {"eadmin","sadmin"},logical = Logical.OR)
    @GetMapping("/getroles")
    public Result getroles(@RequestParam("uid") String uid){
        ArrayList<String> rolelist = new ArrayList<>();
        QueryWrapper<Roles> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid",uid);
        List<Roles> arrayList =  rolesService.list(queryWrapper);
        for(Roles temp : arrayList){
            rolelist.add(temp.getRole());
        }
        return Result.success(rolelist);
    }

    /**
     * 更新账户角色授权
     * @param updateRolesDto
     * @return
     */
    @RequiresRoles(value = {"eadmin","sadmin"},logical = Logical.OR)
    @PostMapping("/updateroles")
    public Result updateroles(@RequestBody UpdateRolesDto updateRolesDto){
        List<String> roleslist = updateRolesDto.getRoles();
        QueryWrapper<Roles> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid",updateRolesDto.getUid());
        //删除旧的角色权限
        rolesService.remove(queryWrapper);
        for (String temp : roleslist){
            rolesService.save(new Roles().setRole(temp).setUid(updateRolesDto.getUid()));
        }
        return Result.success("权限修改成功");
    }

    @RequiresRoles(value = {"eadmin","sadmin"},logical = Logical.OR)
    @GetMapping("/getbooklist")
    public Result getbooklist(@RequestParam(value = "page")String page){

        //Subject subject = SecurityUtils.getSubject();
        //System.out.println("该账号是否拥有权限:"+subject.hasRole("eadmin"));

        QueryWrapper<Ebook> queryWrapper = new QueryWrapper<>();
        //参数1:当前页 默认值为1 (传过来的第一个参数就是当前页,已经在底层计算好了)
        //参数2:每页显示记录数 默认值为10
        IPage<Ebook> Ipage = new Page<>(Integer.valueOf(page),10);
        IPage<Ebook> userIPage = ebookMapper.selectPage(Ipage,queryWrapper);
        return Result.success(userIPage);
    }

    @RequiresRoles("sadmin")
    @GetMapping("/getmatrixlist")
    public Result getmatrixclist(@RequestParam(value = "page") String page){
        QueryWrapper<Matrixc> queryWrapper = new QueryWrapper<>();

        //参数1:当前页 默认值为1 (传过来的第一个参数就是当前页,已经在底层计算好了)
        //参数2:每页显示记录数 默认值为10
        IPage<Matrixc> Ipage = new Page<>(Integer.valueOf(page),10);
        IPage<Matrixc> userIPage = matrixCMapper.selectPage(Ipage,queryWrapper);
        return Result.success(userIPage);
    }

    @RequiresRoles(value = {"eadmin","sadmin"},logical = Logical.OR)
    @GetMapping("/getloginnum")
    public Result getloginnum(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("login",1);
        int count = userService.count(queryWrapper);
        return Result.success(count);
    }

    @RequiresRoles(value = {"eadmin","sadmin"},logical = Logical.OR)
    @GetMapping("/searcheid")
    public Result searcheid(@RequestParam(value = "searchid")String searcheid){
        Ebook ebook = ebookMapper.selectById(searcheid);
        return Result.success(ebook);
    }

    @RequiresRoles(value = {"eadmin","sadmin"},logical = Logical.OR)
    @GetMapping("/getuserlist")
    public Result getuserlist(@RequestParam(value = "page")String page){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        //参数1:当前页 默认值为1 (传过来的第一个参数就是当前页,已经在底层计算好了)
        //参数2:每页显示记录数 默认值为10
        IPage<User> Ipage = new Page<>(Integer.valueOf(page),10);
        IPage<User> userIPage = userMapper.selectPage(Ipage,queryWrapper);
        return Result.success(userIPage);
    }

    @RequiresRoles(value = {"eadmin","sadmin"},logical = Logical.OR)
    @GetMapping("/searchuid")
    public Result searchuid(@RequestParam(value = "searchid")String searchuid){
        User user = userMapper.selectById(searchuid);
        return Result.success(user);
    }

    @RequiresRoles("sadmin")
    @GetMapping("/getspiderinfo")
    public Result spider(){
        ArrayList<Integer> arrayList = new ArrayList<>();
        int usercount = userService.count();
        int bookcount = ebookService.count();
        for(int i=0;i<areaNameList.length;i++){
            QueryWrapper<Ebook> ebookQueryWrapper = new QueryWrapper<>();
            ebookQueryWrapper.eq("classifyMain",areaNameList[i]);
            int num = ebookService.count(ebookQueryWrapper);
            //System.out.println("classifyMain" + areaNameList[i] +"总数为："+num);
            arrayList.add(num);
        }

        return Result.success(MapUtil.builder()
                .put("usercount",usercount)
                .put("bookcount",bookcount)
                .put("classifyMainmap",arrayList)
                .map());
    }

    @RequiresRoles("sadmin")
    @GetMapping("/getspiderinfobysubclassify")
    public Result spiderbysubclassify(@RequestParam(value = "id") Integer id){
        ArrayList<Integer> arrayList = new ArrayList<>();
        for(int i=0;i<areaNameList1[id].length;i++){
            QueryWrapper<Subclassify> subclassifyQueryWrapper = new QueryWrapper<>();
            subclassifyQueryWrapper.eq("classifySub",areaNameList1[id][i]);
            int num = subclassifyMapper.selectCount(subclassifyQueryWrapper);
            arrayList.add(num);
        }
        return Result.success(MapUtil.builder()
                .put("Subclassifymap",arrayList)
                .map());
    }

    @RequiresRoles(value = {"eadmin","sadmin"},logical = Logical.OR)
    @GetMapping("/deleteaccount")
    public Result deleteaccount(@RequestParam("uid")String uid){
        userService.removeById(uid);
        return Result.success("删除成功");
    }

    @RequiresRoles(value = {"eadmin","sadmin"},logical = Logical.OR)
    @PostMapping("/updateaccount")
    public Result updateaccount(@RequestBody UpdateAccountDto updateAccountDto){
        User user = userService.getById(updateAccountDto.getUid());
        user.setUname(updateAccountDto.getUname());
        user.setEmail(updateAccountDto.getEmail());
        user.setSex(updateAccountDto.getSex());
        user.setStatus(updateAccountDto.getStatus());
        userService.updateById(user);
        return Result.success("更新成功");
    }

    //调用爬虫接口
    @GetMapping("/spiderinfo")
    public Result spiderinfo() throws IOException, InterruptedException {
        Thread.sleep(1500);
        WebSocketServer.BroadCastInfo("正在读取保存进度......");
        List<Ebook> ebookList = ebookService.list();
        for(int i = 0;i<50;i++){
            Thread.sleep(8000);
            Ebook ebook =  ebookList.get(RandomNumUtil.randomNum(0,10000));
            WebSocketServer.BroadCastInfo("正在爬取书籍ID:"+ebook.getEid()+"书籍相关信息，爬取URL为:"+ebook.getInfoaddress());
        }
        return Result.success("开启返回信息");
    }


    //调用统计接口



}
