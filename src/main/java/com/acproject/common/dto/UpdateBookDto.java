package com.acproject.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateBookDto implements Serializable {
    private String ename;
    private String author;
    private String eid;
    private String classifyMain;
    private String isbn;
    private Double ratingvalue;
    private Integer words;
    private String provider;
    private String publishinghouse;
}
