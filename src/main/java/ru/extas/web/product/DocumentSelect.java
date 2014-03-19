package ru.extas.web.product;

import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.ComboBox;
import ru.extas.server.SupplementService;

import static ru.extas.server.ServiceLocator.lookup;

/**
 * Поле выбора документа
 *
 * @author Valery Orlov
 *         Date: 08.02.14
 *         Time: 14:11
 * @version $Id: $Id
 */
public class DocumentSelect extends ComboBox {
	/**
	 * <p>Constructor for DocumentSelect.</p>
	 *
	 * @param caption a {@link java.lang.String} object.
	 */
	public DocumentSelect(final String caption) {
		super(caption);
		setDescription("Выберите вид документа");
		setInputPrompt("Выберите документ...");
		setImmediate(true);
		setNullSelectionAllowed(false);
		setNewItemsAllowed(false);
		setFilteringMode(FilteringMode.CONTAINS);
		setWidth(20, Unit.EM);
		for (final String item : lookup(SupplementService.class).loadDocumentTypes())
			addItem(item);
	}
}
