package com.spiderpoi.spider.dao;

import com.spiderpoi.spider.dto.CityModel;
import com.spiderpoi.spider.dto.CityProvinceDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface WebCrawlerDao {

    /***
     * 根据省份来查询城市
     * @param province
     * @return
     */
    CityProvinceDto singleProvinceQuery(@Param("province") String province);

    /***
     * 循环省份查询
     * @return
     */
    List<CityProvinceDto> cycleProvinceQuery();

    /***
     * 根据省份查询
     * @param province
     * @return
     */
    CityModel selectCityModelByProvince(@Param("province")String province);

    /***
     * 根据城市名字查询出省份
     * @param cityName
     * @return
     */
    String selectProvinceByCityName(@Param("cityName") String cityName);

    /***
     * 查出所有城市信息
     * @return
     */
    List<CityModel> findAllCities();
}
