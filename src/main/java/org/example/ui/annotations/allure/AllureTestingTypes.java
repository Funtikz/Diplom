package org.example.ui.annotations.allure;

/**
 * Аннотация для allure, для удобства фильтрации.
 *
 * Пример:
 * @AllureTestingTypes({@AllureTestingType("Full Regress"),@AllureTestingType("Smart Regress")}) для смоук тестов
 * @AllureTestingTypes({@AllureTestingType("Full Regress")}) для регресса
 */

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@API(
        status = Status.STABLE,
        since = "5.0"
)
public @interface AllureTestingTypes {
    AllureTestingType[] value();
}
