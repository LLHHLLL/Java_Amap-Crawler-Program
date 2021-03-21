package com.spiderpoi.spider.controller;

import com.spiderpoi.common.result.CodeMsg;
import com.spiderpoi.common.result.Result;
import com.spiderpoi.spider.dto.SingleCityParam;
import com.spiderpoi.spider.dto.SingleProvinceParam;
import com.spiderpoi.spider.service.PolygonService;
import com.spiderpoi.spider.service.ProvinceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/Amap/CreateTable")
public class WebCrawlerController {

    @Autowired
    private PolygonService polygonService;

    @Autowired
    private ProvinceService provinceService;

    /***
     * 城市搜索
     */
    @PostMapping(value = "singleCity")
    @ResponseBody
    public Result singleCitySpiderInfo(@RequestBody SingleCityParam param){
        provinceService.singleCitySpiderInfo(param);
        return Result.success(CodeMsg.SUCCESS);
    }

    /***
     * 单个省份
     */
    @PostMapping(value = "singleProvince")
    @ResponseBody
    public Result singleSpiderInfo(@RequestBody SingleProvinceParam param) {
        provinceService.singleprovinceInfo(param);
        return Result.success(CodeMsg.SUCCESS);
    }

    /***
     * 获取全国
     */
    @PostMapping(value = "nationwide")
    @ResponseBody
    public Result cycleSpiderInfo(@RequestParam String keyWord) {
        provinceService.cycleProvinceInfo(keyWord);
        return Result.success(CodeMsg.SUCCESS);
    }

    /**
     * 多边形爬取控制器
     */
    @PostMapping(value = "polygonSpiderInfo")
    @ResponseBody
    public Result polygonSpiderInfo() {
        polygonService.polygonSpiderInfo();
        return Result.success(CodeMsg.SUCCESS);
    }
}
