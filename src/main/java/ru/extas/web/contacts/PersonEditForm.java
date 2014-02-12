/**
 *
 */
package ru.extas.web.contacts;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.extas.model.AddressInfo;
import ru.extas.model.Person;
import ru.extas.model.Person.Sex;
import ru.extas.server.ContactRepository;
import ru.extas.server.SupplementService;
import ru.extas.web.commons.component.EditField;
import ru.extas.web.commons.component.EmailField;
import ru.extas.web.commons.component.LocalDateField;
import ru.extas.web.commons.component.PhoneField;
import ru.extas.web.commons.window.AbstractEditForm;
import ru.extas.web.reference.CitySelect;
import ru.extas.web.reference.RegionSelect;
import ru.extas.web.util.ComponentUtil;

import static ru.extas.server.ServiceLocator.lookup;

/**
 * @author Valery Orlov
 */
@SuppressWarnings("FieldCanBeLocal")
public class PersonEditForm extends AbstractEditForm<Person> {

	private static final long serialVersionUID = -7787385620289376599L;
	private final static Logger logger = LoggerFactory.getLogger(PersonEditForm.class);
	// Компоненты редактирования
// Основные персональные данные
	@PropertyId("name")
	private EditField nameField;
	@PropertyId("birthday")
	private PopupDateField birthdayField;
	@PropertyId("sex")
	private ComboBox sexField;
	@PropertyId("phone")
	private EditField cellPhoneField;
	@PropertyId("email")
	private EmailField emailField;
	@PropertyId("actualAddress.region")
	private RegionSelect regionField;
	@PropertyId("actualAddress.city")
	private CitySelect cityField;
	@PropertyId("actualAddress.postIndex")
	private EditField postIndexField;
	@PropertyId("actualAddress.streetBld")
	private TextArea streetBldField;
	// Компания
	@PropertyId("affiliation")
	private AbstractContactSelect jobField;
	@PropertyId("jobPosition")
	private ComboBox jobPositionField;
	@PropertyId("jobDepartment")
	private EditField jobDepartmentField;
	// Паспортнве данные
	@PropertyId("passNum")
	private EditField passNumField;
	@PropertyId("passIssueDate")
	private LocalDateField passIssueDateField;
	@PropertyId("passIssuedBy")
	private TextArea passIssuedByField;
	@PropertyId("passIssuedByNum")
	private EditField passIssuedByNumField;


	/**
	 * @param caption
	 * @param obj
	 */
	public PersonEditForm(final String caption, final BeanItem<Person> obj) {
		super(caption, obj);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ru.extas.web.commons.window.AbstractEditForm#initObject(ru.extas.model.
	 * AbstractExtaObject)
	 */
	@Override
	protected void initObject(final Person obj) {
		if (obj.getActualAddress() == null)
			obj.setActualAddress(new AddressInfo());
		if (obj.getId() == null) {
			// Инициализируем новый объект
			// TODO: Инициализировать клиента в соответствии с локацией текущего
			// пользователя (регион, город)
			obj.setSex(Person.Sex.MALE);
			obj.setJobPosition(Person.Position.EMPLOYEE);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ru.extas.web.commons.window.AbstractEditForm#saveObject(ru.extas.model.
	 * AbstractExtaObject)
	 */
	@Override
	protected void saveObject(final Person obj) {
		logger.debug("Saving contact data...");
		final ContactRepository contactRepository = lookup(ContactRepository.class);
		contactRepository.save(obj);
		Notification.show("Контакт сохранен", Notification.Type.TRAY_NOTIFICATION);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * ru.extas.web.commons.window.AbstractEditForm#checkBeforeSave(ru.extas.model.
	 * AbstractExtaObject)
	 */
	@Override
	protected void checkBeforeSave(final Person obj) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * ru.extas.web.commons.window.AbstractEditForm#createEditFields(ru.extas.model
	 * .AbstractExtaObject)
	 */
	@Override
	protected ComponentContainer createEditFields(final Person obj) {
		TabSheet tabsheet = new TabSheet();
		tabsheet.setSizeUndefined();

		// Форма редактирования персональных данных
		final FormLayout personForm = createMainForm(obj);
		tabsheet.addTab(personForm).setCaption("Общие данные");

		// Форма редактирования данных о компании
		final FormLayout companyForm = createCompanyForm();
		tabsheet.addTab(companyForm).setCaption("Компания");

		// Форма редактирования паспортных данных
		final FormLayout passForm = createPassForm();
		tabsheet.addTab(passForm).setCaption("Паспортные данные");

		return tabsheet;
	}

	private FormLayout createPassForm() {
		final FormLayout passForm = new FormLayout();
		passForm.setMargin(true);

		passNumField = new EditField("Номер паспорта");
		passNumField.setColumns(20);
		passForm.addComponent(passNumField);

		passIssueDateField = new LocalDateField("Дата выдачи", "Дата выдачи документа");
		passForm.addComponent(passIssueDateField);

		passIssuedByField = new TextArea("Кем выдан");
		passIssuedByField.setDescription("Наименование органа выдавшего документ");
		passIssuedByField.setInputPrompt("Наименование органа выдавшего документ");
		passIssuedByField.setNullRepresentation("");
		passIssuedByField.setRows(3);
		passIssuedByField.setColumns(30);
		passForm.addComponent(passIssuedByField);

		passIssuedByNumField = new EditField("Код подразделения");
		passForm.addComponent(passIssuedByNumField);
		return passForm;
	}

	private FormLayout createCompanyForm() {
		final FormLayout companyForm = new FormLayout();
		companyForm.setMargin(true);

		jobField = new CompanySelect("Компания");
		jobField.setDescription("Компания в которой работает контакт");
		companyForm.addComponent(jobField);

		jobPositionField = new ComboBox("Должность");
		jobPositionField.setWidth(15, Unit.EM);
		jobPositionField.setDescription("Укажите должность контакта");
		jobPositionField.setNullSelectionAllowed(false);
		jobPositionField.setNewItemsAllowed(false);
		ComponentUtil.fillSelectByEnum(jobPositionField, Person.Position.class);
		companyForm.addComponent(jobPositionField);

		jobDepartmentField = new EditField("Департамент");
		jobDepartmentField.setDescription("Подразделение в котором работает контакт");
		jobDepartmentField.setColumns(20);
		companyForm.addComponent(jobDepartmentField);
		return companyForm;
	}

	private FormLayout createMainForm(final Person obj) {
		final FormLayout personForm = new FormLayout();
		personForm.setMargin(true);

		nameField = new EditField("Имя");
		nameField.setColumns(30);
		nameField.setDescription("Введите имя (ФИО) контакта");
		nameField.setInputPrompt("Фамилия Имя (Отчество)");
		nameField.setRequired(true);
		nameField.setRequiredError("Имя контакта не может быть пустым. Пожалуйста введите ФИО контакта.");
		personForm.addComponent(nameField);

		sexField = new ComboBox("Пол");
		sexField.setDescription("Укажите пол контакта");
		sexField.setRequired(true);
		sexField.setNullSelectionAllowed(false);
		sexField.setNewItemsAllowed(false);
		ComponentUtil.fillSelectByEnum(sexField, Sex.class);
		personForm.addComponent(sexField);

		birthdayField = new PopupDateField("Дата рождения");
		birthdayField.setImmediate(true);
		birthdayField.setInputPrompt("31.12.1978");
		birthdayField.setDescription("Введите дату рождения контакта");
		birthdayField.setDateFormat("dd.MM.yyyy");
		birthdayField.setConversionError("{0} не является допустимой датой. Формат даты: ДД.ММ.ГГГГ");
		personForm.addComponent(birthdayField);

		cellPhoneField = new PhoneField("Мобильный телефон");
		personForm.addComponent(cellPhoneField);

		emailField = new EmailField("E-Mail");
		personForm.addComponent(emailField);

		regionField = new RegionSelect();
		regionField.setDescription("Укажите регион проживания");
		regionField.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				final String newRegion = (String) event.getProperty().getValue();
				final String city = lookup(SupplementService.class).findCityByRegion(newRegion);
				if (city != null)
					cityField.setValue(city);
			}
		});
		personForm.addComponent(regionField);

		cityField = new CitySelect();
		cityField.setDescription("Введите город проживания контакта");
		if (obj.getActualAddress().getCity() != null)
			cityField.addItem(obj.getActualAddress().getCity());
		cityField.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				final String newCity = (String) event.getProperty().getValue();
				final String region = lookup(SupplementService.class).findRegionByCity(newCity);
				if (region != null)
					regionField.setValue(region);
			}
		});
		personForm.addComponent(cityField);

		postIndexField = new EditField("Почтовый индекс");
		postIndexField.setColumns(8);
		postIndexField.setInputPrompt("Индекс");
		postIndexField.setNullRepresentation("");
		personForm.addComponent(postIndexField);

		streetBldField = new TextArea("Адрес");
		streetBldField.setColumns(30);
		streetBldField.setRows(5);
		streetBldField.setDescription("Почтовый адрес (улица, дом, корпус, ...)");
		streetBldField.setInputPrompt("Улица, Дом, Корпус и т.д.");
		streetBldField.setNullRepresentation("");
		personForm.addComponent(streetBldField);
		return personForm;
	}
}
