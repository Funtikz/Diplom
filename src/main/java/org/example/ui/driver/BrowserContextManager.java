package org.example.ui.driver;

import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.example.ui.utils.TestContext;

import java.nio.file.Paths;

import static org.example.ui.Constants.Capability.NEED_VIDEO;


public class BrowserContextManager {

    // Добавляем константу для пути загрузок
    private static final String DEFAULT_DOWNLOAD_PATH = "target";

    private BrowserContextManager() {
    }

    /**
     * Создает и настраивает браузер, контекст и страницу для текущего потока
     */
    public static void initializeBrowserContext(Integer width, Integer height) {
        initializePlaywright();
        initializeBrowser();
        createBrowserContextAndPage(width, height);
    }

    /**
     * Инициализирует Playwright для текущего потока, если еще не создан
     */
    private static void initializePlaywright() {
        if (TestContext.getPlaywright() == null) {
            TestContext.setPlaywright(Playwright.create());
        }
    }

    /**
     * Инициализирует браузер для текущего потока, если еще не создан
     */
    private static void initializeBrowser() {
        if (TestContext.getBrowser() == null) {
            TestContext.setBrowser(BrowserManager.getBrowser(TestContext.getPlaywright()));
        }
    }

    /**
     * Создает контекст браузера и страницу для текущего теста
     */
    private static void createBrowserContextAndPage(Integer width, Integer height) {
        NewContextOptions contextOptions = createContextOptions(width, height);
        BrowserContext context = TestContext.getBrowser().newContext(contextOptions);

        TestContext.setBrowserContext(context);
        TestContext.setPage(context.newPage());

        //Добавил глобальные таймауты для клика и т.д
        TestContext.getPage().setDefaultTimeout(5000);
        TestContext.getPage().setDefaultNavigationTimeout(30000);

        setupDownloadHandler(TestContext.getPage());
    }

    /**
     * Создает настройки для контекста браузера
     */
    private static NewContextOptions createContextOptions(Integer width, Integer height) {
        NewContextOptions options = new NewContextOptions()
                .setIgnoreHTTPSErrors(true)
                .setViewportSize(width, height);

        if (NEED_VIDEO) {
            options.setRecordVideoDir(Paths.get("build", "video-results"))
                    .setRecordVideoSize(width, height);
        }

        return options;
    }

    /**
     * Настраивает обработчик загрузок для страницы
     */
    private static void setupDownloadHandler(Page page) {
        page.onDownload(download -> {
            String fileName = download.suggestedFilename();
            try {
                download.saveAs(Paths.get(DEFAULT_DOWNLOAD_PATH, fileName));
                System.out.println("Файл скачан: " + DEFAULT_DOWNLOAD_PATH + "/" + fileName);
            } catch (Exception e) {
                System.err.println("Ошибка при скачивании файла: " + fileName);
            }
        });
    }

    /**
     * Возвращает текущий BrowserContext для потока
     */
    public static BrowserContext getBrowserContext() {
        BrowserContext context = TestContext.getBrowserContext();
        if (context == null) {
            throw new IllegalStateException("BrowserContext не инициализирован. Вызовите сначала initializeBrowserContext()");
        }
        return context;
    }

    /**
     * Возвращает текущую Page для потока
     */
    public static Page getPage() {
        Page page = TestContext.getPage();
        if (page == null) {
            throw new IllegalStateException("Page не инициализирована. Вызовите сначала initializeBrowserContext()");
        }
        return page;
    }

    /**
     * Закрывает контекст и страницу (вызывать в @AfterEach)
     */
    public static void closeBrowserContext() {
        if (TestContext.getPage() != null) {
            TestContext.getPage().close();
            TestContext.setPage(null);
        }
        if (TestContext.getBrowserContext() != null) {
            TestContext.getBrowserContext().close();
            TestContext.setBrowserContext(null);
        }
    }

}