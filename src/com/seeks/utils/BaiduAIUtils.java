package com.seeks.utils;

import com.baidu.aip.ocr.AipOcr;
import com.oracle.tools.packager.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class BaiduAIUtils {
    //设置APPID/AK/SK
    public static final String APP_ID = "10860113";
    public static final String API_KEY = "lwHMIfj8NcGnhlGW5faFkhgD";
    public static final String SECRET_KEY = "rFhB3a4CYwagKsmxvLpTMyESTRpateWl";
    private AipOcr client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);
    private static BaiduAIUtils instance;
    public static BaiduAIUtils getInstance(){
        if(instance==null){
            instance = new BaiduAIUtils();
        }
        return instance;
    }
    public JSONObject imageToText(byte[] imagedata) {
        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
//        // 可选：设置代理服务器地址, http和socket二选一，或者均不设置
//        client.setHttpProxy("proxy_host", proxy_port);  // 设置http代理
//        client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理

        // 可选：设置log4j日志输出格式，若不设置，则使用默认配置
        // 也可以直接通过jvm启动参数设置此环境变量
//        System.setProperty("aip.log4j.conf", "path/to/your/log4j.properties");
        HashMap<String, String> options = new HashMap();
//        options.put("recognize_granularity","big");
//        options.put("vertexes_location","true");
        long _start = System.currentTimeMillis();
        // 调用接口
        JSONObject res = client.general(imagedata, options);
        System.out.println("耗时"+(System.currentTimeMillis()-_start)+":"+res.toString(2));


        return res;
    }
    public static String jsonToText(JSONObject res){
        JSONArray words = res.getJSONArray("words_result");
        StringBuffer rst = new StringBuffer();
        for(int i=0;i<words.length();i++){
            rst.append(words.getJSONObject(i).getString("words"));
        }
        return rst.toString();
    }
    public static void main(String[] args) throws IOException {
        File imageFile = new File("/Users/gaokui/weiqunkong/temp/1556185489657.png");
        BaiduAIUtils.getInstance().imageToText(IOUtils.readFully(imageFile));
    }
}
