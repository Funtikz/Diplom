package org.example.ui.annotations.allure;

import io.qameta.allure.LabelAnnotation;

import java.lang.annotation.*;

/**
 * Аннотация для allure, для удобства фильтрации.
 * Реализовано в соответствии с https://confluence.mts.ru/pages/viewpage.action?pageId=1123089857#
 * Навешивается на тест со значениями Критический, Высокий, Средний, Низкий
 *
 * Пример:
 * @AllurePriority("Средний")
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@LabelAnnotation(name = "priority")
public @interface AllurePriority {
    String value();
}
