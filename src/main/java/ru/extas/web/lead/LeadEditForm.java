package ru.extas.web.lead;

import com.google.common.base.Strings;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Or;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.*;
import ru.extas.model.AddressInfo;
import ru.extas.model.Company;
import ru.extas.model.Lead;
import ru.extas.model.Person;
import ru.extas.server.LeadRepository;
import ru.extas.server.LeadService;
import ru.extas.web.commons.ExtaDataContainer;
import ru.extas.web.commons.GridDataDecl;
import ru.extas.web.commons.component.EditField;
import ru.extas.web.commons.component.EmailField;
import ru.extas.web.commons.component.PhoneField;
import ru.extas.web.commons.window.AbstractEditForm;
import ru.extas.web.contacts.CompanyDataDecl;
import ru.extas.web.contacts.CompanyEditForm;
import ru.extas.web.contacts.PersonDataDecl;
import ru.extas.web.contacts.PersonEditForm;
import ru.extas.web.reference.MotorBrandSelect;
import ru.extas.web.reference.MotorTypeSelect;
import ru.extas.web.reference.RegionSelect;

import java.text.MessageFormat;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static ru.extas.server.ServiceLocator.lookup;
import static ru.extas.web.commons.TableUtils.initTableColumns;

/**
 * Форма ввода/редактирования лида
 *
 * @author Valery Orlov
 */
public class LeadEditForm extends AbstractEditForm<Lead> {

	private static final long serialVersionUID = 9510268415882116L;
	// Компоненты редактирования
// Имя контакта
	@PropertyId("contactName")
	private EditField contactNameField;
	@PropertyId("contactPhone")
	private PhoneField cellPhoneField;
	// Эл. почта
	@PropertyId("contactEmail")
	private EmailField contactEmailField;
	// Регион покупки техники
	@PropertyId("region")
	private RegionSelect regionField;
	// Тип техники
	@PropertyId("motorType")
	private ComboBox motorTypeField;
	// Марка техники
	@PropertyId("motorBrand")
	private MotorBrandSelect motorBrandField;
	// Модель техники
	@PropertyId("motorModel")
	private EditField motorModelField;
	// Стоимость техники
	@PropertyId("motorPrice")
	private EditField mototPriceField;
	// Мотосалон
	@PropertyId("pointOfSale")
	private EditField pointOfSaleField;
	@PropertyId("comment")
	private TextArea commentField;

	private boolean qualifyForm;
	private JPAContainer<Company> vendorsContainer;
	private JPAContainer<Person> clientsContainer;

	public LeadEditForm(final String caption, final BeanItem<Lead> obj, boolean qualifyForm) {
		super(caption);
		this.qualifyForm = qualifyForm;
		initForm(obj);
	}

	@Override
	public void attach() {
		super.attach();
		setFieldsStatus();
	}

	private void setFieldsStatus() {
//        contactNameField.setReadOnly(qualifyForm);
//        cellPhoneField.setReadOnly(qualifyForm);
//        contactEmailField.setReadOnly(qualifyForm);
//        regionField.setReadOnly(qualifyForm);
//        motorTypeField.setReadOnly(qualifyForm);
//        motorBrandField.setReadOnly(qualifyForm);
//        motorModelField.setReadOnly(qualifyForm);
//        mototPriceField.setReadOnly(qualifyForm);
//        pointOfSaleField.setReadOnly(qualifyForm);
//        commentField.setReadOnly(qualifyForm);
	}

	private Person createPersonFromLead(Lead lead) {
		Person person = new Person();
		person.setName(lead.getContactName());
		person.setPhone(lead.getContactPhone());
		person.setEmail(lead.getContactEmail());
		person.setActualAddress(new AddressInfo(lead.getRegion(), null, null, null));
		return person;

	}

	private Company createCompanyFromLead(Lead lead) {
		Company company = new Company();
		company.setName(lead.getPointOfSale());
		company.setActualAddress(new AddressInfo(lead.getRegion(), null, null, null));
		return company;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * ru.extas.web.commons.window.AbstractEditForm#createEditFields(ru.extas.model
	 * .AbstractExtaObject)
	 */
	@Override
	protected ComponentContainer createEditFields(final Lead obj) {
		final FormLayout form = new FormLayout();

		contactNameField = new EditField("Клиент", "Введите имя клиента");
		contactNameField.setColumns(25);
		contactNameField.setRequired(true);
		contactNameField.setRequiredError("Имя контакта не может быть пустым.");
		contactNameField.setImmediate(true);
		contactNameField.addTextChangeListener(new FieldEvents.TextChangeListener() {
			@Override
			public void textChange(FieldEvents.TextChangeEvent event) {
				if (qualifyForm)
					setClientsFilter(event.getText());
			}
		});
		contactNameField.addValueChangeListener(new ConactChangeListener());
		form.addComponent(contactNameField);

		cellPhoneField = new PhoneField("Телефон");
		cellPhoneField.setImmediate(true);
		cellPhoneField.addValueChangeListener(new ConactChangeListener());
		form.addComponent(cellPhoneField);

		contactEmailField = new EmailField("E-Mail");
		contactEmailField.setImmediate(true);
		contactEmailField.addValueChangeListener(new ConactChangeListener());
		form.addComponent(contactEmailField);

		regionField = new RegionSelect();
		regionField.setDescription("Укажите регион услуги");
		form.addComponent(regionField);

		motorTypeField = new MotorTypeSelect();
		form.addComponent(motorTypeField);

		motorBrandField = new MotorBrandSelect();
		form.addComponent(motorBrandField);

		motorModelField = new EditField("Модель техники", "Введите модель техники");
		motorModelField.setColumns(15);
		form.addComponent(motorModelField);

		mototPriceField = new EditField("Цена техники");
		form.addComponent(mototPriceField);

		pointOfSaleField = new EditField("Мотосалон");
		pointOfSaleField.setImmediate(true);
		pointOfSaleField.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (qualifyForm)
					setVendorsFilter();
			}
		});
		form.addComponent(pointOfSaleField);


		commentField = new TextArea("Комментарий");
		commentField.setColumns(25);
		commentField.setRows(6);
		commentField.setNullRepresentation("");
		form.addComponent(commentField);

		if (qualifyForm) {
			HorizontalLayout layout = new HorizontalLayout(form, createQualifyForm(obj));
			layout.setSpacing(true);
			return layout;
		} else
			return form;
	}

	private Component createQualifyForm(Lead lead) {
		Component clientPanel = createClientPanel(lead);
		Component vendorPanel = createVendorPanel(lead);
		VerticalLayout qForm = new VerticalLayout(clientPanel, vendorPanel);
		qForm.setSpacing(true);
		qForm.setExpandRatio(clientPanel, 1);
		qForm.setExpandRatio(vendorPanel, 1);
		return qForm;
	}

	private Panel createVendorPanel(final Lead lead) {
		VerticalLayout panel = new VerticalLayout();
		panel.setSpacing(true);

		final Table table = new Table();
		table.setRequired(true);
		// Запрос данных
		vendorsContainer = new ExtaDataContainer<>(Company.class);
		vendorsContainer.addNestedContainerProperty("actualAddress.region");
		setVendorsFilter();

		Button newBtn = new Button("Новый");
		newBtn.addStyleName("icon-doc-new");
		newBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				final BeanItem<Company> newObj = new BeanItem<>(createCompanyFromLead(lead));
				newObj.expandProperty("actualAddress");

				final String edFormCaption = "Ввод нового контакта в систему";
				final CompanyEditForm editWin = new CompanyEditForm(edFormCaption, newObj);
				editWin.setModified(true);

				editWin.addCloseListener(new Window.CloseListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void windowClose(final Window.CloseEvent e) {
						if (editWin.isSaved()) {
							vendorsContainer.refresh();
							table.setValue(newObj.getBean().getId());
						}
					}
				});
				editWin.showModal();
			}
		});
		panel.addComponent(newBtn);

		// Общие настройки таблицы
		table.setContainerDataSource(vendorsContainer);
		table.setSelectable(true);
		table.setImmediate(true);
		table.setColumnCollapsingAllowed(true);
		table.setColumnReorderingAllowed(true);
		table.setNullSelectionAllowed(false);
		table.setHeight(10, Unit.EM);
		// Настройка столбцов таблицы
		table.setColumnHeaderMode(Table.ColumnHeaderMode.EXPLICIT);
		GridDataDecl dataDecl = new CompanyDataDecl();
		initTableColumns(table, dataDecl);
		table.setColumnCollapsed("phone", true);
		table.setColumnCollapsed("email", true);
		// Обрабатываем выбор контакта
		table.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				final Company curObj = ((EntityItem<Company>) table.getItem(table.getValue())).getEntity();
				lead.setVendor(curObj);
				pointOfSaleField.setValue(curObj.getName());
				setModified(true);
			}
		});
		panel.addComponent(table);

		return new Panel("Мото салон", panel);
	}

	private void setVendorsFilter() {
		vendorsContainer.removeAllContainerFilters();
		String name = pointOfSaleField.getValue();
		if (!Strings.isNullOrEmpty(name))
			vendorsContainer.addContainerFilter(new Like("name", MessageFormat.format("%{0}%", name), false));
	}

	private Panel createClientPanel(final Lead lead) {
		VerticalLayout panel = new VerticalLayout();
		panel.setSpacing(true);

		final Table table = new Table();
		table.setRequired(true);

		// Запрос данных
		clientsContainer = new ExtaDataContainer<>(Person.class);
		clientsContainer.addNestedContainerProperty("actualAddress.region");
		setClientsFilter(lead.getContactName());

		Button newBtn = new Button("Новый");
		newBtn.addStyleName("icon-doc-new");
		newBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				final BeanItem<Person> newObj = new BeanItem<>(createPersonFromLead(lead));
				newObj.expandProperty("actualAddress");

				final String edFormCaption = "Ввод нового контакта в систему";
				final PersonEditForm editWin = new PersonEditForm(edFormCaption, newObj);
				editWin.setModified(true);

				editWin.addCloseListener(new Window.CloseListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void windowClose(final Window.CloseEvent e) {
						if (editWin.isSaved()) {
							clientsContainer.refresh();
							table.setValue(newObj.getBean().getId());
						}
					}
				});
				editWin.showModal();

			}
		});
		panel.addComponent(newBtn);

		// Общие настройки таблицы
		table.setContainerDataSource(clientsContainer);
		table.setSelectable(true);
		table.setImmediate(true);
		table.setColumnCollapsingAllowed(true);
		table.setColumnReorderingAllowed(true);
		table.setNullSelectionAllowed(false);
		table.setHeight(10, Unit.EM);
		// Настройка столбцов таблицы
		table.setColumnHeaderMode(Table.ColumnHeaderMode.EXPLICIT);
		GridDataDecl dataDecl = new PersonDataDecl();
		initTableColumns(table, dataDecl);
		table.setColumnCollapsed("sex", true);
		table.setColumnCollapsed("birthday", true);
		table.setColumnCollapsed("email", true);
		// Обрабатываем выбор контакта
		table.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				final Person curObj = ((EntityItem<Person>) table.getItem(table.getValue())).getEntity();
				lead.setClient(curObj);
				fillLeadFromClient(curObj);
				setModified(true);
			}
		});
		panel.addComponent(table);


		return new Panel("Клиент", panel);
	}

	private void fillLeadFromClient(Person contact) {
		contactNameField.setValue(contact.getName());
		cellPhoneField.setValue(contact.getPhone());
		contactEmailField.setValue(contact.getEmail());
	}

	private void setClientsFilter(String name) {
		//String name = contactNameField.getValue();
		String email = contactEmailField.getValue();
		String cellPhone = cellPhoneField.getValue();
		clientsContainer.removeAllContainerFilters();
		List<Container.Filter> filters = newArrayList();
		if (!Strings.isNullOrEmpty(name)) {
			filters.add(new Like("name", MessageFormat.format("%{0}%", name), false));
		}
		if (!Strings.isNullOrEmpty(cellPhone)) {
			filters.add(new Like("phone", MessageFormat.format("%{0}%", cellPhone), false));
		}
		if (!Strings.isNullOrEmpty(email)) {
			filters.add(new Like("email", MessageFormat.format("%{0}%", email), false));
		}
		if (!filters.isEmpty())
			clientsContainer.addContainerFilter(new Or(filters.toArray(new Container.Filter[filters.size()])));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ru.extas.web.commons.window.AbstractEditForm#initObject(ru.extas.model.
	 * AbstractExtaObject)
	 */
	@Override
	protected void initObject(final Lead obj) {
		if (obj.getId() == null) {
			obj.setStatus(Lead.Status.NEW);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ru.extas.web.commons.window.AbstractEditForm#saveObject(ru.extas.model.
	 * AbstractExtaObject)
	 */
	@Override
	protected void saveObject(final Lead obj) {
		if (qualifyForm) {
			lookup(LeadService.class).qualify(obj);
			Notification.show("Лид квалифицирован", Notification.Type.TRAY_NOTIFICATION);
		} else {
			lookup(LeadRepository.class).save(obj);
			Notification.show("Лид сохранен", Notification.Type.TRAY_NOTIFICATION);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * ru.extas.web.commons.window.AbstractEditForm#checkBeforeSave(ru.extas.model.
	 * AbstractExtaObject)
	 */
	@Override
	protected void checkBeforeSave(final Lead obj) {
	}

	private enum Qualify {
		EXIST, NEW
	}

	private class ConactChangeListener implements Property.ValueChangeListener {
		@Override
		public void valueChange(Property.ValueChangeEvent event) {
			if (qualifyForm)
				setClientsFilter(contactNameField.getValue());
		}
	}
}
