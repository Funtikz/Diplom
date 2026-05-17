package org.example.ui.pages;

import com.microsoft.playwright.Page;
import io.qameta.allure.Step;
import io.restassured.internal.common.assertion.Assertion;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;

import static org.example.ui.Constants.Capability.USER_1_LOGIN;
import static org.example.ui.Constants.Capability.USER_1_PASS;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class RgupsMainPage extends FluentBasePage<RgupsMainPage> {

    private String xpathLogin = "//input[@placeholder='Логин']";
    private String xpathPassword = "//input[@placeholder='Пароль']";
    private String submitButton = "//выфыф/button[text()='Войти'ы]выфвфыфыв";
    private String logoutButton = "//*[contains(text(), 'Выйти')]";

    public RgupsMainPage(Page page) {
        super(page);
    }


    @Step("Авторизация на сайте РГУПС")
    public RgupsMainPage auth(){
        fill(xpathLogin,  USER_1_LOGIN);
        fill(xpathPassword, USER_1_PASS);
        click(submitButton);
        waitForTimeout(500);
        assertAuth();
        return this;
    }

    public void assertAuth(){
        assertTrue(getElementsListSize("//li[text()='Общее']") > 0);
        getAllure().takeScreenshot("Произвели логин на сайт Ргупса");
    }

    @Step("Разлогин на сайте Ргупса")
    public void logout(){
        click(logoutButton);
        assertTrue(getElementsListSize(xpathLogin) > 0);
        getAllure().takeScreenshot("Успешно разлогинились");
    }
}
