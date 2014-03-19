package ru.extas.web.contacts;

import ru.extas.model.Company;
import ru.extas.model.SalePoint;
import ru.extas.web.commons.DefaultAction;
import ru.extas.web.commons.UIAction;
import ru.extas.web.commons.window.CloseOnlylWindow;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static ru.extas.web.commons.GridItem.extractBean;

/**
 * Окно выбора торговой точки
 *
 * @author Valery Orlov
 *         Date: 21.02.14
 *         Time: 13:40
 * @version $Id: $Id
 */
public class SalePointSelectWindow extends CloseOnlylWindow {
	private SalePoint selected;
	private boolean selectPressed;

	/**
	 * <p>Constructor for SalePointSelectWindow.</p>
	 *
	 * @param caption a {@link java.lang.String} object.
	 * @param company a {@link ru.extas.model.Company} object.
	 */
	public SalePointSelectWindow(final String caption, final Company company) {
		super(caption);
		addStyleName("base-view");
		setContent(new SelectGrid(company));
	}

	/**
	 * <p>isSelectPressed.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isSelectPressed() {
		return selectPressed;
	}

	private class SelectGrid extends SalePointsGrid {
		public SelectGrid(final Company company) {
			super(company);
		}

		@Override
		protected List<UIAction> createActions() {
			List<UIAction> actions = newArrayList();

			actions.add(new DefaultAction("Выбрать", "Выбрать выделенный в списке контакт и закрыть окно", "icon-check") {
				@Override
				public void fire(final Object itemId) {

					selected = extractBean(table.getItem(itemId));
					selectPressed = true;
					close();
				}
			});

			actions.addAll(super.createActions());

			return actions;
		}
	}

	/**
	 * <p>Getter for the field <code>selected</code>.</p>
	 *
	 * @return a {@link ru.extas.model.SalePoint} object.
	 */
	public SalePoint getSelected() {
		return selected;
	}
}
