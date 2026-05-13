package org.example.ui.annotations.allure;

import io.qameta.allure.LabelAnnotation;

import java.lang.annotation.*;

/**
 * Аннотация для allure, для удобства фильтрации.
 * Реализовано в соответствии с https://confluence.mts.ru/pages/viewpage.action?pageId=1123089857#
 * Навешивается на тест со значениями Автоматизирован, Не автоматизирован, Запланирован к автоматизации, На автоматизации, Приемка автоматизации, Автоматизация неприменима
 *
 * Пример:
 * @AllureAutomationStatus("Автоматизирован")
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@LabelAnnotation(name = "automationStatus")
public @interface AllureAutomationStatus {
    String value();
}
