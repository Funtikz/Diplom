package org.example.ui.driver;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

import java.util.List;


public enum BrowserFactory {

    CHROMIUM {
        @Override
        public Browser createInstance(final Playwright playwright) {
            return playwright.chromium().launch(options());
        }
    },
    FIREFOX {
        @Override
        public Browser createInstance(final Playwright playwright) {
            return playwright.firefox().launch(options());
        }
    },
    WEBKIT {
        @Override
        public Browser createInstance(final Playwright playwright) {
            return playwright.webkit().launch(options());
        }
    };

    public BrowserType.LaunchOptions options() {
        return new BrowserType.LaunchOptions()
                .setHeadless(checkHeadless())
                .setSlowMo(1000)
                .setArgs(List.of(
                        "--disable-blink-features=AutomationControlled",  // основной stealth
                        "--disable-features=VizDisplayCompositor",        // отключает CDP
                        "--disable-extensions",
                        "--disable-plugins",
                        // "--disable-images",  // экономит трафик
                        "--no-sandbox",
                        "--disable-setuid-sandbox"
                ));
    }

    public boolean checkHeadless(){
        // Проверяем переменные окружения - они автоматически установлены в CI/CD
        return System.getenv("CI") != null ||      // GitLab CI, GitHub Actions
                System.getenv("GITLAB_CI") != null;
    }

    public abstract Browser createInstance(final Playwright playwright);
}