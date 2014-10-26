package ru.extas.web.contacts.employee;

import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import ru.extas.model.contacts.Company;
import ru.extas.model.contacts.Employee;
import ru.extas.model.contacts.LegalEntity;
import ru.extas.model.contacts.SalePoint;
import ru.extas.utils.SupplierSer;
import ru.extas.web.commons.ExtaEditForm;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Реализует ввод/редактирование списка сотрудников для компании и торговой точки
 *
 * @author Valery Orlov
 *         Date: 17.02.14
 *         Time: 13:04
 * @version $Id: $Id
 * @since 0.3
 */
public class EmployeeFieldMany extends CustomField<Set> {

    private SupplierSer<Company> companySupplier;
    private SupplierSer<SalePoint> salePointSupplier;
    private SupplierSer<LegalEntity> legalEntitySupplier;

    private EmployeesGrid grid;
    private BeanItemContainer<Employee> beanContainer;

    public EmployeeFieldMany() {
        setBuffered(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Component initContent() {
        grid = new EmployeesGrid() {
            @Override
            protected Container createContainer() {
                final Set<Employee> list = getValue() != null ? getValue() : newHashSet();
                beanContainer = new BeanItemContainer<>(Employee.class);
                beanContainer.addNestedContainerProperty("company.name");
                beanContainer.addAll(list);

                return container = beanContainer;
            }

            @Override
            public ExtaEditForm<Employee> createEditForm(Employee employee, boolean isInsert) {
                final EmployeeEditForm form = new EmployeeEditForm(employee) {
                    @Override
                    protected Employee saveObject(Employee obj) {
                        if (isInsert)
                            beanContainer.addBean(obj);
                        setValue(newHashSet(beanContainer.getItemIds()));
                        return obj;
                    }
                };
                form.setCompanySupplier(companySupplier);
                form.setSalePointSupplier(salePointSupplier);
                form.setLegalEntitySupplier(legalEntitySupplier);
                form.setReadOnly(isReadOnly());
                return form;
            }
        };

        grid.setCompanySupplier(companySupplier);
        grid.setReadOnly(isReadOnly());
        return grid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Set> getType() {
        return Set.class;
    }

    public SupplierSer<Company> getCompanySupplier() {
        return companySupplier;
    }

    public void setCompanySupplier(SupplierSer<Company> companySupplier) {
        this.companySupplier = companySupplier;
    }

    public SupplierSer<SalePoint> getSalePointSupplier() {
        return salePointSupplier;
    }

    public void setSalePointSupplier(SupplierSer<SalePoint> salePointSupplier) {
        this.salePointSupplier = salePointSupplier;
    }

    public SupplierSer<LegalEntity> getLegalEntitySupplier() {
        return legalEntitySupplier;
    }

    public void setLegalEntitySupplier(SupplierSer<LegalEntity> legalEntitySupplier) {
        this.legalEntitySupplier = legalEntitySupplier;
    }
}
