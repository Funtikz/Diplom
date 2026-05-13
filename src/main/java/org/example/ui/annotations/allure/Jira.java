package org.example.ui.annotations.allure;

import io.qameta.allure.LabelAnnotation;

import java.lang.annotation.*;

/**
 * Аннотация для allure, для удобства фильтрации.
 * Навешивается на тест со значениями таски в Jira
 *
 * Пример:
 * @Jira("AGENTS-123")
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(JiraIssues.class)
@Target({ElementType.METHOD, ElementType.TYPE})
@LabelAnnotation(name = "jira")
public @interface Jira {

    String value();

}
