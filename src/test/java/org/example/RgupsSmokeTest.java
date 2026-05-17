package org.example;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import lombok.SneakyThrows;
import org.example.ui.pages.RgupsMainPage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.example.ui.Constants.Capability.*;

@Execution(ExecutionMode.CONCURRENT)
public class RgupsSmokeTest extends BaseTest{

    @Test
    @DisplayName("1.Проверка авторизации и разлогина")
    @Description("Проверка авторизации пользователя в системе: вход в портал РГУПС и последующий выход из аккаунта")
    void loginAgentsTest() {
        new RgupsMainPage(getPage())
                .open("https://portal.rgups.ru/index.php?r=site/login")
                .auth()
                .logout();
    }

    @SneakyThrows
    @Test
    void parallelTest(){

        String apiKey =
                System.getenv("OPENROUTER_API_KEY");
        new RgupsMainPage(getPage())
                .open("https://portal.rgups.ru/index.php?r=site/login");
        Allure.step(OPEN_ROUTER_KEY);
        Allure.step(USER_1_LOGIN);
        Allure.step(USER_1_PASS);


    }

}
