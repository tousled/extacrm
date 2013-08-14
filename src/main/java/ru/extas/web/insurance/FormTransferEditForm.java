package ru.extas.web.insurance;

import static ru.extas.server.ServiceLocator.lookup;

import org.joda.time.LocalDate;

import ru.extas.model.FormTransfer;
import ru.extas.server.FormTransferService;
import ru.extas.web.commons.AbstractEditForm;
import ru.extas.web.commons.component.LocalDateField;
import ru.extas.web.contacts.ContactSelect;

import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.PopupDateField;

/**
 * Форма ввода/редактирования имущественной страховки
 * 
 * @author Valery Orlov
 * 
 */
public class FormTransferEditForm extends AbstractEditForm<FormTransfer> {

	private static final long serialVersionUID = 9510268415882116L;

	// Компоненты редактирования
	@PropertyId("fromContact")
	private ComboBox fromContactField;
	@PropertyId("toContact")
	private ComboBox toContactField;
	@PropertyId("transferDate")
	private PopupDateField transferDateField;
	@PropertyId("formNums")
	private A7NumListEdit formNums;

	public FormTransferEditForm(final String caption, final BeanItem<FormTransfer> obj) {
		super(caption, obj);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ru.extas.web.commons.AbstractEditForm#createEditFields(ru.extas.model
	 * .AbstractExtaObject)
	 */
	@Override
	protected FormLayout createEditFields(final FormTransfer obj) {
		final FormLayout form = new FormLayout();

		fromContactField = new ContactSelect("От кого");
		fromContactField.setRequired(true);
		form.addComponent(fromContactField);

		toContactField = new ContactSelect("Кому");
		toContactField.setRequired(true);
		form.addComponent(toContactField);

		transferDateField = new LocalDateField("Дата приема/передачи", "Введите дату акта приема/передачи");
		transferDateField.setRequired(true);
		form.addComponent(transferDateField);

		formNums = new A7NumListEdit("Номера квитанций");
		formNums.setRequired(true);
		form.addComponent(formNums);

		return form;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ru.extas.web.commons.AbstractEditForm#initObject(ru.extas.model.
	 * AbstractExtaObject)
	 */
	@Override
	protected void initObject(final FormTransfer obj) {
		if (obj.getKey() == null) {
			final LocalDate now = LocalDate.now();
			obj.setTransferDate(now);
			// TODO: Инициализировать поле "От" текущим пользователем
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ru.extas.web.commons.AbstractEditForm#saveObject(ru.extas.model.
	 * AbstractExtaObject)
	 */
	@Override
	protected void saveObject(final FormTransfer obj) {
		final FormTransferService service = lookup(FormTransferService.class);
		service.persist(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ru.extas.web.commons.AbstractEditForm#checkBeforeSave(ru.extas.model.
	 * AbstractExtaObject)
	 */
	@Override
	protected void checkBeforeSave(final FormTransfer obj) {
	}

}
