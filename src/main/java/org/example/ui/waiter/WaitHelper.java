package org.example.ui.waiter;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class WaitHelper {
    public static void waitForElementCount(Page page, String xpath, int expectedCount,
                                           int timeoutMs, int pollMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (page.locator(xpath).count() == expectedCount) {
                return;
            }
            page.waitForTimeout(pollMs);
        }
    }
}
