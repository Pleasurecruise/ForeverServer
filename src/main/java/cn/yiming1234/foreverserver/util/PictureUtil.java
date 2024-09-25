package cn.yiming1234.foreverserver.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static cn.yiming1234.foreverserver.constant.DKConstant.*;

public class PictureUtil {

    public static String screenShot(String url, String output) {

        String outPutPath = DK_PHANTOMJS_OUTPUTPATH + output;

        Runtime rt = Runtime.getRuntime();
        StringBuilder sb = new StringBuilder();
        try {
            String cmd = DK_PHANTOMJS_SHELLPATH + " " + DK_PHANTOMJS_JSPATH + " " + url + " " + outPutPath;
            Process process = rt.exec(cmd);
            InputStream is = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String tmp = "";
            try {
                while ((tmp = br.readLine()) != null) {
                    sb.append(tmp);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

}
