package com.acproject.statistics;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.acproject.entity.Matrixc;
import com.acproject.entity.Ratinglist;
import com.acproject.entity.User;
import com.acproject.mapper.MatrixCMapper;
import com.acproject.mapper.RatingListMapper;
import com.acproject.mapper.UserMapper;
import com.acproject.service.MatrixCService;
import com.acproject.service.RatingListService;
import com.acproject.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 用于计算基于物品的协同过滤推荐矩阵
 */
public class ItemCollaborationFilter {

    /**
     * 设置日期格式
     */
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    UserMapper userMapper;

    @Autowired
    UserService userService;

    @Autowired
    RatingListService ratingListService;

    @Autowired
    MatrixCService matrixCService;

    @Autowired
    RatingListMapper ratingListMapper;

    @Autowired
    MatrixCMapper matrixCMapper;

    /**
     * mysql数据库连接
     */
    public static Connection conn = null;
    public static void initConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
        Class.forName("org.gjt.mm.mysql.Driver").newInstance();
        String url ="jdbc:mysql://localhost/acproject?user=root&password=root&useUnicode=true&characterEncoding=utf-8";
        //myDB为数据库名
        conn= DriverManager.getConnection(url);
        if(conn == null){
            System.out.println("创建数据库连接失败");
        }else{
            System.out.println("创建数据库连接成功" + conn);
        }
    }
    public static void closeConnection() throws SQLException{
        conn.close();
    }



    /**
     * 共现矩阵C：表示两个物品同时出现的次数
     * 例如：物品10000464和10000517同时出现1次
     * key:10000464,10000517(小的编号在左)
     * value:1
     */
    private Map<String,Integer> matrixC = new HashMap<>();
    /**
     * 共现矩阵C：表示两个物品同时出现的次数
     * 例如：物品10000464和10000517同时出现1次，并且10000464出现2次，10000517出现3次
     * key:10000464,10000517(小的编号在左)
     * value:1/Math.sqrt(2*3)约等于0.41
     * 这个0.41就是物品10000464和10000517的相似度
     */
    private Map<String,Double> matrixW = new HashMap<>();

    /**
     * 计算共现矩阵C
     */
    private void computerMatrixC(){
        // 建立用户物品倒排表
        // 若用户对物品评分大于等于4则认为喜欢(出现)
        List<User> allUser = userService.list();
        for(int i = 0; i < allUser.size(); i++){ // 遍历全部用户
            // 获取一个用户的评分列表中>=4的评分记录
            QueryWrapper<Ratinglist> ratinglistQueryWrapper = new QueryWrapper<>();
            ratinglistQueryWrapper.eq("uid",allUser.get(i).getUid()).and(ratinglistQueryWrapper1 -> ratinglistQueryWrapper1.ge("ratingValue",4));
            List<Ratinglist> likeList = ratingListService.list(ratinglistQueryWrapper);
            if(likeList.size() <= 1){ // 若用户只喜欢一本或不喜欢任何图书
                continue;
            }

//			System.out.print("{");
//			for(int j = 0; j < likeList.size(); j++){
//				System.out.print(likeList.get(j).getEid()+","); // debug
//			}
//			System.out.println("}");

            for(int j = 0; j < likeList.size(); j++){ // 计算likeList中两两出现的图书并写入同现矩阵C
                for(int k = j+1; k < likeList.size(); k++){
                    int a = Integer.valueOf(likeList.get(j).getEid());
                    int b = Integer.valueOf(likeList.get(k).getEid());
                    // 生成key
                    String key = null;
                    if(a < b){
                        key = a + "," + b;
                    }else{
                        key = b + "," + a;
                    }
//					System.out.println("key:"+key); // debug

                    // 检查key是否已经存在
                    if(this.matrixC.get(key) != null){
                        int value = this.matrixC.get(key);
                        this.matrixC.put(key, value+1);
//						System.out.println("allUser["+i+"]="+allUser.get(i).getUid()+",likeList["+j+"]="+a+"[k="+k+"]="+b+";"+this.matrixC.containsKey(key)+" key:"+key+",value:"+value+1); // debug
                    }else{
                        this.matrixC.put(key, 1);
//						System.out.println("allUser["+i+"]="+allUser.get(i).getUid()+",likeList["+j+"]="+a+"[k="+k+"]="+b+";"+this.matrixC.containsKey(key)+" key:"+key+",value:"+1); // debug
                    }
                }
            }
            System.out.println("["+df.format(new Date())+"]"+"[已完成"+i+",共"+allUser.size()+"]:用户uid="+allUser.get(i).getUid()+"的记录以计算完成,共"+likeList.size()+"本图书"); // debug
//			try {
//				Thread.sleep(1000); // debug
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
        }

    }

    /**
     * 将共现矩阵C写入MySQL数据库
     * @throws SQLException
     */
    private void writeMatrixCToDB() throws SQLException{
        Iterator it = this.matrixC.entrySet().iterator();
        while (it.hasNext()) { // 遍历同现矩阵C
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String)entry.getKey();
            Integer value = (Integer)entry.getValue();
            // System.out.println("key=" + key + " value=" + value);
            String[] eidList = key.split(",");
            // 异常数据处理
            if(eidList[0] == null || eidList[1] == null){
                continue;
            }
            if(value == null){
                value = 0;
            }
            // 查询数据库该数据是否已经存在
            QueryWrapper<Matrixc> matrixcQueryWrapper = new QueryWrapper<>();
            matrixcQueryWrapper.eq("eida",eidList[0]).and(matrixcQueryWrapper1 -> matrixcQueryWrapper1.eq("eidb",eidList[1]));
            Matrixc c =  matrixCMapper.selectOne(matrixcQueryWrapper);
            if(c != null){ // 若存在则不插入(只更新)
                QueryWrapper<Matrixc> matrixcQueryWrapper1 = new QueryWrapper<>();
                matrixcQueryWrapper1.eq("eida",eidList[0]).and(matrixcQueryWrapper2 -> matrixcQueryWrapper2.eq("eidb",eidList[1]));
                Matrixc temp = matrixCMapper.selectOne(matrixcQueryWrapper1);
                temp.setCounter(value);
                matrixCMapper.update(temp,matrixcQueryWrapper1);
                System.out.println("update:[" + eidList[0] + "],[" + eidList[1] + "]:count=" + c.getCounter() + "->" + value);
//				continue;
            }else{
                // 写入数据库
                c = new Matrixc();
                c.setCounter(value);
                matrixCMapper.insert(new Matrixc().setEida(eidList[0]).setEidb(eidList[1]).setCounter(value));
                System.out.println("insert:[" + eidList[0] + "],[" + eidList[1] + "]:count=" + c.getCounter() + "->" + value);
                //System.out.println(query); // debug
            }

        }

    }

    /**
     * 计算余弦相似度矩阵W
     * 计算方法：
     * 使用矩阵C的每个value作为分子，key中的两个图书的喜欢人数的积开根号作为分母
     */
    private Double computerMatrixW(String eida, String eidb, int value){
        DecimalFormat df = new DecimalFormat("#.##");
        // 查询每个图书有多少人喜欢
        try {
            Statement statemenet = conn.createStatement();
            ResultSet rs = statemenet.executeQuery("select count(rid) from ratinglist where eid = '"+ eida +"' and ratingValue >= 4;");
            rs.next();
            int likeANum = rs.getInt("count(rid)");
            rs = statemenet.executeQuery("select count(rid) from ratinglist where eid = '"+ eidb +"' and ratingValue >= 4;");
            rs.next();
            int likeBNum = rs.getInt("count(rid)");
//		int likeANum = this.ratingListDao.countRatingListByEidAndUserLike(eida);
//		int likeBNum = this.ratingListDao.countRatingListByEidAndUserLike(eidb);
            if(likeANum == 0)
                likeANum = 1;
            if(likeBNum == 0)
                likeBNum = 1;
            // 开始计算
            Double answer = value*1.0/Math.sqrt(likeANum*likeBNum);
            // 精确到小数点后两位
            Double result = Double.parseDouble(df.format(answer));
            // 返回计算结果
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return null;
    }

    /**
     * 将计算出来的余弦相似度矩阵W保存到矩阵C的cos_similarity属性中
     */
    private void computerAndWriteMatrixW(){
        List<Matrixc> matrixCList = matrixCService.list();
        System.out.println("["+this.df.format(new Date())+"]"+"开始余弦相似度矩阵W的计算,需要计算"+matrixCList.size()+"条");
        for(int i = 0; i < matrixCList.size(); i++){
            Matrixc c = matrixCList.get(i);
            if(c.getCossimilarity() == null){
                Double cos_similarity = this.computerMatrixW(c.getEida(), c.getEidb(), c.getCounter());
                QueryWrapper<Matrixc> matrixcQueryWrapper1 = new QueryWrapper<>();
                matrixcQueryWrapper1.eq("eida",c.getEida()).and(matrixcQueryWrapper2 -> matrixcQueryWrapper2.eq("eidb",c.getEidb()));
                Matrixc temp = matrixCMapper.selectOne(matrixcQueryWrapper1);
                temp.setCossimilarity(cos_similarity);
                matrixCMapper.update(temp,matrixcQueryWrapper1);
            }

        }
        System.out.println("["+this.df.format(new Date())+"]"+"完成余弦相似度矩阵W的计算,共计算"+matrixCList.size()+"条,写入数据库完毕");
    }

    //--------------------------------------------------------------------------------------

    /**
     * 计算同现矩阵C并写入数据库
     * @throws SQLException
     */
    public void computerAndWriteMtrixC() throws SQLException{
        System.out.println("["+this.df.format(new Date())+"]"+"开始计算同现矩阵C");
        this.computerMatrixC();
        System.out.println("["+this.df.format(new Date())+"]"+"开始将同现矩阵C写入数据库");
        this.writeMatrixCToDB();
        System.out.println("["+this.df.format(new Date())+"]"+"同现矩阵C写入数据库完毕");
    }


    public static void main(String[] args){
        PrintStream ps;
        try {
            // 重定向标准输出到文件方便查看
            ps = new PrintStream(new FileOutputStream("output-ItemCollaborationFilter"));
            System.setOut(ps);

            ItemCollaborationFilter icf = new ItemCollaborationFilter();
            ItemCollaborationFilter.initConnection();

            //计算同现矩阵C
//			try {
//				icf.computerAndWriteMtrixC();
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

            //计算余弦相似度矩阵W
            icf.computerAndWriteMatrixW();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

//        ItemCollaborationFilter icf = new ItemCollaborationFilter();
//        icf.matrixC.put("15232623,16834057",11);
//        try {
//            icf.writeMatrixCToDB();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

    }




}