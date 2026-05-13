package org.example.ui.annotations.allure;

/**
 * Аннотация для allure, для удобства фильтрации.
 *
 * Пример:
 * @AllureSubsystems({@AllureSubsystem("agents")}) для сценариев в агентах
 * @AllureSubsystems({@AllureSubsystem("agents"),@AllureSubsystem("sercon")}) для сценариев в агентах и серконе
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
public @interface AllureSubsystems {
    AllureSubsystem[] value();
}
