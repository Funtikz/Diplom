package org.example.ui.annotations.allure;

import io.qameta.allure.LabelAnnotation;

import java.lang.annotation.*;

/**
 * Аннотация для allure, для удобства фильтрации.
 * Реализовано в соответствии с https://confluence.mts.ru/pages/viewpage.action?pageId=1123089857#
 * Навешивается на тест со значениями LeadGen, LeadView, AccountCreation, Other
 *
 * Пример:
 * @AllureBusinessProcess("LeadGen")
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AllureBusinessProcesses.class)
@Target({ElementType.METHOD, ElementType.TYPE})
@LabelAnnotation(name = "businessProcess")
public @interface AllureBusinessProcess {
    String value();
}
