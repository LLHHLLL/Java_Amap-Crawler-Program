package com.spiderpoi.spider.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SingleCityParam implements Serializable {

    //城市
    private String city;

    //关键词
    private String keyword;
}
