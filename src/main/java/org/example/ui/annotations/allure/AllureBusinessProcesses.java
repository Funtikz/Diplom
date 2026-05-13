package org.example.ui.annotations.allure;

/**
 * Аннотация для allure, для удобства фильтрации.
 *
 * Пример:
 * @AllureBusinessProcesses({@AllureBusinessProcess("LeadSending")}) для сценариев отправки лида
 * @AllureBusinessProcesses({@AllureBusinessProcess("LeadView")}) для сценариев связанных со списком и отображением лидов
 * @AllureBusinessProcesses({@AllureBusinessProcess("AccountFlow")}) для сценариев создания/изменения контрагента
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
public @interface AllureBusinessProcesses {
    AllureBusinessProcess[] value();
}
