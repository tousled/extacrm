package ru.extas.web.insurance;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.*;
import org.joda.time.LocalDate;
import ru.extas.model.contacts.Contact;
import ru.extas.model.contacts.LegalEntity;
import ru.extas.model.contacts.Person;
import ru.extas.model.insurance.Insurance;
import ru.extas.model.insurance.Policy;
import ru.extas.server.insurance.InsuranceCalculator;
import ru.extas.server.insurance.InsuranceRepository;
import ru.extas.server.insurance.PolicyRepository;
import ru.extas.server.security.UserManagementService;
import ru.extas.web.commons.DocFilesEditor;
import ru.extas.web.commons.component.EditField;
import ru.extas.web.commons.component.LocalDateField;
import ru.extas.web.commons.converters.StringToPercentConverter;
import ru.extas.web.commons.window.AbstractEditForm;
import ru.extas.web.contacts.LegalEntitySelect;
import ru.extas.web.contacts.PersonSelect;
import ru.extas.web.contacts.SalePointSelect;
import ru.extas.web.motor.MotorBrandSelect;
import ru.extas.web.motor.MotorTypeSelect;
import ru.extas.web.util.ComponentUtil;

import java.math.BigDecimal;

import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.extas.server.ServiceLocator.lookup;

/**
 * Форма ввода/редактирования имущественной страховки
 *
 * @author Valery Orlov
 * @version $Id: $Id
 * @since 0.3.0
 */
public class InsuranceEditForm extends AbstractEditForm<Insurance> {

    private static final long serialVersionUID = 9510268415882116L;
    // Компоненты редактирования
    @PropertyId("regNum")
    private PolicySelect regNumField;
    @PropertyId("a7Num")
    private A7Select a7FormNumField;
    @PropertyId("date")
    private PopupDateField dateField;
    private CheckBox isLegalEntityField;
    @PropertyId("client")
    private AbstractField<? extends Contact> clientNameField;
    @PropertyId("beneficiary")
    private ComboBox beneficiaryField;
    @PropertyId("usedMotor")
    private CheckBox usedMotorField;
    @PropertyId("motorType")
    private MotorTypeSelect motorTypeField;
    @PropertyId("motorBrand")
    private MotorBrandSelect motorBrandField;
    @PropertyId("motorModel")
    private TextField motorModelField;
    @PropertyId("motorVin")
    private EditField motorVinField;
    @PropertyId("saleNum")
    private EditField saleNumField;
    @PropertyId("saleDate")
    private LocalDateField saleDateField;
    @PropertyId("riskSum")
    private TextField riskSumField;
    @PropertyId("coverTime")
    private OptionGroup coverTimeField;
    @PropertyId("premium")
    private TextField premiumField;
    @PropertyId("paymentDate")
    private PopupDateField paymentDateField;
    @PropertyId("startDate")
    private PopupDateField startDateField;
    @PropertyId("endDate")
    private PopupDateField endDateField;
    @PropertyId("dealer")
    private SalePointSelect dealerField;
    @PropertyId("files")
    private DocFilesEditor docFilesEditor;
    @PropertyId("docComplete")
    private CheckBox docCompleteField;

    private Label tarifField;
    private ObjectProperty<BigDecimal> tarifDataSource;

    /**
     * <p>Constructor for InsuranceEditForm.</p>
     *
     * @param caption a {@link java.lang.String} object.
     * @param obj     a {@link com.vaadin.data.util.BeanItem} object.
     */
    public InsuranceEditForm(final String caption, final BeanItem<Insurance> obj) {
        super(caption, obj);
    }

    /** {@inheritDoc} */
    @Override
    protected ComponentContainer createEditFields(final Insurance obj) {
        TabSheet tabsheet = new TabSheet();
        tabsheet.setSizeUndefined();

        final FormLayout polyceForm = createPolyceForm(obj);
        tabsheet.addTab(polyceForm).setCaption("Данные полиса");

        final Component docsForm = createDocsForm();
        tabsheet.addTab(docsForm).setCaption("Документы");

        return tabsheet;
    }

    private Component createDocsForm() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        docFilesEditor = new DocFilesEditor();
        layout.addComponent(docFilesEditor);

        docCompleteField = new CheckBox("Полный комплект документов");
        docCompleteField.setDescription("Укажите когда все документы загружены");
        layout.addComponent(docCompleteField);

        return layout;
    }

    private FormLayout createPolyceForm(Insurance obj) {
        final FormLayout form = new FormLayout();

        regNumField = new PolicySelect("Номер полиса",
                "Введите номер полиса страхования. Выбирается из справочника БСО.", obj.getRegNum());
        regNumField.setRequired(true);
        // regNumField.setConverter(String.class);
        regNumField.addValueChangeListener(new ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unchecked")
            @Override
            public void valueChange(final ValueChangeEvent event) {
                if (event.getProperty().getType() == Policy.class) {
                    final Object selItemId = event.getProperty().getValue();
                    if (selItemId != null) {
                        final BeanItem<Policy> policy = (BeanItem<Policy>) regNumField.getItem(selItemId);
                        // Зарезервировать полис в БСО
                        lookup(PolicyRepository.class).bookPolicy(policy.getBean());
                    }

                }
            }
        });
        form.addComponent(regNumField);

        a7FormNumField = new A7Select("Номер квитанции А-7",
                "Введите номер квитанции А-7. Выбирается из справочника БСО.", obj.getA7Num());
        // TODO a7FormNumField.setRequired(true);
        form.addComponent(a7FormNumField);

        dateField = new LocalDateField("Дата договора", "Введите дату оформления договора страхования");
        dateField.setRequired(true);
        dateField.addValueChangeListener(new ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(final ValueChangeEvent event) {
                // пересчитать дату оплаты страховки
                final LocalDate newDate = (LocalDate) dateField.getConvertedValue();
                if (newDate != null && paymentDateField.getPropertyDataSource() != null)
                    paymentDateField.setConvertedValue(newDate);
            }
        });
        form.addComponent(dateField);

        boolean isLegalEntity = obj.getClient() != null && obj.getClient() instanceof LegalEntity;
        isLegalEntityField = new CheckBox("Страхователь Юр.лицо", isLegalEntity);
        isLegalEntityField.setDescription("Отметте флаг, если страхователь является юр.лицом");
        isLegalEntityField.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                Boolean isLegalEntity1 = isLegalEntityField.getValue();
                createAndBindClientNameField(isLegalEntity1, form);
            }
        });
        form.addComponent(isLegalEntityField);

        createAndBindClientNameField(isLegalEntity, form);

        beneficiaryField = new ComboBox("Выгодопреобретатель");
        beneficiaryField.setDescription("Введите имя выгодопреобретателя по данному договору страхования");
        beneficiaryField.setNewItemsAllowed(true);
        beneficiaryField.setRequired(true);
        beneficiaryField.setWidth(25, Unit.EM);
        fillBeneficiariesChoice(clientNameField.getPropertyDataSource() != null ? clientNameField.getValue() : obj.getClient());
        form.addComponent(beneficiaryField);

        usedMotorField = new CheckBox("Б/у техника");
        usedMotorField.setDescription("Признак бывшей в употреблении техники");
        usedMotorField.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(final ValueChangeEvent event) {
                calculatePremium();
                updateTarifField();
            }
        });
        form.addComponent(usedMotorField);

        motorTypeField = new MotorTypeSelect();
        motorTypeField.setRequired(true);
        form.addComponent(motorTypeField);

        motorBrandField = new MotorBrandSelect();
        motorBrandField.setRequired(true);
        motorBrandField.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(final ValueChangeEvent event) {
                calculatePremium();
                updateTarifField();
            }
        });
        motorBrandField.linkToType(motorTypeField);
        form.addComponent(motorBrandField);

        tarifDataSource = new ObjectProperty<>(new BigDecimal(0));
        tarifField = new Label(tarifDataSource);
        tarifField.setCaption("Тариф");
        tarifField.setConverter(lookup(StringToPercentConverter.class));
        form.addComponent(tarifField);

        motorModelField = new EditField("Модель техники", "Введите модель техники");
        motorModelField.setRequired(true);
        motorModelField.setColumns(25);
        form.addComponent(motorModelField);

        motorVinField = new EditField("VIN", "Введите Идентификационный номер транспортного средства");
        motorVinField.setRequired(true);
        motorVinField.setColumns(20);
        form.addComponent(motorVinField);

        saleNumField = new EditField("Номер договора купли-продажи", "Введите номер договора купли-продажи");
        saleNumField.setRequired(true);
        saleNumField.setColumns(20);
        form.addComponent(saleNumField);

        saleDateField = new LocalDateField("Дата договора купли-продажи", "Введите дату договора купли-продажи");
        saleDateField.setRequired(true);
        form.addComponent(saleDateField);

        riskSumField = new EditField("Страховая сумма", "Введите сумму возмещения в рублях");
        riskSumField.addValueChangeListener(new ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(final ValueChangeEvent event) {
                calculatePremium();
            }
        });
        riskSumField.setRequired(true);
        form.addComponent(riskSumField);

        // По умолчанию страховка на год.
        if (obj.getCoverTime() == null)
            obj.setCoverTime(Insurance.PeriodOfCover.YEAR);
        coverTimeField = new OptionGroup("Срок страхования");
        coverTimeField.setDescription("Укажите требуемый срок действия страхового полиса");
        coverTimeField.setRequired(true);
        coverTimeField.setNullSelectionAllowed(false);
        coverTimeField.setNewItemsAllowed(false);
        coverTimeField.setImmediate(true);
        ComponentUtil.fillSelectByEnum(coverTimeField, Insurance.PeriodOfCover.class);
        coverTimeField.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                calculatePremium();
                calcEndDate();
                updateTarifField();
            }
        });
        form.addComponent(coverTimeField);

        premiumField = new EditField("Страховая премия", "Введите стоимость страховки в рублях");
        premiumField.setRequired(true);
        form.addComponent(premiumField);

        paymentDateField = new LocalDateField("Дата оплаты", "Введите дату оплаты страховой премии");
        paymentDateField.setRequired(true);
        paymentDateField.addValueChangeListener(new ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(final ValueChangeEvent event) {
                // пересчитать дату начала действия договора
                final LocalDate newDate = (LocalDate) paymentDateField.getConvertedValue();
                final LocalDate startDate = ((LocalDate) startDateField.getConvertedValue());
                if (newDate != null && startDate != null && newDate.isAfter(startDate))
                    startDateField.setConvertedValue(newDate.plusDays(1));
            }
        });
        form.addComponent(paymentDateField);

        startDateField = new LocalDateField("Дата начала договора", "Введите дату начала действия страхового договора");
        startDateField.setRequired(true);
        startDateField.addValueChangeListener(new ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(final ValueChangeEvent event) {
                calcEndDate();

            }
        });
        form.addComponent(startDateField);

        endDateField = new LocalDateField("Дата окончания договора", "Введите дату окончания");
        endDateField.setRequired(true);
        form.addComponent(endDateField);

        dealerField = new SalePointSelect("Точка продажи", "Название мотосалона где продана страховка", null);
        // dealerField.setRequired(true);
        form.addComponent(dealerField);
        return form;
    }

    private void fillBeneficiariesChoice(Contact client) {
        // Очищаем все
        beneficiaryField.removeAllItems();
        if (client != null)
            beneficiaryField.addItem(client.getName());
        // Добавляем заданных выгодопреобретателей
        beneficiaryField.addItem("ВТБ24 (ЗАО)");
        beneficiaryField.addItem("ООО \"Финпрайд\"");
    }

    /**
     * <p>createAndBindClientNameField.</p>
     *
     * @param isLegalEntity a {@link java.lang.Boolean} object.
     * @param form a {@link com.vaadin.ui.FormLayout} object.
     */
    protected void createAndBindClientNameField(Boolean isLegalEntity, FormLayout form) {
        AbstractField<? extends Contact> select;
        String caption = "Страхователь";
        // FIXME Ограничить выбор контакта только клиентами
        if (isLegalEntity) {
            select = new LegalEntitySelect(caption);
        } else {
            select = new PersonSelect(caption);
        }
        select.setRequired(true);
        select.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                Contact contact = clientNameField.getValue();
                if (beneficiaryField.getPropertyDataSource() != null && contact != null) {
                    fillBeneficiariesChoice(contact);
                    beneficiaryField.setValue(contact.getName());
                }
            }
        });

        if (clientNameField != null) {
            clientNameField.getPropertyDataSource().setValue(null);
            getFieldGroup().unbind(clientNameField);
            getFieldGroup().bind(select, "client");
            form.replaceComponent(clientNameField, select);
        } else {
            form.addComponent(select);
        }

        clientNameField = select;
    }

    private void updateTarifField() {
        if (motorBrandField.getPropertyDataSource() != null &&
                coverTimeField.getContainerDataSource() != null &&
                usedMotorField.getPropertyDataSource() != null) {
            final Insurance.PeriodOfCover coverPeriod = (Insurance.PeriodOfCover) coverTimeField.getConvertedValue();
            final String motorBrand = (String) motorBrandField.getValue();
            Boolean isUsed = usedMotorField.getValue();
            if (coverPeriod != null && motorBrand != null && isUsed != null) {
                final InsuranceCalculator calc = lookup(InsuranceCalculator.class);
                final BigDecimal premium = calc.findTarif(motorBrand, coverPeriod, isUsed);
                tarifDataSource.setValue(premium);
            }
        }
    }

    private void calcEndDate() {
        // пересчитать дату окончания договора
        final LocalDate newDate = (LocalDate) startDateField.getConvertedValue();
        if (newDate != null && endDateField.getPropertyDataSource() != null) {
            final Insurance.PeriodOfCover coverPeriod = (Insurance.PeriodOfCover) coverTimeField.getConvertedValue();
            if (coverPeriod == Insurance.PeriodOfCover.YEAR)
                endDateField.setConvertedValue(newDate.plusYears(1).minusDays(1));
            else
                endDateField.setConvertedValue(newDate.plusMonths(6).minusDays(1));
        }
    }


    /** {@inheritDoc} */
    @Override
    protected void initObject(final Insurance obj) {
        if (obj.getId() == null) {
            final LocalDate now = LocalDate.now();
            obj.setDate(now);
            obj.setPaymentDate(now);
            obj.setCoverTime(Insurance.PeriodOfCover.YEAR);
            obj.setStartDate(obj.getPaymentDate().plusDays(1));
            obj.setEndDate(obj.getStartDate().plusYears(1).minusDays(1));
            UserManagementService userService = lookup(UserManagementService.class);
            Person user = userService.getCurrentUserContact();
            if(user != null) {
                if(!isEmpty(user.getWorkPlaces()))
                    obj.setDealer(user.getWorkPlaces().iterator().next());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void saveObject(final Insurance obj) {
        lookup(InsuranceRepository.class).saveAndIssue(obj);
        Notification.show("Полис сохранен", Notification.Type.TRAY_NOTIFICATION);
    }

    /** {@inheritDoc} */
    @Override
    protected void checkBeforeSave(final Insurance obj) {
    }

    /**
     *
     */
    private void calculatePremium() {
        if (motorBrandField.getPropertyDataSource() != null &&
                premiumField.getPropertyDataSource() != null &&
                coverTimeField.getContainerDataSource() != null &&
                usedMotorField.getPropertyDataSource() != null) {
            final Insurance.PeriodOfCover coverPeriod = (Insurance.PeriodOfCover) coverTimeField.getConvertedValue();
            final BigDecimal riskSum = (BigDecimal) riskSumField.getConvertedValue();
            final String motorBrand = (String) motorBrandField.getValue();
            boolean isUsedMotor = usedMotorField.getValue();
            if (riskSum != null && motorBrand != null) {
                final InsuranceCalculator calc = lookup(InsuranceCalculator.class);
                final BigDecimal premium = calc.calcPropInsPremium(new Insurance(motorBrand, riskSum, coverPeriod, isUsedMotor));
                premiumField.setConvertedValue(premium);
            }
        }
    }

}
