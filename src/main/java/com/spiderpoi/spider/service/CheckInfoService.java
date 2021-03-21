package com.spiderpoi.spider.service;

import com.spiderpoi.spider.dto.CityModel;
import com.spiderpoi.spider.dto.Shop;
import com.spiderpoi.spider.dto.SingleCityParam;
import com.spiderpoi.spider.dto.SingleProvinceParam;

import java.util.List;

public interface CheckInfoService {

    /***
     * 通过关键字查找全国的所有商店
     * @param keyword
     * @return
     */
    List<Shop> getShopInfoInNationwide(String keyword);

    /***
     * 通过关键字查找省份中的所有商店
     * @param param
     * @return
     */
    List<Shop> findAllShopInProvinceByKeyword(SingleProvinceParam param);

    /***
     * 通过关键字查找城市中的所有商店
     * @param param
     * @return
     */
    List<Shop> findAllShopInCityByKeyword(SingleCityParam param);

    /***
     * 查找所有城市信息
     * @return
     */
    List<CityModel> findAllCities();
}
