package ru.extas.server.insurance;

import ru.extas.model.contacts.Employee;
import ru.extas.model.insurance.A7Form;

import java.util.List;
import java.util.Set;

/**
 * <p>A7FormService interface.</p>
 *
 * @author Valery Orlov
 *         Date: 18.12.13
 *         Time: 12:49
 * @version $Id: $Id
 * @since 0.3
 */
public interface A7FormService {
    /**
     * Использовать квитанцию
     *
     * @param formNum номер квитанции
     */
    void spendForm(String formNum);

    /**
     * Сменить владельца для набора квитанций
     *  @param formNums Список номеров квитанций
     * @param owner    Новый владелец
     */
    void changeOwner(List<String> formNums, Employee owner);

    /**
     * Сменить статус для набора квитанций
     *
     * @param form      квитанция
     * @param newStatus Новый статус
     */
    void changeStatus(A7Form form, A7Form.Status newStatus);

    /**
     * Сменить статус для набора квитанций
     *
     * @param forms      Список квитанций
     * @param newStatus Новый статус
     */
    void changeStatus(Set<A7Form> forms, A7Form.Status newStatus);

    /**
     * Загружает доступные пользователю квитанции А-7
     *
     * @return доступные пользователю квитанции А-7
     */
    List<A7Form> loadAvailable();
}
