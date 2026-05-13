package org.example;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.example.ui.allure.AllureUtils;
import org.example.ui.driver.BrowserContextManager;
import org.example.ui.utils.TestContext;
import org.junit.jupiter.api.*;

import java.io.IOException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseTest {

    protected Page getPage() {
        return TestContext.getPage();
    }

    public BrowserContext getBrowserContext() {
        return TestContext.getBrowserContext();
    }

    protected AllureUtils getAllureUtils() {
        return TestContext.getAllureUtils();
    }

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        BrowserContextManager.initializeBrowserContext(1920, 1080);
        TestContext.setAllureUtils(new AllureUtils(getBrowserContext(), getPage()));
        TestContext.setTestInfo(testInfo);
    }

    @AfterEach
    public void tearDown(TestInfo testInfo) throws IOException {
        if (getAllureUtils() != null) {
            getAllureUtils().attachVideoToAllure(testInfo);
        }

        // Правильная очистка через менеджер (закрывает page + context)
        TestContext.setBrowserContext(null);
        TestContext.setPage(null);
        BrowserContextManager.closeBrowserContext();
    }

    @AfterAll
    void closeBrowser() {
        // Закрываем браузер и Playwright после всех тестов
        TestContext.cleanup();
    }
}