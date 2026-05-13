package org.example.ui.driver;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.WaitForSelectorState;

public class HighlightElements {

    public Page page;
    private static final int HIGHLIGHT_TIMEOUT_MS = 2000;
    private static final int FALLBACK_TIMEOUT_MS = 5000;

    public HighlightElements(Page page){
        this.page = page;
    }

    public void setPage(Page page){
        this.page = page;
    }

    public void initializeHighlightStyles() {
        page.addStyleTag(new Page.AddStyleTagOptions()
                .setContent(
                        ".playwright-highlight-red {" +
                                "  border: 3px solid #ff0000 !important;" +
                                "  background-color: rgba(255, 0, 0, 0.1) !important;" +
                                "  box-shadow: 0 0 10px rgba(255, 0, 0, 0.5) !important;" +
                                "  transition: all 0.3s ease !important;" +
                                "}"
                ));
    }

    protected boolean highlightElement(Locator locator) {
        try {
            // Для локаторов с несколькими элементами используем first()
            if (locator.count() > 1) {
                locator.first().evaluate("""
                    element => {
                        element.style.border = '3px solid #ff0000';
                        element.style.backgroundColor = 'rgba(255, 0, 0, 0.1)';
                        element.style.boxShadow = '0 0 10px rgba(255, 0, 0, 0.5)';
                        element.style.transition = 'all 0.3s ease';
                    }
                """);
            } else {
                locator.evaluate("""
                    element => {
                        element.style.border = '3px solid #ff0000';
                        element.style.backgroundColor = 'rgba(255, 0, 0, 0.1)';
                        element.style.boxShadow = '0 0 10px rgba(255, 0, 0, 0.5)';
                        element.style.transition = 'all 0.3s ease';
                    }
                """);
            }
            page.waitForTimeout(200);
            return true;
        } catch (Exception e) {
            System.out.println("Не удалось выделить элемент: " + locator);
            return false;
        }
    }

    protected boolean removeHighlight(Locator locator) {
        try {
            page.evaluate("""
            (xpath) => {
                const element = document.evaluate(xpath, document, null, 
                    XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
                if (element) {
                    element.style.border = '';
                    element.style.backgroundColor = '';
                    element.style.boxShadow = '';
                }
            }
        """, locator.toString().replace("Locator@", ""));

            return true;

        } catch (Exception e) {
            System.out.println("Не удалось убрать выделение: " + locator);
            return false;
        }
    }

    public void clickWithHighlight(Locator locator) {
        clickWithHighlight(locator, HIGHLIGHT_TIMEOUT_MS);
    }

    public void clickWithHighlight(Locator locator, int timeoutMs) {
        long startTime = System.currentTimeMillis();

        try {
            // ВАЖНО: проверяем количество элементов с нашим таймаутом
            int elementCount = getElementCountWithTimeout(locator, timeoutMs);

            if (elementCount == 0) {
                throw new RuntimeException("Элемент не найден" + locator);
            }

            Locator targetLocator = elementCount > 1 ? locator.first() : locator;

            boolean highlighted = false;
            try {
                highlighted = highlightElement(locator);
                if (highlighted) {
                    page.waitForTimeout(300);
                }
            } catch (Exception e) {
                System.out.println("Не удалось выделить элемент: " + locator);
            }

            targetLocator.click(new Locator.ClickOptions().setTimeout(timeoutMs));

            if (highlighted) {
                try {
                    removeHighlight(locator);
                } catch (Exception e) {
                    System.out.println("Не удалось убрать выделение: " + locator);
                }
            }

        } catch (TimeoutError e) {
            System.out.println("Таймаут выделенного клика (" + timeoutMs + "ms). Пробуем обычный клик...");

            try {
                removeHighlight(locator);
            } catch (Exception ex) {
                System.out.println("Не удалось убрать выделение: " + locator);
            }

            // Fallback: обычный клик с нашим таймаутом
            try {
                int elementCount = getElementCountWithTimeout(locator, FALLBACK_TIMEOUT_MS);
                Locator targetLocator = elementCount > 1 ? locator.first() : locator;

                // Явно указываем таймаут для клика
                targetLocator.click(new Locator.ClickOptions().setTimeout(FALLBACK_TIMEOUT_MS));
                System.out.println("Обычный клик выполнен успешно");

            } catch (TimeoutError fallbackError) {
                System.out.println("Ошибка при обычном клике: " + locator);
                throw fallbackError;
            }

        } catch (Exception e) {
            System.out.println("Ошибка при выделенном клике: " + locator);
            try {
                removeHighlight(locator);
            } catch (Exception ex) {
                System.out.println("Не удалось убрать выделение: " + locator);
            }
            throw e;
        }

        long endTime = System.currentTimeMillis();
    }

    private int getElementCountWithTimeout(Locator locator, int timeoutMs) {
        try {
            return locator.locator("xpath=.").count();
        } catch (TimeoutError e) {
            try {
                locator.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(timeoutMs));
                return 1;
            } catch (TimeoutError e2) {
                return 0;
            }
        }
    }

    public void clickWithHighlight(String xPath) {
        clickWithHighlight(xPath, HIGHLIGHT_TIMEOUT_MS);
    }

    public void clickWithHighlight(String xPath, int timeoutMs) {
        Locator locator = page.locator(xPath);
        clickWithHighlight(locator, timeoutMs);
    }



}