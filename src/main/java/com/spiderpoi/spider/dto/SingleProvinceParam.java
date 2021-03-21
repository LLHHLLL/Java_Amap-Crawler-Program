package com.spiderpoi.spider.dto;

import lombok.Data;

import java.io.Serializable;

/***
 * 关键词和省份 参数
 */
@Data
public class SingleProvinceParam implements Serializable {

    //搜索的关键词
    private String keywords;

    //省份
    private String province;
}
