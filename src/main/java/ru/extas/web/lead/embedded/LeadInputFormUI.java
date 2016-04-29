package ru.extas.web.lead.embedded;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.extas.model.contacts.Company;
import ru.extas.model.contacts.Employee;
import ru.extas.model.contacts.SalePoint;
import ru.extas.model.lead.Lead;
import ru.extas.model.security.AccessRole;
import ru.extas.server.common.AddressAccessService;
import ru.extas.server.contacts.CompanyRepository;
import ru.extas.server.contacts.SalePointRepository;
import ru.extas.server.lead.LeadRepository;
import ru.extas.server.motor.MotorBrandRepository;
import ru.extas.server.motor.MotorTypeRepository;
import ru.extas.server.security.UserManagementService;
import ru.extas.web.commons.ExtaTheme;
import ru.extas.web.commons.Fontello;
import ru.extas.web.commons.NotificationUtil;
import ru.extas.web.commons.component.EditField;
import ru.extas.web.commons.component.EmailField;
import ru.extas.web.commons.component.ExtaFormLayout;
import ru.extas.web.commons.component.PhoneField;
import ru.extas.web.contacts.salepoint.SalePointSimpleSelect;
import ru.extas.web.lead.LeadSourceSelect;
import ru.extas.web.motor.MotorBrandSelect;
import ru.extas.web.motor.MotorTypeSelect;
import ru.extas.web.reference.RegionSelect;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.extas.server.ServiceLocator.lookup;
import static ru.extas.web.UiUtils.initUi;
import static ru.extas.web.UiUtils.showValidationError;

/**
 * Форма ввода лида для внедрения во внешние порталы
 *
 * @author Valery Orlov
 *         Date: 14.04.2014
 *         Time: 16:43
 * @version $Id: $Id
 * @since 0.4.2
 */
@Component
@Scope("session")
@Theme(ExtaTheme.NAME)
@Title("Extreme Assistance CRM")
public class LeadInputFormUI extends UI {

    private final static Logger logger = LoggerFactory.getLogger(LeadInputFormUI.class);

    // Имя контакта
    @PropertyId("contactName")
    private EditField contactNameField;

    @PropertyId("contactPhone")
    private PhoneField cellPhoneField;
    // Эл. почта
    @PropertyId("contactEmail")
    private EmailField contactEmailField;
    // Регион проживания клиента
    @PropertyId("contactRegion")
    private RegionSelect contactRegionField;
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
    // Регион покупки техники
    @PropertyId("region")
    private RegionSelect regionField;
    // Мотосалон
    @PropertyId("pointOfSale")
    private EditField pointOfSaleField;
    @PropertyId("vendor")
    private SalePointSimpleSelect vendorField;
    @PropertyId("comment")
    private TextArea commentField;

    private FieldGroup fieldGroup;
    private Company company;

    /** {@inheritDoc} */
    @Override
    protected void init(final VaadinRequest request) {

        initUi(this);

        final Lead lead = new Lead();
        lead.setStatus(Lead.Status.NEW);

        // Прочитать параметры адресной строки
        final Map<String, String[]> params = request.getParameterMap();
        // Пользовательский стиль
        final String customCss = getParamValue("custom_css", params);
        if (!isNullOrEmpty(customCss))
            getPage().getStyles().add(new ExternalResource(customCss));
        if (initLead(lead, params)) return;

        final FormLayout form = new ExtaFormLayout();
        form.setSizeUndefined();

        contactNameField = new EditField("Имя", "Введите фамилию имя отчество");
        contactNameField.setInputPrompt("Фамилия Имя Отчество");
        contactNameField.setColumns(25);
        contactNameField.setRequired(true);
        contactNameField.setImmediate(true);
        form.addComponent(contactNameField);

        cellPhoneField = new PhoneField("Телефон");
        cellPhoneField.setRequired(true);
        cellPhoneField.setImmediate(true);
        form.addComponent(cellPhoneField);

        contactEmailField = new EmailField("E-Mail");
        contactEmailField.setRequired(true);
        contactEmailField.setImmediate(true);
        form.addComponent(contactEmailField);

        contactRegionField = new RegionSelect("Регион проживания");
        contactRegionField.setDescription("Укажите регион проживания");
        contactRegionField.setRequired(true);
        form.addComponent(contactRegionField);

        motorTypeField = new MotorTypeSelect();
        motorTypeField.setInputPrompt("Выберите тип...");
        motorTypeField.setRequired(true);
        form.addComponent(motorTypeField);

        motorBrandField = new MotorBrandSelect();
        motorBrandField.setInputPrompt("Выберите марку...");
        motorBrandField.setRequired(true);
        form.addComponent(motorBrandField);

        motorModelField = new EditField("Модель техники", "Введите модель техники");
        motorModelField.setColumns(15);
        motorModelField.setRequired(true);
        form.addComponent(motorModelField);

        mototPriceField = new EditField("Цена техники");
        mototPriceField.setRequired(true);
        form.addComponent(mototPriceField);

        regionField = new RegionSelect("Регион покупки");
        regionField.setDescription("Укажите регион покупки техники");
        if (company == null && lead.getVendor() == null) {
            regionField.setRequired(true);
            form.addComponent(regionField);
        }

        pointOfSaleField = new EditField("Мотосалон");
        pointOfSaleField.setColumns(25);
        pointOfSaleField.setImmediate(true);
        if (company == null && lead.getVendor() == null) {
            pointOfSaleField.setRequired(true);
            form.addComponent(pointOfSaleField);
        }

        vendorField = new SalePointSimpleSelect("Мотосалон", "Выберите мотосалон");
        vendorField.setInputPrompt("Выберите мотосалон...");
        vendorField.addValueChangeListener(event -> {
            final Property property = event.getProperty();
            if (property != null) {
                final Object value = property.getValue();
                if (value != null) {
                    pointOfSaleField.setValue(((SalePoint) vendorField.getConvertedValue()).getName());
                    regionField.setValue(((SalePoint) vendorField.getConvertedValue()).getPosAddress().getRegionWithType());
                }
            }
        });
        if (company != null && lead.getVendor() == null) {
            vendorField.setRequired(true);
            vendorField.setContainerFilter(company, null);
            form.addComponent(vendorField);
        }

        commentField = new TextArea("Примечание");
        commentField.setInputPrompt("Укажите любую дополнительную информацию");
        commentField.setRows(3);
        commentField.setColumns(20);
        commentField.setNullRepresentation("");
        form.addComponent(commentField);

        // Привязываем поля
        final BeanItem<Lead> beanItem = new BeanItem<>(lead);
        fieldGroup = new FieldGroup(beanItem);
        fieldGroup.setBuffered(true);
        fieldGroup.bindMemberFields(this);

        final VerticalLayout panel = new VerticalLayout(form);
        panel.setMargin(true);

        // Кнопка ввода
        final Button submitBtn = new Button("Отправить", new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(final Button.ClickEvent event) {

                if (fieldGroup.isValid()) {
                    try {
                        fieldGroup.commit();
                        saveObject(lead);
                        NotificationUtil.show(
                                "Заявка отправлена!",
                                "В ближайшее время наш менеджер свяжется с вами для уточнения деталей.");
                        LeadInputFormUI.this.setContent(new HorizontalLayout());
                    } catch (final FieldGroup.CommitException e) {
                        // TODO Correct error handling
                        logger.error("Can't apply form changes", e);
                        NotificationUtil.showError("Невозможно отправить данные!", e.getLocalizedMessage());
                        return;
                    }
                    close();
                } else {
                    final String caption = "Невозможно отправить данные!";
                    showValidationError(caption, fieldGroup);
                }
            }

        });

        submitBtn.setIcon(Fontello.OK);
                submitBtn.setClickShortcut(ShortcutAction.KeyCode.ENTER, ShortcutAction.ModifierKey.CTRL);

        panel.addComponent(submitBtn);

        setContent(panel);

    }

    private boolean initLead(final Lead lead, final Map<String, String[]> params) {
        // Компания
        final String[] companyPrm = params.get("company");
        if (companyPrm != null && companyPrm.length > 0) {
            final String companyId = companyPrm[0];
            if (!isNullOrEmpty(companyId)) {
                final CompanyRepository companyRepository = lookup(CompanyRepository.class);
                company = companyRepository.findOne(companyId);
                if (company == null) {
                    NotificationUtil.showError("Неверные параметры формы!",
                            "Неверно задан идентификатор компании в параметре 'company'.");
                    return true;
                }
            } else {
                NotificationUtil.showError("Неверные параметры формы!",
                        "Неверно задан идентификатор компании в параметре 'company'.");
                return true;
            }
        } else { // или торговая точка
            final String[] salePointPrm = params.get("salepoint");
            if (salePointPrm != null && salePointPrm.length > 0) {
                final String salePointId = salePointPrm[0];
                if (!isNullOrEmpty(salePointId)) {
                    final SalePointRepository salePointRepository = lookup(SalePointRepository.class);
                    final SalePoint salePoint = salePointRepository.findOne(salePointId);
                    if (salePoint == null) {
                        NotificationUtil.showError("Неверные параметры формы!",
                                "Неверно задан идентификатор торговой точки в параметре 'salepoint'.");
                        return true;
                    } else
                        lead.setVendor(salePoint);
                } else {
                    NotificationUtil.showError("Неверные параметры формы!",
                            "Неверно задан идентификатор торговой точки в параметре 'salepoint'.");
                    return true;
                }
            }
        }

        // Имя клиента
        final String contactName = getParamValue("contactName", params);
        if (!isNullOrEmpty(contactName))
            lead.setContactName(contactName);
        // Телефон клиента
        final String contactPhone = getParamValue("contactPhone", params);
        if (!isNullOrEmpty(contactPhone))
            lead.setContactPhone(contactPhone);
        // Эл. почта
        final String contactEmail = getParamValue("contactEmail", params);
        if (!isNullOrEmpty(contactEmail))
            lead.setContactEmail(contactEmail);
        // Регион проживания клиента
        String contactRegion = getParamValue("contactRegion", params);
        if (!isNullOrEmpty(contactRegion)) {
            contactRegion = contactRegion.trim();
            final AddressAccessService service = lookup(AddressAccessService.class);
            final Collection<String> regions = service.loadRegions();
            final String finalContactRegion = contactRegion;
            final Optional<String> trueRegion = Iterables.tryFind(regions, input -> StringUtils.containsIgnoreCase(input, finalContactRegion));
            lead.setContactRegion(trueRegion.orNull());
        }
        // Тип техники
        String motorType = getParamValue("motorType", params);
        if (!isNullOrEmpty(motorType)) {
            motorType = motorType.trim();
            final MotorTypeRepository repository = lookup(MotorTypeRepository.class);
            final List<String> types = repository.loadAllNames();
            final String finalMotorType = motorType;
            final Optional<String> trueType = Iterables.tryFind(types, input -> StringUtils.containsIgnoreCase(input, finalMotorType));
            lead.setMotorType(trueType.orNull());
        }
        // Марка техники
        String motorBrand = getParamValue("motorBrand", params);
        if (!isNullOrEmpty(motorBrand)) {
            motorBrand = motorBrand.trim();
            final MotorBrandRepository repository = lookup(MotorBrandRepository.class);
            final List<String> brands = repository.loadAllNames();
            final String finalMotorBrand = motorBrand;
            final Optional<String> trueMotorBrand = Iterables.tryFind(brands, input -> StringUtils.containsIgnoreCase(input, finalMotorBrand));
            lead.setMotorBrand(trueMotorBrand.orNull());
        }
        // Модель техники
        final String motorModel = getParamValue("motorModel", params);
        if (!isNullOrEmpty(motorModel))
            lead.setMotorModel(motorModel);
        // Стоимость техники
        final String motorPrice = getParamValue("motorPrice", params);
        if (!isNullOrEmpty(motorPrice)) {
            final DecimalFormat format = (DecimalFormat) NumberFormat.getNumberInstance(lookup(Locale.class));
            format.setParseBigDecimal(true);
            final BigDecimal price = (BigDecimal) format.parse(motorPrice, new ParsePosition(0));
            if (price == null) {
                NotificationUtil.showError("Неверные параметры формы!",
                        "Неверно задана сумма в параметре 'motorPrice'.");
                return true;
            }
            lead.setMotorPrice(price);
        }
        // Регион покупки техники
        String region = getParamValue("region", params);
        if (!isNullOrEmpty(region)) {
            region = region.trim();
            final AddressAccessService service = lookup(AddressAccessService.class);
            final Collection<String> regions = service.loadRegions();
            final String finalregion = region;
            final Optional<String> trueRegion = Iterables.tryFind(regions, input -> StringUtils.containsIgnoreCase(input, finalregion));
            lead.setRegion(trueRegion.orNull());
        }
        // Примечание
        final String comment = getParamValue("comment", params);
        if (!isNullOrEmpty(comment))
            lead.setComment(comment);

        // Источник лида
        final String source = getParamValue("source", params);
        if (!isNullOrEmpty(source))
            lead.setSource(source);
        else
            lead.setSource(LeadSourceSelect.DEALER_SITE);

        return false;
    }

    private String getParamValue(final String prmName, final Map<String, String[]> params) {
        final String[] contactNamePrm = params.get(prmName);
        if (contactNamePrm != null && contactNamePrm.length > 0)
            return contactNamePrm[0];
        return null;
    }

    private void saveObject(final Lead lead) {
        final LeadRepository leadRepository = lookup(LeadRepository.class);
        final UserManagementService userService = lookup(UserManagementService.class);

        // Определить потенциального пользователя
        Employee user = null;
        if (lead.getVendor() != null) {
            final Set<Employee> employees = lead.getVendor().getEmployees();
            if (!isEmpty(employees))
                user = employees.iterator().next();
        }
        if (user == null)
            user = userService.findUserEmployeeByLogin("admin");

        leadRepository.permitAndSave(lead, new ImmutablePair<>(user.getId(), AccessRole.OWNER));
    }


}
