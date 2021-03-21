package com.spiderpoi.spider.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CityModel implements Serializable {

    private Integer id;

    private String cities;

    private String province;
}
