package org.example.ui.annotations.allure;

import io.qameta.allure.LabelAnnotation;

import java.lang.annotation.*;

/**
 * Аннотация для allure, для удобства фильтрации.
 * Реализовано в соответствии с https://confluence.mts.ru/pages/viewpage.action?pageId=1123089857#
 * Навешивается на тест со значениями Full Regress и/или Smart Regress (если кейс входит в смоук)
 *
 * Пример:
 * @AllureTestingType("Full Regress")
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AllureTestingTypes.class)
@Target({ElementType.METHOD, ElementType.TYPE})
@LabelAnnotation(name = "testingType")
public @interface AllureTestingType {
    String value();
}
