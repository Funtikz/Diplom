package org.example.ui.allure;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import lombok.Setter;
import org.junit.jupiter.api.TestInfo;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Set;

import static org.example.ui.Constants.Capability.NEED_VIDEO;


public class AllureUtils {
    @Setter
    protected BrowserContext browserContext;
    private static final HashMap<String,byte[]> videoBuffers = new HashMap<>();



    @Setter
    protected Page page;

    public AllureUtils(BrowserContext browserContext, Page page){
        this.page = page;
        this.browserContext = browserContext;
    }

    /**
     * Прикрепляет видео запись теста к отчету Allure вместе со скриншотом.
     * Метод выполняет следующие действия:
     * Создает скриншот текущей страницы и прикрепляет его к отчету
     * Закрывает контекст браузера, завершая запись видео
     * Извлекает путь к записанному видеофайлу
     * Читает видеофайл и прикрепляет его к отчету Allure с именем в формате: {@code [имя_тестового_метода]_video}
     */
    public void attachVideoToAllure(TestInfo testInfo) throws IOException {
        takeScreenshot(testInfo);
        browserContext.close();
        if (NEED_VIDEO){
            String videoPath = page.video().path().toString();
            try (InputStream videoStream = new FileInputStream(videoPath)) {
                Allure.addAttachment(testInfo.getTestMethod().get().getName() + "_video", "video/webm", videoStream, ".webm");
                if (videoBuffers.containsKey(testInfo.getDisplayName())){
                    Set<String> allKeys = videoBuffers.keySet();
                    for (String key : allKeys) {
                        if (key.equals(testInfo.getDisplayName())) {
                            attachAllVideosFromBuffer(testInfo);
                            System.out.println("Добавили видео к тесту " + testInfo);
                        }
                    }
                }
            }
        }
    }

    public void attachVideoToAllureNewTabs(TestInfo testInfo) throws IOException {
        browserContext.close();
        if (NEED_VIDEO){
            String videoPath = page.video().path().toString();
            byte[] videoBytes = Files.readAllBytes(Paths.get(videoPath));
            videoBuffers.put(testInfo.getDisplayName(),videoBytes);
        }
    }

    public void attachAllVideosFromBuffer(TestInfo testInfo) {
        byte[] videoBytes = videoBuffers.get(testInfo.getDisplayName());
        if (videoBytes != null) {
            String name = testInfo.getTestMethod().get().getName() + "_video_additional";
            Allure.getLifecycle().addAttachment(
                    name, "video/webm", ".webm",
                    new ByteArrayInputStream(videoBytes)
            );
            videoBuffers.remove(testInfo);
        }
    }


    public void takeScreenshot(TestInfo testInfo) {
        try {
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions());
            Allure.addAttachment(
                    testInfo.getTestMethod().get().getName() + "_screenshot",
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    ".png"
            );
        } catch (Exception e) {
            System.out.println("Ошибка при снятии скриншота: " + e.getMessage());
        }
    }

    public void takeScreenshot(String screenshotName) {
        try {
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions());
            Allure.addAttachment(
                    screenshotName, // используем переданное имя
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    ".png"
            );
        } catch (Exception e) {
            System.out.println("Ошибка при снятии скриншота: " + e.getMessage());
        }
    }

    /**
     * Скриншот конкретной вкладки (решает проблему с TestContext)
     */
    public void takeScreenshot(Page specificPage, String screenshotName) {
        try {
            byte[] screenshot = specificPage.screenshot(new Page.ScreenshotOptions());
            Allure.addAttachment(
                    screenshotName,
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    ".png"
            );
        } catch (Exception e) {
            System.out.println("Ошибка скриншота: " + e.getMessage());
        }
    }

}
