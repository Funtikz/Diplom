package org.example.ui.utils;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.example.ui.allure.AllureUtils;
import org.junit.jupiter.api.TestInfo;

public class TestContext {
    private static final ThreadLocal<Playwright> playwrightThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browserThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> browserContextThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<AllureUtils> allureUtilsThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<TestInfo> testInfoThreadLocal = new ThreadLocal<>();

    public static TestInfo getTestInfo() {
        return testInfoThreadLocal.get();
    }

    public static void setTestInfo(TestInfo testInfo) {
        testInfoThreadLocal.set(testInfo);
    }

    public static Playwright getPlaywright() {
        return playwrightThreadLocal.get();
    }

    public static void setPlaywright(Playwright playwright) {
        playwrightThreadLocal.set(playwright);
    }

    public static Browser getBrowser() {
        return browserThreadLocal.get();
    }

    public static void setBrowser(Browser browser) {
        browserThreadLocal.set(browser);
    }

    public static BrowserContext getBrowserContext() {
        return browserContextThreadLocal.get();
    }

    public static void setBrowserContext(BrowserContext browserContext) {
        browserContextThreadLocal.set(browserContext);
    }

    public static Page getPage() {
        return pageThreadLocal.get();
    }

    public static void setPage(Page page) {
        pageThreadLocal.set(page);
    }

    public static AllureUtils getAllureUtils() {
        return allureUtilsThreadLocal.get();
    }

    public static void setAllureUtils(AllureUtils allureUtils) {
        allureUtilsThreadLocal.set(allureUtils);
    }

    public static void cleanup() {
        if (getBrowser() != null) {
            getBrowser().close();
            setBrowser(null);
        }
        if (getPlaywright() != null) {
            getPlaywright().close();
            setPlaywright(null);
        }

        playwrightThreadLocal.remove();
        browserThreadLocal.remove();
        browserContextThreadLocal.remove();
        pageThreadLocal.remove();
        allureUtilsThreadLocal.remove();
    }
}