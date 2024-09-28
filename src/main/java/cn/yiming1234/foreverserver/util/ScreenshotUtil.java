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
            File screenshotFile = new File(screenshotPath);

            if (screenshotFile.exists()) {
                if (screenshotFile.delete()) {
                    log.info("Existing screenshot deleted: " + screenshotPath);
                } else {
                    log.error("Failed to delete existing screenshot: " + screenshotPath);
                    return "Failed to delete existing screenshot.";
                }
            }

            ProcessBuilder processBuilder = new ProcessBuilder("node", "scripts/screenshot.js", url, screenshotPath);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            process.waitFor();

            return "Screenshot taken and saved to " + screenshotPath;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Failed to take screenshot.";
        }
    }
}