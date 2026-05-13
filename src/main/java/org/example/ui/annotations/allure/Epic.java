package org.example.ui.annotations.allure;

import io.qameta.allure.LabelAnnotation;

import java.lang.annotation.*;

/**
 * Аннотация для allure, для удобства фильтрации.
 * Навешивается на тест со значениями ui, api, unit
 *
 * Пример:
 * @Epic("ui")
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@LabelAnnotation(name = "epic")
public @interface Epic {

    String value();

}
