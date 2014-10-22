package ru.extas.web.contacts.employee;

import com.vaadin.addon.jpacontainer.fieldfactory.SingleSelectConverter;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import ru.extas.model.contacts.Company;
import ru.extas.model.contacts.Employee;
import ru.extas.model.contacts.LegalEntity;
import ru.extas.web.commons.ExtaDataContainer;
import ru.extas.web.commons.ExtaTheme;
import ru.extas.web.commons.Fontello;
import ru.extas.web.commons.FormUtils;
import ru.extas.web.commons.component.ExtaFormLayout;
import ru.extas.web.commons.component.FormGroupHeader;
import ru.extas.web.commons.converters.PhoneConverter;

import java.util.Objects;
import java.util.Optional;

import static ru.extas.server.ServiceLocator.lookup;

/**
 * Выбор сотрудника с возможностью ввода нового
 *
 * @author Valery Orlov
 *         Date: 22.10.2014
 *         Time: 16:25
 */
public class EmployeeField extends CustomField<Employee> {

    private Company company;
    private PopupView popupView;
    private PopupEmployeeContent entityContent;

    public EmployeeField(String caption, String description, Company company) {
        setCaption(caption);
        setDescription(description);
        setBuffered(true);
        this.company = company;
    }

    @Override
    protected Component initContent() {
        entityContent = new PopupEmployeeContent();
        popupView = new PopupView(entityContent);
        popupView.setHideOnMouseOut(false);
        return popupView;
    }

    public void setCompany(Company company) {
        if (!Objects.equals(this.company, company)) {
            this.company = company;
            final Employee employee = getValue();
            if (employee != null && company != null && !employee.getCompany().equals(company)) {
                entityContent.refreshFields(null);
                markAsDirtyRecursive();
            }
        }
    }

    private class EmployeeSelectField extends ComboBox {

        private static final long serialVersionUID = -8005905898383483037L;
        protected ExtaDataContainer<Employee> container;

        protected EmployeeSelectField(final String caption) {
            this(caption, "Выберите существующего сотрудника или введите нового");
        }

        protected EmployeeSelectField(final String caption, final String description) {
            super(caption);

            // Преконфигурация
            setWidth(15, Unit.EM);
            setDescription(description);
            setInputPrompt("ФИО");
            setImmediate(true);

            // Инициализация контейнера
            container = new ExtaDataContainer<>(Employee.class);
            container.sort(new Object[]{"name"}, new boolean[]{true});
            setContainerFilter();

            // Устанавливаем контент выбора
            setFilteringMode(FilteringMode.CONTAINS);
            setContainerDataSource(container);
            setItemCaptionMode(ItemCaptionMode.PROPERTY);
            setItemCaptionPropertyId("name");
            setConverter(new SingleSelectConverter<Employee>(this));

            // Функционал добавления нового контакта
            setNullSelectionAllowed(false);
            setNewItemsAllowed(false);
        }

        /**
         * <p>refreshContainer.</p>
         */
        public void refreshContainer() {
            setContainerFilter();
            container.refresh();
            if (company != null && !Objects.equals(getConvertedValue(), company))
                setConvertedValue(null);
        }

        protected void setContainerFilter() {
            container.removeAllContainerFilters();
            if (company != null)
                container.addContainerFilter(new Compare.Equal("company", company));
        }

    }

    private class PopupEmployeeContent implements PopupView.Content {
        private EmployeeSelectField selectField;
        private Label emailField;
        private Label positionField;
        private Label phoneField;
        private Button viewBtn;

        @Override
        public String getMinimizedValueAsHTML() {
            final Employee employee = getValue();
            if (employee != null)
                return employee.getName();
            else
                return "Нажмите для выбора или ввода...";
        }

        @Override
        public Component getPopupComponent() {

            final ExtaFormLayout formLayout = new ExtaFormLayout();
            formLayout.setSpacing(true);

            formLayout.addComponent(new FormGroupHeader("Сотрудник"));

            selectField = new EmployeeSelectField("Имя", "Введите или выберите имя сотрудника");
            selectField.setPropertyDataSource(getPropertyDataSource());
            selectField.setNewItemsAllowed(true);
            selectField.setNewItemHandler(new AbstractSelect.NewItemHandler() {
                private static final long serialVersionUID = 1L;

                @SuppressWarnings({"unchecked"})
                @Override
                public void addNewItem(final String newItemCaption) {
                    final Employee newObj = new Employee();
                    newObj.setCompany(company);
                    newObj.setName(newItemCaption);

                    final EmployeeEditForm editWin = new EmployeeEditForm(newObj, company);
                    editWin.setModified(true);

                    editWin.addCloseFormListener(event -> {
                        if (editWin.isSaved()) {
                            selectField.refreshContainer();
                            selectField.setValue(editWin.getObjectId());
                        }
                        popupView.setPopupVisible(false);
                    });
                    FormUtils.showModalWin(editWin);
                }
            });
            selectField.addValueChangeListener(event -> refreshFields((Employee) selectField.getConvertedValue()));
            formLayout.addComponent(selectField);

            // Телефон
            phoneField = new Label();
            phoneField.setCaption("Телефон");
            phoneField.setConverter(lookup(PhoneConverter.class));
            formLayout.addComponent(phoneField);
            // Мыло
            emailField = new Label();
            emailField.setCaption("E-Mail");
            formLayout.addComponent(emailField);

            // ИНН
            positionField = new Label();
            positionField.setCaption("Должность");
            formLayout.addComponent(positionField);

            HorizontalLayout toolbar = new HorizontalLayout();
            viewBtn = new Button("Просмотр", event -> {
                final Employee bean = (Employee) selectField.getConvertedValue();

                final EmployeeEditForm editWin = new EmployeeEditForm(bean, company);
                editWin.setModified(true);

                editWin.addCloseFormListener(event1 -> {
                    if (editWin.isSaved()) {
                        refreshFields(bean);
                    }
                });
                FormUtils.showModalWin(editWin);
            });
            viewBtn.setDescription("Открыть форму ввода/редактирования сотрудника");
            viewBtn.setIcon(Fontello.EDIT_3);
            viewBtn.addStyleName(ExtaTheme.BUTTON_BORDERLESS_COLORED);
            viewBtn.addStyleName(ExtaTheme.BUTTON_SMALL);
            toolbar.addComponent(viewBtn);

            final Button searchBtn = new Button("Поиск", event -> {
                final EmployeeSelectWindow selectWindow = new EmployeeSelectWindow("Выберите сотрудника или введите нового", company);
                selectWindow.addCloseListener(e -> {
                    if (selectWindow.isSelectPressed()) {
                        final Employee selected = selectWindow.getSelected();
                        selectField.setConvertedValue(selected);
                    }
                });
                selectWindow.showModal();

            });
            searchBtn.setDescription("Открыть форму для поиска и выбора сотрудника");
            searchBtn.setIcon(Fontello.SEARCH_OUTLINE);
            searchBtn.addStyleName(ExtaTheme.BUTTON_BORDERLESS_COLORED);
            searchBtn.addStyleName(ExtaTheme.BUTTON_SMALL);
            toolbar.addComponent(searchBtn);

            formLayout.addComponent(toolbar);

            refreshFields((Employee) getPropertyDataSource().getValue());
            return formLayout;
        }

        public void refreshFields(Employee employee) {
            setValue(employee);

            final BeanItem<Employee> beanItem = new BeanItem<>(Optional.ofNullable(employee).orElse(new Employee()));
            if (viewBtn != null) viewBtn.setEnabled(employee != null);
            // Телефон
            if (phoneField != null) phoneField.setPropertyDataSource(beanItem.getItemProperty("phone"));
            // Мыло
            if (emailField != null) emailField.setPropertyDataSource(beanItem.getItemProperty("email"));
            // ИНН
            if (positionField != null) positionField.setPropertyDataSource(beanItem.getItemProperty("jobPosition"));
        }
    }

    @Override
    public Class<? extends Employee> getType() {
        return Employee.class;
    }
}
