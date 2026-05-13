package org.example.ui.annotations.allure;

/**
 * Аннотация для allure, для удобства фильтрации.
 * Навешивается на тест со значениями таски в Jira
 *
 * Пример:
 * @JiraIssues({@Jira("AGENTS-559"),@Jira("AGENTS-49"),@Jira("AGENTS-470"),@Jira("AGENTS-389")})
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
public @interface JiraIssues {
    Jira[] value();
}
