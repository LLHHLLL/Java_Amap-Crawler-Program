package com.spiderpoi.spider.service;

import com.spiderpoi.spider.dto.Shop;
import com.spiderpoi.spider.dto.SingleCityParam;
import com.spiderpoi.spider.dto.SingleProvinceParam;

import java.util.List;

public interface ProvinceService {

    /***
     * 高德地图初始化接口
     * @param keyword 关键词
     * @param city 城市
     * @param listShop 店铺列表
     * @return
     */
    List<Shop> initialData(String keyword, String city, List<Shop> listShop);

    /***
     * 城市搜索
     * @param param
     */
    void singleCitySpiderInfo(SingleCityParam param);

    /***
     * 单个省份搜索
     * @param param
     */
    void singleprovinceInfo(SingleProvinceParam param);

    /***
     * 循环省份搜索
     * @param keyWord
     */
    void cycleProvinceInfo(String keyWord);
}
