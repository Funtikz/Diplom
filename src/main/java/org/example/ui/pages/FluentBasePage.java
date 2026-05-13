
package org.example.ui.pages;


import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.qameta.allure.Allure;
import io.qameta.allure.Param;
import io.qameta.allure.Step;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.ui.allure.AllureUtils;
import org.example.ui.driver.BrowserContextManager;
import org.example.ui.driver.HighlightElements;
import org.example.ui.utils.TestContext;
import org.example.ui.waiter.WaitHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInfo;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.qameta.allure.model.Parameter.Mode.HIDDEN;

@Slf4j
public abstract class FluentBasePage<T extends FluentBasePage<T>> {
    protected Page page;
    protected final HighlightElements highlightElements;
    protected BrowserContext browserContext;

    public FluentBasePage(Page page) {
        this.page = page;
        this.highlightElements = new HighlightElements(page);
        this.highlightElements.initializeHighlightStyles();
        browserContext = TestContext.getBrowserContext();
    }


    protected T self() {
        return (T) this;
    }

    public Integer getElementsListSize(String xpath){
        return getPage().locator(xpath).count();
    }

    public Integer getElementsListSize(Locator locator){
        return locator.count();
    }

    public String getText(String xpath){
        return page.locator(xpath).innerText();
    }

    public String getText(Locator locator){
        return locator.innerText();
    }

    @Step("Происходит редирект на {endUrl}")
    public T checkFinalUrl(@Param(mode = HIDDEN) String endUrl) {
        PlaywrightAssertions.assertThat(page).hasURL(endUrl);
        log.info("Редиректнуло на " + endUrl);
        return self();
    }

    @Step("Скролл вниз до конца страницы")
    public T scrollDown() {
        page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
        page.waitForTimeout(1000);
        log.info("Проскроллили до конца страницы");
        return self();
    }

    @Step("Скролл вверх в начало страницы")
    public T scrollUp() {
        page.evaluate("window.scrollTo(0, 0)");
        page.waitForTimeout(1000);
        log.info("Проскроллили в начало страницы");
        return self();
    }


    @Step("Видим текстовку: {expectedSubstring}")
    public T assertText(@Param(mode=HIDDEN) String expectedSubstring, @Param(mode=HIDDEN) String xpathElement) {
        String actualText = getText(xpathElement);
        Assertions.assertTrue(
                actualText.contains(expectedSubstring),
                "Для элемента с xpath=" + xpathElement + " ожидали, что текст содержит: " + expectedSubstring + ". Фактический текст: " + actualText
        );
        return self();
    }

    public String getUrl(){
        return page.url();
    }

    @Step("Открыли страницу: {url}")
    public T open(@Param(mode=HIDDEN) String url) {
        page.navigate(url);
        waitForLoadState();
//        page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions()
//                .setTimeout(30000));
        return self();
    }

    /**
     * Скролл к элементу (если он не виден)
     */
    @Step("Скроллим к элементу: {xpath}")
    public T scrollToElement(String xpath) {
        Locator element = page.locator(xpath);
        element.scrollIntoViewIfNeeded();
        return self();
    }

    /**
     * Скролл к элементу (если он не виден)
     */
    @Step("Скроллим к элементу: {xpath}")
    public T scrollToElement(Locator element) {
        element.scrollIntoViewIfNeeded();
        return self();
    }

    @SneakyThrows
    @Step("Переключаемся в шапке на {organization}")
    public T selectOrganizationInHeader(@Param(mode=HIDDEN) String organization) {
        openHeaderOrganizationsList();
        String organizationXpath = "(//*[text()='" + organization + "'])[last()]";
        String allBussinessAccountsXpath = "//*[text()='Все бизнес-аккаунты']";

        if (getElementsListSize(organizationXpath) != 0) {
            click(organizationXpath);
            log.info("Переключились в шапке на: " + organization);
        } else {
            click(allBussinessAccountsXpath);
            click(organizationXpath);
            click(allBussinessAccountsXpath + "/following-sibling::button");
            log.info("Переключились в шапке на: " + organization + ", провалившись во все бизнес-аккаунты");
        }

        getAllure().takeScreenshot("Переключились в шапке на " + organization);
        return self();
    }

    private T openHeaderOrganizationsList() {
        click("(//*[text()='Вы управляете:']/../div)[2]");
        return self();
    }

    /**
     * Загрузка файла в input элемент
     */
    @Step("Загружаем файл {filePath} в инпут {inputXpath}")
    public T uploadFile(String inputXpath, String filePath) {
        Locator uploadInput = page.locator(inputXpath);
        Path file = Paths.get(filePath);

        uploadInput.setInputFiles(file);

        log.info("Загрузка файла " + filePath + " в инпут " + inputXpath);
        return self();
    }


    @Step("Проверили, что элементов {xpathElement} на странице (число): {expectedElementsCount}")
    public T checkElementCounts(@Param(mode=HIDDEN) String xpathElement, @Param(mode=HIDDEN) int expectedElementsCount){
        // Ждём перед ассертом
        WaitHelper.waitForElementCount(getPage(), xpathElement, expectedElementsCount, 5000, 200);
        Assertions.assertEquals(expectedElementsCount, page.locator(xpathElement).count());
        return self();
    }

    public T refresh() {
        page.reload();
        return self();
    }

    @SneakyThrows
    public void selectBrowserTab(int index) {
        waitForTimeout(1500);
        List<Page> pages = browserContext.pages();
        if (index >= 0 && index < pages.size()) {
            // 1. Сохраняем данные
            Page targetPage = pages.get(index);
            String currentUrl = targetPage.url();
            Map<String, String> cookies = saveCookies();
            TestInfo testInfo = TestContext.getTestInfo();
            // 2. Сохраняем видео и закрываем контекст
            TestContext.getAllureUtils().attachVideoToAllureNewTabs(testInfo);
            // 3. ПЕРЕСОЗДАЕМ контекст
            BrowserContextManager.initializeBrowserContext(1920, 1080);
            // 4. Обновляем ссылки на новые объекты из TestContext
            this.browserContext = TestContext.getBrowserContext();
            this.page = TestContext.getPage();
            TestContext.getAllureUtils().setPage(this.page);
            highlightElements.setPage(this.page);
            // 5. Восстанавливаем и переходим
            if (currentUrl.startsWith("blob:")){
                getAllure().takeScreenshot("Открыли страницу " + currentUrl);
                return;
            }
            restoreCookies(this.browserContext, cookies, currentUrl);
            this.page.navigate(currentUrl);
            this.page.waitForLoadState();
            getAllure().setBrowserContext(browserContext);
        }
    }

    @Step("Проверяем открытие документации в новой вкладке {additionalText}")
    public T checkPdfOnNewPage(String elementXpath, String... additionalText) {
        int countTabs = browserContext.pages().size();
        click(elementXpath);
        // Ждем максимум 10 секунд появления новой вкладки
        browserContext.waitForCondition(
                () -> browserContext.pages().size() == countTabs + 1,
                new BrowserContext.WaitForConditionOptions().setTimeout(10000)
        );
        Page newPage = browserContext.pages().get(browserContext.pages().size() - 1);
        log.info("Перешли на страницу {}", newPage.url());
        getAllure().takeScreenshot(newPage, "Перешли на страницу " + newPage.url());
        Assertions.assertEquals(countTabs + 1, browserContext.pages().size());
        Allure.addAttachment("Статистика вкладок", "text/plain",
                String.format("Было: %d\nСтало: %d\nURL: %s", countTabs, browserContext.pages().size(), newPage.url()));
        log.info("Было: {}\nСтало: {}\nURL: {}", countTabs, browserContext.pages().size(), newPage.url());
        newPage.close();
        return self();
    }

    @Step("Проверяем открытие документации в новой вкладке и ожидаем редирект на - {expectedUrl}")
    public void checkNewTabPage(String elementXpath, String expectedUrl) {
        int countTabs = browserContext.pages().size();
        click(elementXpath);
        // Ждем максимум 10 секунд появления новой вкладки
        browserContext.waitForCondition(
                () -> browserContext.pages().size() == countTabs + 1,
                new BrowserContext.WaitForConditionOptions().setTimeout(10000)
        );
        Page newPage = browserContext.pages().get(browserContext.pages().size() - 1);
        getAllure().takeScreenshot(newPage, "Перешли на страницу " + newPage.url());
        Assertions.assertEquals(countTabs + 1, browserContext.pages().size());
        Assertions.assertEquals(expectedUrl, newPage.url());

        newPage.close();
    }



    private Map<String, String> saveCookies() {
        List<Cookie> cookies = browserContext.cookies();
        Map<String, String> cookieMap = new HashMap<>();

        for (Cookie cookie : cookies) {
            cookieMap.put(cookie.name, cookie.value);
        }
        return cookieMap;
    }

    @SneakyThrows
    private void restoreCookies(BrowserContext context, Map<String, String> cookies, String currentUrl) {
        String domain = new URL(currentUrl).getHost();

        List<Cookie> cookieList = cookies.entrySet().stream()
                .map(entry -> new Cookie(entry.getKey(), entry.getValue())
                        .setDomain(domain)
                        .setPath("/"))
                .collect(Collectors.toList());

        if (!cookieList.isEmpty()) {
            context.addCookies(cookieList);
        }
    }


    @Step("Проверили, что элемент {elementXpath} содержит атрибут {myAttribute} со значением: {expectedValue}")
    public T validElementByAttributeAndValue(@Param(mode = HIDDEN) String elementXpath,
                                             @Param(mode = HIDDEN) String myAttribute,
                                             @Param(mode = HIDDEN) String expectedValue) {

        Locator element = page.locator(elementXpath);
        waitForLocatorVisible(element);
        String actualAttributeValue = element.getAttribute(myAttribute);
        Assertions.assertTrue(actualAttributeValue.contains(expectedValue),
                "Ожидали найти вхождение: " + expectedValue + "\nПолучили: " + actualAttributeValue);

        return self();
    }

    public T goBack() {
        page.goBack();
        return self();
    }

    public T goForward() {
        page.goForward();
        return self();
    }

    public T click(Locator locator) {
        waitForLocatorVisible(locator.first());
        highlightElements.clickWithHighlight(locator);
        waitForLoadState();
        return self();
    }

    public T click(String xPath) {
        waitForLocatorVisible(page.locator(xPath).first());
        highlightElements.clickWithHighlight(xPath);
        waitForLoadState();
        return self();
    }

    public T goIfClickable(String xpath){
        for (int i = 0; i < 5; i ++){
            if (getElementsListSize(xpath) > 0){
                click(xpath);
                log.info(xpath + " - элемент найден за " + (i + 1) + " попытку");
                break;
            }
            log.info((i + 1) + " - попытка найти элемент " +  xpath);
            waitForTimeout(200);
        }
        return self();
    }

    public T fill(Locator locator, String value) {
        locator.fill(value);
        return self();
    }

    public T fill(String xpath, String value) {
        page.locator(xpath).fill(value);
        return self();
    }

    @Step("Ожидали, что разрешение содержит: {actual}. Фактически: {expected}")
    public void assertContains(String actual, String expected, String errorMessage) {
        Assertions.assertTrue(
                actual.contains(expected),
                String.format(errorMessage, expected, actual)
        );
    }

    /**
     * Проверка, что элемент видим сейчас
     */
    public boolean elementIsVisibilityNow(String xpath) {
        return page.locator(xpath).isVisible();
    }

    /**
     * Проверка, что элемент существует в DOM страницы
     */
    public boolean elementExistsInDOM(String xpath) {
        return page.locator(xpath).count() > 0;
    }

    public T type(Locator locator, String value) {
        locator.type(value);
        return self();
    }

    public T press(Locator locator, String key) {
        locator.press(key);
        return self();
    }

    public T check(Locator locator) {
        locator.check();
        return self();
    }

    public T uncheck(Locator locator) {
        locator.uncheck();
        return self();
    }

    public T selectOption(Locator locator, String value) {
        locator.selectOption(value);
        return self();
    }

    // Методы ожидания
    public T waitForSelector(String selector) {
        page.waitForSelector(selector);
        return self();
    }

    public T waitForUrl(String expectedUrl) {
        page.waitForURL(expectedUrl);
        return self();
    }

    public T waitForUrlContains(String expectedUrlPart) {
        page.waitForURL(url -> url.contains(expectedUrlPart));
        waitForLoadState();
        return self();
    }

    public T waitForTimeout(int milliseconds) {
        page.waitForTimeout(milliseconds);
        return self();
    }

    public T waitForLoadState() {
        page.waitForLoadState(LoadState.LOAD);
        return self();
    }


    public T waitForLoadStateNetwork() {
        page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions()
                .setTimeout(30000));
        return self();
    }


    public T waitForLocator(Locator locator) {
        locator.waitFor();
        return self();
    }

    public T waitForLocatorVisible(Locator locator) {
        locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        return self();
    }

    public T waitForLocatorHidden(Locator locator) {
        locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        return self();
    }

    public T closePopUCookie() {
        click("//button[contains(text(), 'OK')]");
        return self();
    }

    // Вспомогательные методы
    public void initComponents() {
        // Базовая реализация, можно переопределить в наследниках
    }

    // Геттеры
    public Page getPage() {
        return page;
    }

    public AllureUtils getAllure(){
        return TestContext.getAllureUtils();
    }

    public HighlightElements getHighlightElements() {
        return highlightElements;
    }
}
