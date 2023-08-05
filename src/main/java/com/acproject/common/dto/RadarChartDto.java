package com.acproject.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RadarChartDto implements Serializable {
    private Integer[] value;
    private String name;
}
