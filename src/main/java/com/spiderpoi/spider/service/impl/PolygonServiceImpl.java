package com.spiderpoi.spider.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spiderpoi.common.exception.Asserts;
import com.spiderpoi.common.result.CodeMsg;
import com.spiderpoi.common.utils.HttpUtils;
import com.spiderpoi.spider.dto.Shop;
import com.spiderpoi.spider.enums.JWEnum;
import com.spiderpoi.spider.service.PolygonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/***
 * 多边形搜索实现类
 */
@Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
@Service
@Slf4j
public class PolygonServiceImpl implements PolygonService {

    /***
     * 高德api密钥
     */
    private static final String KEY = "389880a06e3f893ea46036f030c94700";

    /***
     * json格式
     */
    private static final String OUTPUT = "JSON";

    /**
     * 多边形搜索API
     */
    private static final String GET_LNG_PIO_URL = "http://restapi.amap.com/v3/place/polygon";

    /***
     * 初始化日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PolygonServiceImpl.class);


    /**
     * 多边形爬取控制器
     */
    @Override
    public void polygonSpiderInfo() {
        List<Shop> listShop = new ArrayList<>();
        //取经纬度(枚举类修改经纬度)
        for (double i = JWEnum.leftLongitude.getCode(); i <= JWEnum.rightLongitude.getCode(); i = i + 0.1) {
            for (double j = JWEnum.underLatitude.getCode(); j <= JWEnum.aboveLatitude.getCode(); j = j + 0.1) {
                List<Shop> listShopSon = new ArrayList<>();
                double lonHead = i;
                double latHead = j;
                double lonTail = i + 0.1;
                double latTail = j + 0.1;
                String lonLat = lonHead + "," + latHead + ";" + lonTail + "," + latTail;
                //多边形搜索：搜索的关键词keyword（修改即可搜索）
                listShopSon = initialData(lonLat, "烟店", listShopSon);
                for (Shop aListShopSon : listShopSon) {
                    System.out.println("店铺地址：" + aListShopSon.getSpecificAddress());
                }
                if (listShopSon.size() > 0) {
                    listShop.addAll(listShopSon);
                }
                System.out.println("店铺数量：" + listShop.size());
                double d = distance(lonHead, latHead, lonTail, latTail);
                System.out.println("两点距离" + d);
            }
        }
        System.out.println("店铺数量：" + listShop.size());
        createExcel(listShop);
    }

    /***
     * 多边形搜索方法初始化数据
     * @param lonLat
     * @param keyword
     * @param shopListSon
     * @return
     */
    private static List<Shop> initialData(String lonLat, String keyword, List<Shop> shopListSon) {
        if (StringUtils.isBlank(keyword)) {
            LOGGER.error("地址（" + keyword + "）为null或者空");
        }
        Map<String, String> params = new HashMap<>();
        params.put("key", KEY);
        params.put("keywords", keyword);
        params.put("polygon", lonLat);
        params.put("output", OUTPUT);
        params.put("offset", "20");
        params.put("page", "1");
        String result = HttpUtils.URLGet(GET_LNG_PIO_URL, params, "UTF-8");
        JSONObject jsonObject = JSONObject.parseObject(result);
        int statusOne = Integer.valueOf(jsonObject.getString("status"));
        //第一次获取数据时做的判断
        if (statusOne == 1) {
            int count = Integer.valueOf(jsonObject.getString("count"));
            int pageNumber = count / 20;
            int remainder = count % 20;
            if (remainder > 0) {
                pageNumber = pageNumber + 1;
            }
            for (int i = 1; i <= pageNumber; i++) {
                params.put("page", String.valueOf(i));
                result = HttpUtils.URLGet(GET_LNG_PIO_URL, params, "UTF-8");
                JSONObject jsonObject2 = JSONObject.parseObject(result);
                System.out.println("++++++++" + result + "++++++++");
                //拿到返回报文的status值，高德的该接口返回值有两个：0-请求失败，1-请求成功；
                int status = Integer.valueOf(jsonObject2.getString("status"));
                if (status == 1) {
                    JSONArray jsonArray = jsonObject2.getJSONArray("pois");
                    if (jsonArray.size() > 0) {
                        for (int j = 0; j < jsonArray.size(); j++) {
                            Shop shop = new Shop();
                            JSONObject jsonObject1 = jsonArray.getJSONObject(j);
                            //店铺id
                            shop.setId(jsonObject1.getString("id"));
                            //省份或直辖市
                            shop.setPname(jsonObject1.getString("pname"));
                            //城市
                            shop.setCityname(jsonObject1.getString("cityname"));
                            //城市下县级市或县级
                            shop.setAdname(jsonObject1.getString("adname"));
                            //具体位置
                            shop.setSpecificAddress(jsonObject1.getString("address"));
                            //店铺名字
                            shop.setShopName(jsonObject1.getString("name"));
                            //店铺电话
                            shop.setTelePhone(jsonObject1.getString("tel"));
                            //店铺类型
                            shop.setShopType(jsonObject1.getString("type"));
                            //经纬度
                            String[] initLonLat = jsonObject1.getString("location").split(",");
                            shop.setLongitude(initLonLat[0]);
                            shop.setLatitude(initLonLat[1]);
                            //添加进list中
                            shopListSon.add(shop);
                        }
                    }
                } else {
                    String errorMsg = jsonObject.getString("info");
                    LOGGER.error("地址（" + keyword + "）" + errorMsg);
                }
            }
        }
        //通过HashSet剔除重复数据
        HashSet hashSet = new HashSet(shopListSon);
        shopListSon.clear();
        shopListSon.addAll(hashSet);
        return shopListSon;
    }

    /***
     * 计算距离
     * @param long1
     * @param lat1
     * @param long2
     * @param lat2
     * @return
     */
    private static double distance(double long1, double lat1, double long2, double lat2) {
        double a, b, r;
        //地球半径 6371km
        r = 6371;
        lat1 = lat1 * Math.PI / 180.0;
        lat2 = lat2 * Math.PI / 180.0;
        a = lat1 - lat2;
        b = (long1 - long2) * Math.PI / 180.0;
        double d;
        double sa2, sb2;
        sa2 = Math.sin(a / 2.0);
        sb2 = Math.sin(b / 2.0);
        d = 2
                * r
                * Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1)
                * Math.cos(lat2) * sb2 * sb2));
        BigDecimal bigDecimal = new BigDecimal(d * 1000);
        return bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 写入excel中(多边形搜索方法）
     *
     * @param shopList
     */
    private static void createExcel(List<Shop> shopList) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        //第二步，在workbook中创建一个sheet对应excel中的sheet
        HSSFSheet sheet = workbook.createSheet("高德地图数据");
        //第三步，在sheet表中添加表头第0行，老版本的poi对sheet的行列有限制
        HSSFRow row = sheet.createRow(0);
        //第四步，创建单元格，设置表头
        HSSFCell cell = row.createCell(0);
        cell.setCellValue("店铺id");

        cell = row.createCell(1);
        cell.setCellValue("省份或直辖市");

        cell = row.createCell(2);
        cell.setCellValue("城市");

        cell = row.createCell(3);
        cell.setCellValue("县级市或县级");

        cell = row.createCell(4);
        cell.setCellValue("具体位置");

        cell = row.createCell(5);
        cell.setCellValue("店铺名字");

        cell = row.createCell(6);
        cell.setCellValue("店铺电话");

        cell = row.createCell(7);
        cell.setCellValue("店铺类型");

        cell = row.createCell(8);
        cell.setCellValue("经度");

        cell = row.createCell(9);
        cell.setCellValue("纬度");

        //第五步，写入实体数据，实际应用中这些数据从数据库得到,对象封装数据，集合包对象。对象的属性值对应表的每行的值
        for (int i = 0; i < shopList.size(); i++) {
            HSSFRow hssfRow = sheet.createRow(i + 1);
            Shop shop = shopList.get(i);
            //创建单元格设值
            hssfRow.createCell(0).setCellValue(shop.getId());
            hssfRow.createCell(1).setCellValue(shop.getPname());
            hssfRow.createCell(2).setCellValue(shop.getCityname());
            hssfRow.createCell(3).setCellValue(shop.getAdname());
            hssfRow.createCell(4).setCellValue(shop.getSpecificAddress());
            hssfRow.createCell(5).setCellValue(shop.getShopName());
            hssfRow.createCell(6).setCellValue(shop.getTelePhone());
            hssfRow.createCell(7).setCellValue(shop.getShopType());
            hssfRow.createCell(8).setCellValue(shop.getLongitude());
            hssfRow.createCell(9).setCellValue(shop.getLatitude());
        }

        //将文件保存到指定的位置
        try {
            //需提前建好excel表格！！！（不然运行完写入失败浪费时间）
            FileOutputStream fos
                    = new FileOutputStream("");
            workbook.write(fos);
            System.out.println("写入成功");
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Asserts.fail(CodeMsg.FILE_LOCATION_ERROR);
        }
    }
}
