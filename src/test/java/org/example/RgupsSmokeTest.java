package org.example;

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

//    @SneakyThrows
//    @Test
//    @DisplayName("Поиск: проверка работы поисковой строки и перехода на страницу ДПО")
//    @Description("Проверка работы поисковой строки и перехода на страницу дополнительного профессионального образования")
//    void searchTextTest(){
//        //input[@id='menu-search-desk']
//        RgupsMainPage page = auth()
//                .fill("//input[@id = 'menu-search-desktop']", "Образование")
//                .pressEnter();
//
//        getAllureUtils().takeScreenshot("Проверили работу поисковой строки");
//
//        page.click("//a[text()='Дополнительное профессиональное образование']");
//
//        Assertions.assertEquals(
//                "https://dev.meetings.rgups.ru/dpo-programs",
//                page.getUrl()
//        );
//
//        getAllureUtils().takeScreenshot(
//                "Проверили успешный переход на страницу дополнительного профессионального образования"
//        );
//    }

    @SneakyThrows
    @Test
    @DisplayName("Портфолио: проверка фильтрации по семестру и значения поля 'Семестр/заезд'")
    @Description("Проверка отображения данных портфолио: фильтрация по семестру и корректность значения 'Семестр/заезд'")
    public void assertPortfolioSemesterValue() {

        String actualText = auth()

                .click("//a[text()='Портфолио']")
                .waitForTimeout(500)

                .click("(//*[text()='Работы обучающегося'])[2]")

                .selectByValue("//select[@name='EduWorkStudentSearch[semester]']", "7")

                .click("//button[@id='submit-button']")

                .click("//i[@class='icon-pencil']")
                .click("//*[text()='Семестр/заезд']/parent::tr/td")
                .waitForTimeout(300)
                .getText("//*[text()='Семестр/заезд']/parent::tr/td");

        getAllureUtils().takeScreenshot("Открыта форма редактирования работы обучающегося");

        getAllureUtils().takeScreenshot("Проверка значения поля 'Семестр/заезд'");

        Assertions.assertEquals("7", actualText);
    }

    @SneakyThrows
    @DisplayName("Вакансии: проверка соответствия данных в списке и карточке вакансии")
    @Test
    @Description("Проверяет, что название вакансии и зарплата в списке совпадают с данными в карточке вакансии")
    void shouldMatchVacancyDataBetweenListAndCard() {

        RgupsMainPage rgupsPage = auth()
                .click("//a[text()='Вакансии']");

        getAllureUtils().takeScreenshot("Открыли раздел Вакансии");

        rgupsPage.click("//a[text()='Просмотр вакансий']");

        getAllureUtils().takeScreenshot("Открыли список вакансий");

        String name = rgupsPage.getText(
                "(//tr[td/a[contains(@href,'vacancy')]]/td[2])[1]"
        );

        String salary = rgupsPage.getText(
                "(//tr[td/a[contains(@href,'vacancy')]]/td[4])[1]"
        );

        getAllureUtils().takeScreenshot("Получили данные первой вакансии из списка");

        rgupsPage.click("(//tr[td/a[contains(@href,'vacancy')]]/td[5])[1]");
        Thread.sleep(200);

        getAllureUtils().takeScreenshot("Открыли карточку вакансии");

        rgupsPage.click("//tr[th[normalize-space()='Зарплата']]/td");
        String currentSalary = rgupsPage.getText(
                "//tr[th[normalize-space()='Зарплата']]/td"
        );

        rgupsPage.click("(//h3)[1]");
        String currentName = rgupsPage.getText("(//h3)[1]");

        getAllureUtils().takeScreenshot("Сравниваем данные вакансии с карточкой");

        Assertions.assertAll(
                () -> Assertions.assertEquals(name, currentName, "Название вакансии не совпадает"),
                () -> Assertions.assertEquals(salary, currentSalary, "Зарплата вакансии не совпадает")
        );
    }


    @SneakyThrows
    @DisplayName("Онлайн расписание — проверка отображения данных за выбранную неделю")
    @Description("Проверка, что в разделе 'Онлайн расписание' после выбора конкретной недели и применения фильтра отображаются данные в таблице расписания")
    @Test
    public void shouldDisplayScheduleForSelectedWeek() {

        RgupsMainPage rgupsMainPage = new RgupsMainPage(getPage());
        auth()
                .scrollToElement("//a[text()='Онлайн расписание']")
                .hover("//a[text()='Онлайн расписание']")
                .click("//a[text() = 'Мое расписание']")
                .selectByValue("//select[@id='week_timestamps']", "09.03.2026 - 15.03.2026")
                .click("//button[text()='Подобрать']");

         getAllureUtils().takeScreenshot("Открыли расписание и применили фильтр по неделе");

        Assertions.assertTrue(
                rgupsMainPage.isTableNotEmpty("table"),
                "Таблица расписания пустая"
        );

        getAllureUtils().takeScreenshot("Таблица расписания содержит данные");
    }





}
