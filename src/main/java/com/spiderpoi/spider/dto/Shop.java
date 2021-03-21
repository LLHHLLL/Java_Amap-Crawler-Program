package com.spiderpoi.spider.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class Shop implements Serializable {

    //店铺id
    private String id;

    //省份或直辖市
    private String pname;

    //城市
    private String cityname;

    //县级市或县级
    private String adname;

    //具体位置
    private String specificAddress;

    //店铺名字
    private String shopName;

    //店铺电话
    private String telePhone;

    //店铺类型
    private String shopType;

    //经度
    private String longitude;

    //纬度
    private String latitude;

}
