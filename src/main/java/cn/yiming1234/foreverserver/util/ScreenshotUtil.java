package cn.yiming1234.foreverserver.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
@Slf4j
public class ScreenshotUtil {

    public static String takeScreenshot(String url) {
        try {
            File tempDir = new File("temp");
            if (!tempDir.exists()) {
                tempDir.mkdir();
            }
            String screenshotPath = "temp/screenshot.png";

            ProcessBuilder processBuilder = new ProcessBuilder("node", "scripts/screenshot.js", url, screenshotPath);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            process.waitFor();

            log.info("截图已保存至：{} " + screenshotPath);
            return "Screenshot taken and saved to " + screenshotPath;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Failed to take screenshot.";
        }
    }
}