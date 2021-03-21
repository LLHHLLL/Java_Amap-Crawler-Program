package com.spiderpoi.spider.enums;

import lombok.Getter;

/***
 * 多边形搜索方法枚举配置
 */
@Getter
public enum JWEnum {

    //上海的经纬度是东经120°52′-122°12′，北纬30°40′-31°53′之间。
    //北京位于东经115.7°—117.4°，北纬39.4°—41.6°
    //汕头市全境位于东经116°14′至 117°19′，北纬23°02′至23°38′之间
    //厦门的地理坐标（经纬度）：117.906193,24.440563-118.413268,24.717117
    //福州 118.129535,25.686997-119.840484,26.389226
    //莆田位于东经118°27`-119°39`，北纬24°59`-25°46`
    //温州全境介于北纬27°03’～28°36′，东经119°37′～121°18′之间。
    //宁波东经120°55’至122°16’，北纬28°51’至30°33’
    //杭州地理坐标为坐标为东经118°21′-120°30′，北纬29°11′-30°33′
    //南京地理坐标为北纬31°14″至32°37″，东经118°22″至119°14″。
    //南通市地处北纬31°41’06”～32°42'44”和东经120°11'47”～121°54'33”。
    //连云港北纬33°59′～35°07′、东经118°24′～119°48′之间
    //青岛位于东经119°30′～121°00′、北纬35°35′～37°09′
    //烟台位于东经119°34′～121°57′，北纬36°16′～38°23′
    //天津位于东经116°43'至118°04'，北纬38°34'至40°15'之间
    //秦皇岛介于北纬39°24′~40°37′，东经118°33′~119°51′之间
    //大连位于北纬38°43′～40°12′，东经120°58′～123°31′之间
    //石家庄位于北纬37°27′～38°47′（误差±1′），东经113°30′～115°20′（误差±1′）之间
    //济南市 116.342466,36.359759 - 117.731462,37.070733
    //郑州介于东经112°42′-114°14′，北纬34°16′-34°58′之间
    //合肥介于北纬30°57′-32°32′、东经116°41′-117°58′之间
    //南昌位于东经115°27'至116°35'、北纬28°10'至29°11'之间

    leftLongitude(115.27, "左经度"),
    rightLongitude(116.35, "右经度"),

    underLatitude(28.10, "下纬度"),
    aboveLatitude(29.11, "上纬度");

    /**
     * 描述
     */
    private double code;
    private String message;

    JWEnum(double code, String message) {
        this.code = code;
        this.message = message;
    }
}
