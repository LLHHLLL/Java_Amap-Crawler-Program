package com.spiderpoi.spider.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spiderpoi.common.exception.Asserts;
import com.spiderpoi.common.result.CodeMsg;
import com.spiderpoi.common.utils.HttpUtils;
import com.spiderpoi.spider.dao.WebCrawlerDao;
import com.spiderpoi.spider.dto.*;
import com.spiderpoi.spider.service.ProvinceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

@Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
@Service
@Slf4j
public class ProvinceServiceImpl implements ProvinceService {

    /***
     * 高德api密钥
     * 自己去官网注册一个账号获取密钥
     */
    private static final String KEY = "389880a06e3f893ea46036f030c94700";

    /***
     * json格式
     */
    private static final String OUTPUT = "JSON";

    /***
     * 关键字搜索API
     */
    private static final String GET_TEXT_PIO_URL = "https://restapi.amap.com/v3/place/text";

    /***
     * 初始化日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProvinceServiceImpl.class);

    @Autowired
    private WebCrawlerDao webCrawlerDao;

    /***
     * 城市搜索
     * @param
     */
    @Override
    public void singleCitySpiderInfo(SingleCityParam param) {
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

        //定义新的实体类list
        List<Shop> listShop = new ArrayList<>();
        //调用高德地图获取初始化信息
        initialData(keywords, cityName, listShop);
        //打印信息
        System.out.println("城市:" + cityName);
        System.out.println("店铺数量：" + listShop.size());
        //如果该地区店铺数量为0则不生成表
        if (listShop.size() != 0) {
            //写入excel
            createExcel(listShop, keywords, province, cityName);
        } else {
            System.out.println("该城市或地区的店铺数量为0,不生成表!");
        }
        System.out.println("=========");
        System.out.println("---OK---");
        System.out.println("=========");
    }

    /***
     * 单个省份搜索
     * @param param
     */
    @Override
    public void singleprovinceInfo(SingleProvinceParam param) {
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

        //sql查询数据库
        CityProvinceDto dto = webCrawlerDao.singleProvinceQuery(province);
        //分割装换成数组,再遍历
        String[] split = dto.getCity().split(",");
        for (String cityName : split) {
            //在循环中定义新的实体类
            List<Shop> listShop = new ArrayList<>();
            //调用高德地图获取初始化信息
            listShop = initialData(keywords, cityName, listShop);
            //打印信息
            System.out.println("城市:" + cityName);
            System.out.println("店铺数量：" + listShop.size());
            //如果该地区店铺数量为0则不生成表
            if (listShop.size() != 0) {
                //写入excel
                createExcel(listShop, keywords, province, cityName);
            } else {
                System.out.println("该城市或地区的店铺数量为0,不生成表!");
            }
        }
        System.out.println("=========");
        System.out.println("---OK---");
        System.out.println("=========");
    }

    /***
     * 循环省份搜索
     * @param keywords
     */
    @Override
    public void cycleProvinceInfo(String keywords) {
        //校验参数
        if (StringUtils.isEmpty(keywords) || "".equals(keywords)) {
            Asserts.fail(CodeMsg.BIND_ERROR.fillArgs("关键词为空，请重新输入！"));
        }

        //sql
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
                    //在循环中定义新的实体类
                    List<Shop> listShop = new ArrayList<>();
                    //调用高德地图获取初始化信息
                    listShop = initialData(keywords, cityName, listShop);
                    //打印信息
                    System.out.println("城市:" + cityName);
                    System.out.println("店铺数量：" + listShop.size());
                    //如果该地区店铺数量为0则不生成表
                    if (listShop.size() != 0) {
                        //写入excel
                        createExcel(listShop, keywords, dto.getProvince(), cityName);
                    } else {
                        System.out.println("该城市或地区的店铺数量为0,不生成表!");
                    }
                }
            } else {
                System.out.println("不输出台湾省数据!");
            }
        }
        System.out.println("=========");
        System.out.println("---OK---");
        System.out.println("=========");
    }

    /***
     * 初始化数据
     * @param keyword
     * @param city
     * @param listShop
     * @return
     */
    @Override
    public List<Shop> initialData(String keyword, String city, List<Shop> listShop) {
        if (StringUtils.isBlank(keyword)) {
            LOGGER.error("地址（" + keyword + "）为null或者空");
        }
        Map<String, String> params = new HashMap<>();
        params.put("key", KEY);
        params.put("keywords", keyword);
        params.put("city", city);
        params.put("offset", "20");
        params.put("page", "1");
        params.put("output", OUTPUT);
        //仅返回指定城市数据(可不传)
        params.put("citylimit", "true");
        //调用工具类拼接地址返回
        String result = HttpUtils.URLGet(GET_TEXT_PIO_URL, params, "UTF-8");
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
                result = HttpUtils.URLGet(GET_TEXT_PIO_URL, params, "UTF-8");
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
                            listShop.add(shop);
                        }
                    }
                } else {
                    String errorMsg = jsonObject.getString("info");
                    LOGGER.error("地址（" + keyword + "）" + errorMsg);
                }
            }
        }
        //通过HashSet剔除重复数据
        HashSet hashSet = new HashSet(listShop);
        listShop.clear();
        listShop.addAll(hashSet);
        return listShop;
    }

    /**
     * 写入excel中
     */
    private static void createExcel(List<Shop> listShop, String keywords, String province, String cityName) {
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
        for (int i = 0; i < listShop.size(); i++) {
            HSSFRow hssfRow = sheet.createRow(i + 1);
            Shop shop = listShop.get(i);
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

//        //将文件保存到指定的位置
//        try {
//            //需提前建好excel表格！！！（不然运行完写入失败浪费时间）
//            FileOutputStream fos
//                    = new FileOutputStream("");
//            workbook.write(fos);
//            System.out.println("写入成功");
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // 第六步，将文件存到指定位置
        try {
//            String path = "C:\\Users\\LHL\\Desktop\\" + keywords + "\\" + province + "\\" + cityName + keywords + ".xls";
            String path = "";
            File file = new File(path);
            //如果已经存在则删除
            if (file.exists()) {
                file.delete();
            }
            //检查父包是否存在
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            //创建文件
            file.createNewFile();
            FileOutputStream fout = new FileOutputStream(path);
            workbook.write(fout);
            System.out.println("导出成功！");
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
            Asserts.fail(CodeMsg.FILE_LOCATION_ERROR);
        }
    }
}
