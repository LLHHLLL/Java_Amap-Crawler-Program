package com.spiderpoi.spider.controller;

import com.spiderpoi.spider.dto.CityModel;
import com.spiderpoi.spider.dto.Shop;
import com.spiderpoi.spider.dto.SingleCityParam;
import com.spiderpoi.spider.dto.SingleProvinceParam;
import com.spiderpoi.spider.service.CheckInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class CheckInfoController {

    @Autowired
    private CheckInfoService checkInfoService;


    /***
     * 通过关键字查找全国的所有商店
     * @param model
     * @param keyword
     * @return
     */
    @RequestMapping("/getShopInfoInNationwide.do")
    public String getShopInfoInNationwide(Model model, String keyword) {
        List<Shop> shopList = checkInfoService.getShopInfoInNationwide(keyword);
        model.addAttribute("shopList",shopList);
        return "shopList";
    }

    /***
     * 全国搜索页面接口
     * @return
     */
    @RequestMapping("/nationwide/searchShopInNationwidePage")
    public String searchShopInNationwidePage() {
        return "searchShopInNationwidePage";
    }

    /***
     * 通过关键字查找省份中的所有商店
     * @param model
     * @param param
     * @return
     */
    @RequestMapping("/getShopInfoInProvince.do")
    public String getShopInfoInProvince(Model model, SingleProvinceParam param) {
        List<Shop> shopList = checkInfoService.findAllShopInProvinceByKeyword(param);
        model.addAttribute("shopList", shopList);
        return "shopList";
    }

    /***
     * 省份搜索页面接口
     * @return
     */
    @RequestMapping("/province/searchShopInProvincePage")
    public String searchShopInProvincePage() {
        return "searchShopInProvincePage";
    }

    /***
     * 通过关键字查找城市中的所有商店
     * @param model
     * @param param
     * @return
     */
    @RequestMapping("/getShopInfoInCity.do")
    public String getShopInCityInfo(Model model, SingleCityParam param) {
        List<Shop> shopList = checkInfoService.findAllShopInCityByKeyword(param);
        model.addAttribute("shopList", shopList);
        return "shopList";
    }

    /***
     * 城市搜索页面接口
     * @return
     */
    @RequestMapping("/city/searchShopInCityPage")
    public String searchShopInCityPage() {
        return "searchShopInCityPage";
    }

    /***
     * 获取所有城市信息
     * @param model
     * @return
     */
    @RequestMapping("/getCities.do")
    public String getCitiesInfo(Model model) {
        List<CityModel> cityList = checkInfoService.findAllCities();
        model.addAttribute("cityList", cityList);
        return "cityList";
    }

    /***
     * 测试
     * @param model
     * @return
     */
    @RequestMapping("/index.do")
    public String index(Model model) {
        model.addAttribute("name", "jack");
        model.addAttribute("age", 20);
        model.addAttribute("info", "我是好青年");
        return "index";
    }
}
