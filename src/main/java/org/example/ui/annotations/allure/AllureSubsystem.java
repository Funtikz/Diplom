package org.example.ui.annotations.allure;

import io.qameta.allure.LabelAnnotation;

import java.lang.annotation.*;

/**
 * Аннотация для allure, для удобства фильтрации.
 * Реализовано в соответствии с https://confluence.mts.ru/pages/viewpage.action?pageId=1123089857#
 * Навешивается на тест со значениями agents / sercon / backoffice / arm / cms / dhw
 *
 * Пример:
 * @AllureSubsystem("agents")
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AllureSubsystems.class)
@Target({ElementType.METHOD, ElementType.TYPE})
@LabelAnnotation(name = "subsystem")
public @interface AllureSubsystem {
    String value();
}
