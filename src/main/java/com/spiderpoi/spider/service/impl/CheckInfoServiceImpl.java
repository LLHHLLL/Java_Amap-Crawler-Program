package com.spiderpoi.spider.service.impl;

import com.spiderpoi.common.exception.Asserts;
import com.spiderpoi.common.result.CodeMsg;
import com.spiderpoi.spider.dao.WebCrawlerDao;
import com.spiderpoi.spider.dto.*;
import com.spiderpoi.spider.service.CheckInfoService;
import com.spiderpoi.spider.service.ProvinceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CheckInfoServiceImpl implements CheckInfoService {

    @Autowired
    private WebCrawlerDao webCrawlerDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ProvinceService provinceService;


    /***
     * 通过关键字查找全国的所有商店
     * @param keyword
     * @return
     */
    @Override
    public List<Shop> getShopInfoInNationwide(String keyword) {
        //校验参数
        if (StringUtils.isEmpty(keyword) || "".equals(keyword)) {
            Asserts.fail(CodeMsg.BIND_ERROR.fillArgs("关键词为空，请重新输入！"));
        }
        //判断redis是否存在缓存
        String redisKey = "全国" + "_" + keyword;
        List shopList = redisTemplate.opsForList().range(redisKey, 0, -1);
        //如果不存在缓存
        if (shopList == null || shopList.size() == 0) {
            //sql查询省份
            List<CityProvinceDto> dtos = webCrawlerDao.cycleProvinceQuery();
            //循环省份
            for (CityProvinceDto dto : dtos) {
                //去掉台湾省,台湾省的数据时总是有问题,不是把添加全国的数据就是空,有需要可以去掉此处判断
                if (!"台湾省".equals(dto.getProvince())) {
                    //遍历省份查询属于该省份的城市名称
                    CityProvinceDto cityProvinceDto = webCrawlerDao.singleProvinceQuery(dto.getProvince());
                    //分割装换成数组,再遍历
                    String[] split = cityProvinceDto.getCity().split(",");
                    for (String cityName : split) {
                        //调用高德地图获取初始化信息
                        provinceService.initialData(keyword, cityName, shopList);
                        System.out.println("城市:" + cityName);
                    }
                    //存缓存(存在bug，没有设置失效时间)
                    //yml文件设置了超时时间,有需要可以去掉,不然最后会写入失败并且报错
                    redisTemplate.opsForList().rightPushAll(redisKey, shopList);
                }
            }
        }
        //控制台打印店铺数量
        System.out.println("数量:" + shopList.size());
        System.out.println("++++++ok++++++");
        return shopList;
    }

    /***
     * 通过关键字查找省份中的所有商店
     * @param param
     * @return
     */
    @Override
    public List<Shop> findAllShopInProvinceByKeyword(SingleProvinceParam param) {
        //取关键字(keywords, city城市名，可填：城市中文、中文全拼、citycode或adcode)
        String keywords = param.getKeywords();
        String province = param.getProvince();
        //校验参数
        if (StringUtils.isEmpty(keywords) || "".equals(keywords)) {
            Asserts.fail(CodeMsg.BIND_ERROR.fillArgs("关键词为空，请重新输入！"));
        }
        if (StringUtils.isEmpty(province) || "".equals(province)) {
            Asserts.fail(CodeMsg.BIND_ERROR.fillArgs("省份为空，请重新输入！"));
        }
        CityModel cityModel = webCrawlerDao.selectCityModelByProvince(province);
        if (cityModel == null) {
            Asserts.fail(CodeMsg.BIND_ERROR.fillArgs("省份有误,请重新输入!"));
        }
        //暂时去掉台湾省,台湾省的数据时总是有问题,不是把添加全国的数据就是输出空,有需要可以去掉此处判断
        if ("台湾省".equals(province)) {
            Asserts.fail(CodeMsg.BIND_ERROR.fillArgs("暂时不支持爬取台湾省的数据！"));
        }

        //判断redis是否存在缓存
        String redisKey = province + "_" + keywords;
        List shopList = redisTemplate.opsForList().range(redisKey, 0, -1);
        //如果不存在缓存
        if (shopList == null || shopList.size() == 0) {
            //sql查询数据库
            CityProvinceDto dto = webCrawlerDao.singleProvinceQuery(province);
            //分割装换成数组,再遍历
            String[] split = dto.getCity().split(",");
            for (String cityName : split) {
                //调用高德地图获取初始化信息(调用接口)
                provinceService.initialData(keywords, cityName, shopList);
                System.out.println("城市:" + cityName);
            }
            //存缓存(存在bug，没有设置失效时间）
            redisTemplate.opsForList().rightPushAll(redisKey, shopList);
            //控制台打印店铺数量
            System.out.println("数量:" + shopList.size());
        }else {
            //控制台打印店铺数量
            System.out.println("数量:" + shopList.size());
        }
        System.out.println("++++++ok++++++");
        return shopList;
    }

    /***
     * 通过关键字查找城市中的所有商店
     * @param param
     * @return
     */
    @Override
    public List<Shop> findAllShopInCityByKeyword(SingleCityParam param) {
        //校验参数
        String cityName = param.getCity();
        String keywords = param.getKeyword();
        if (StringUtils.isEmpty(cityName) || "".equals(cityName)) {
            Asserts.fail(CodeMsg.BIND_ERROR.fillArgs("城市为空，请重新输入！"));
        }
        if (StringUtils.isEmpty(keywords) || "".equals(keywords)) {
            Asserts.fail(CodeMsg.BIND_ERROR.fillArgs("关键词为空，请重新输入！"));
        }
        //判断是否存在该城市
        String province = webCrawlerDao.selectProvinceByCityName(cityName);
        if (StringUtils.isEmpty(province)) {
            Asserts.fail(CodeMsg.BIND_ERROR.fillArgs("城市有误，请重新输入！"));
        }
        //判断redis是否存在缓存
        String redisKey = cityName + "_" + keywords;
        List shopList = redisTemplate.opsForList().range(redisKey, 0, -1);
        //如果不存在缓存
        if (shopList == null || shopList.size() == 0) {
            //调用高德地图获取初始化信息(调用接口)
            provinceService.initialData(keywords, cityName, shopList);
            //存缓存(存在bug，没有设置失效时间）
            redisTemplate.opsForList().rightPushAll(redisKey, shopList);
        }
        //控制台打印店铺数量
        System.out.println("数量:" + shopList.size());
        System.out.println("++++++ok++++++");
        return shopList;
    }

    /***
     * 查找所有城市
     * @return
     */
    @Override
    public List<CityModel> findAllCities() {
        //从redis中提取数据
        List citylist = redisTemplate.opsForList().range("citylist", 0, -1);
        //如果不存在缓存
        if (citylist == null || citylist.size() == 0) {
            citylist = webCrawlerDao.findAllCities();
            //写入redis(存在bug，没有设置失效时间）
            redisTemplate.opsForList().rightPushAll("citylist", citylist);
        }
        return citylist;
    }
}
