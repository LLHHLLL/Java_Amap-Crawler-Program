package com.spiderpoi.spider.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CityProvinceDto implements Serializable {

    //城市
    private String city;

    //省份
    private String province;
}