package cn.yiming1234.foreverserver.constant;

public class DKConstant {

    /**
     * 蛋壳域名
     */
    private static final String DK_DOMIN = "https://api-wxc.sf-rush.com/";

    /**
     * phantomJS 配置
     */
    private static final String DK_PHANTOMJS_PATH = "/data/wwwroot/sftc.dankal.cn/phantomjs/";

    /**
     * phantomJS 脚本路径
     */
    public static final String DK_PHANTOMJS_SHELLPATH = DK_PHANTOMJS_PATH + "bin/phantomjs";

    /**
     * phantomJS js路径
     */
    public static final String DK_PHANTOMJS_JSPATH = DK_PHANTOMJS_PATH + "request.js";

    /**
     * phantomJS 截图保存路径
     */
    public static final String DK_PHANTOMJS_OUTPUTPATH = DK_PHANTOMJS_PATH + "images/";

    /**
     * phantomJS 页面地址
     */
    public static final String DK_PHANTOMJS_WEB_URL = DK_DOMIN + "web/index.html?order_id=";

    /**
     * phantomJS 图片地址
     */
    public static final String DK_PHANTOMJS_IMAGES = DK_DOMIN + "phantomjs/images/";

}
