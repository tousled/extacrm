package ru.extas.web.motor;

import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.ComboBox;
import ru.extas.server.motor.MotorTypeRepository;

import static ru.extas.server.ServiceLocator.lookup;

/**
 * Выбор типа техники (справочник)
 *
 * @author Valery Orlov
 *         Date: 23.10.13
 *         Time: 15:09
 * @version $Id: $Id
 * @since 0.3
 */
public class MotorTypeSelect extends ComboBox {
    /**
     * <p>Constructor for MotorTypeSelect.</p>
     *
     * @param caption a {@link java.lang.String} object.
     */
    public MotorTypeSelect(final String caption) {
        super(caption);
        setDescription("Укажите тип техники");
        setInputPrompt("Выберите...");
        setRequiredError(String.format("Поле '%s' не может быть пустым", caption));
        setImmediate(true);
        setNullSelectionAllowed(false);
        setNewItemsAllowed(false);
        setFilteringMode(FilteringMode.CONTAINS);
        setWidth(13, Unit.EM);
        lookup(MotorTypeRepository.class).loadAllNames().forEach((final String item) -> addItem(item));
    }

    /**
     * <p>Constructor for MotorTypeSelect.</p>
     */
    public MotorTypeSelect() {
        this("Тип техники");
    }
}