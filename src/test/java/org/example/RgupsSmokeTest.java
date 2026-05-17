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
    @Description("Проверка работы поисковой строки и перехода на страницу дополнительного профессионального образования")
    void searchTextTest(){

        RgupsMainPage page = auth()
                .fill("//input[@id='menu-search-desk']", "Образование")
                .pressEnter();

        getAllureUtils().takeScreenshot("Проверили работу поисковой строки");

        page.click("//a[text()='Дополнительное профессиональное образование']");

        Assertions.assertEquals(
                "https://dev.meetings.rgups.ru/dpo-programs",
                page.getUrl()
        );

        getAllureUtils().takeScreenshot(
                "Проверили успешный переход на страницу дополнительного профессионального образования"
        );
    }

    @SneakyThrows
    @Test
    @Description("Проверка отображения данных портфолио: фильтрация по семестру и корректность значения 'Семестр/заезд'")
    public void assertPortfolioSemesterValue() {

        String actualText = auth()

                .click("//a[text()='Портфолио']")
                .waitForTimeout(500)

                .click("(//*[text()='Работы обучающегося'])[2]")

                .selectByValue("//select[@name='EduWorkStudentSearch[semester]']", "7")

                .click("//button[@id='submit-button']")

                .click("//i[@class='icon-pencil']")

                .getText("//*[text()='Семестр/заезд']/parent::tr/td");

        getAllureUtils().takeScreenshot("Открыта форма редактирования работы обучающегося");

        getAllureUtils().takeScreenshot("Проверка значения поля 'Семестр/заезд'");

        Assertions.assertEquals("7", actualText);
    }

}
