package com.acproject.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
public class Matrixc implements Comparable<Matrixc>{
    @TableId(type = IdType.INPUT)
    private Integer cid;
    private String eida;
    private String eidb;
    private Integer counter;
    private Double cossimilarity;

    @Override
    public int compareTo(Matrixc o) {
        if(this.getCossimilarity() == null || o.getCossimilarity() == null){
            return this.counter - o.getCounter();
        }else{
            double value = o.getCossimilarity() - this.getCossimilarity();
            if (value > 0) {
                return 1;
            } else if (value < 0) {
                return -1;
            }else
                return 0;
        }
    }
}
