package com.acproject.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
public class Classifymainstatistics {
    @TableId(type = IdType.INPUT)
    private String classifymain;
    private Integer reviewcount5;
    private Integer reviewcount4;
    private Integer reviewcount3;
    private Integer reviewcount2;
    private Integer reviewcount1;
    private Double varianceratingvalue;
    private Double avgratingvalue;
}
