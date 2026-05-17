package org.example;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import lombok.SneakyThrows;
import org.example.ui.pages.RgupsMainPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
public class RgupsSmokeTest extends BaseTest{

    public RgupsMainPage auth(){
        return new RgupsMainPage(getPage())
                .open("https://portal.rgups.ru/index.php?r=site/login")
                .auth();
    }



    @Test
    @DisplayName("1.Проверка авторизации и разлогина")
    @Description("Проверка авторизации пользователя в системе: вход в портал РГУПС и последующий выход из аккаунта")
    void loginAgentsTest() {
        auth().logout();
    }

    @SneakyThrows
    @Test
    void searchTextTest(){
        RgupsMainPage page = auth().fill("//input[@id='menu-search-desk']", "Образование");
        getAllureUtils().takeScreenshot("Проверили что поисковая строка верно работает");
        page.click("//a[text()='Дополнительное профессиональное образование']");
        Assertions.assertTrue(page.getUrl().equals("https://dev.meetings.rgups.ru/dpo-programs"));
        getAllureUtils().takeScreenshot("Проверили что переход на страницу https://dev.meetings.rgups.ru/dpo-programs - успешно выполнен");
    }

}
