package org.example.ui.driver;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;

import static org.example.ui.Constants.Capability.BROWSER;


public class BrowserManager {
    public static Browser getBrowser(final Playwright playwright) {
        return BrowserFactory.valueOf(BROWSER).createInstance(playwright);
    }
}