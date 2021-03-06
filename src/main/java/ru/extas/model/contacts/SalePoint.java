package ru.extas.model.contacts;

import ru.extas.model.common.Address;
import ru.extas.model.common.ArchivedObject;
import ru.extas.model.common.Comment;
import ru.extas.model.security.CuratorsGroup;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Модель данных "Точка продаж"
 *
 * @author Valery Orlov
 *         Date: 10.02.14
 *         Time: 15:24
 * @version $Id: $Id
 * @since 0.3
 */
@Entity
@Table(name = "SALE_POINT", indexes = {@Index(columnList = "NAME")})
public class SalePoint extends Contact implements ArchivedObject {

    private static final int CODE_LENGTH = 50;
    // Компания
    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = {CascadeType.REFRESH, CascadeType.DETACH})
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = {CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "CURATORS_GROUP_ID")
    private CuratorsGroup curatorsGroup;

    // Юр. лица работающие на торговой точке
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.DETACH})
    @JoinTable(
            name = "SALEPOINT_LEGALENTITY",
            joinColumns = {@JoinColumn(name = "SALEPOINT_ID", referencedColumnName = "ID")},
            inverseJoinColumns = {@JoinColumn(name = "LEGALENTITY_ID", referencedColumnName = "ID")})
    @OrderBy("name ASC")
    private Set<LegalEntity> legalEntities = newHashSet();

    // Сотрудники
    @OneToMany(mappedBy = "workPlace", fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.DETACH})
    @OrderBy("name ASC")
    private Set<Employee> employees = newHashSet();

    // Идентификация:

    //  - Код Экстрим Ассистанс
    @Column(name = "EXTA_CODE", length = CODE_LENGTH)
    @Size(max = CODE_LENGTH)
    private String extaCode;

    //  - Код Альфа Банка
    @Column(name = "ALPHA_CODE", length = CODE_LENGTH)
    @Size(max = CODE_LENGTH)
    private String alphaCode;

    //  - Код HomeCredit Банка
    @Column(name = "HOME_CODE", length = CODE_LENGTH)
    @Size(max = CODE_LENGTH)
    private String homeCode;

    //  - Код Банка СЕТЕЛЕМ
    @Column(name = "SETELEM_CODE", length = CODE_LENGTH)
    @Size(max = CODE_LENGTH)
    private String setelemCode;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = Comment.OWNER_ID_COLUMN)
    @OrderBy("createdDate")
    private List<SalePointComment> comments = newArrayList();

    @Column(name = "IS_API_EXPOSE")
    private boolean apiExpose = true;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "ADDRESS_POS")
    private Address posAddress;


    public boolean isApiExpose() {
        return apiExpose;
    }

    public void setApiExpose(final boolean apiExpose) {
        this.apiExpose = apiExpose;
    }

    public List<SalePointComment> getComments() {
        return comments;
    }

    public void setComments(final List<SalePointComment> comments) {
        this.comments = comments;
    }

    public CuratorsGroup getCuratorsGroup() {
        return curatorsGroup;
    }

    public void setCuratorsGroup(final CuratorsGroup curatorsGroup) {
        this.curatorsGroup = curatorsGroup;
    }

    /**
     * <p>Getter for the field <code>legalEntities</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public Set<LegalEntity> getLegalEntities() {
        return legalEntities;
    }

    /**
     * <p>Setter for the field <code>legalEntities</code>.</p>
     *
     * @param legalEntities a {@link java.util.List} object.
     */
    public void setLegalEntities(final Set<LegalEntity> legalEntities) {
        this.legalEntities = legalEntities;
    }

    /**
     * <p>Getter for the field <code>employees</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public Set<Employee> getEmployees() {
        return employees;
    }

    /**
     * <p>Setter for the field <code>employees</code>.</p>
     *
     * @param employees a {@link java.util.List} object.
     */
    public void setEmployees(final Set<Employee> employees) {
        if(!Objects.equals(employees, this.employees)) {
            synchronized (this) {
                // Обрываем сущесвующую связь
                if (this.employees != null)
                    this.employees.forEach(e -> e.setWorkPlace(null));
                // Устанавливаем новую связь
                this.employees = employees;
                if (this.employees != null)
                    this.employees.forEach(e -> e.setWorkPlace(this));
            }
        }
    }

    /**
     * <p>Getter for the field <code>extaCode</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getExtaCode() {
        return extaCode;
    }

    /**
     * <p>Setter for the field <code>extaCode</code>.</p>
     *
     * @param extaCode a {@link java.lang.String} object.
     */
    public void setExtaCode(final String extaCode) {
        this.extaCode = extaCode;
    }

    /**
     * <p>Getter for the field <code>alphaCode</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAlphaCode() {
        return alphaCode;
    }

    /**
     * <p>Setter for the field <code>alphaCode</code>.</p>
     *
     * @param alphaCode a {@link java.lang.String} object.
     */
    public void setAlphaCode(final String alphaCode) {
        this.alphaCode = alphaCode;
    }

    /**
     * <p>Getter for the field <code>homeCode</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHomeCode() {
        return homeCode;
    }

    /**
     * <p>Setter for the field <code>homeCode</code>.</p>
     *
     * @param homeCode a {@link java.lang.String} object.
     */
    public void setHomeCode(final String homeCode) {
        this.homeCode = homeCode;
    }

    /**
     * <p>Getter for the field <code>setelemCode</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSetelemCode() {
        return setelemCode;
    }

    /**
     * <p>Setter for the field <code>setelemCode</code>.</p>
     *
     * @param setelemCode a {@link java.lang.String} object.
     */
    public void setSetelemCode(final String setelemCode) {
        this.setelemCode = setelemCode;
    }

    /**
     * <p>Getter for the field <code>company</code>.</p>
     *
     * @return a {@link ru.extas.model.contacts.Company} object.
     */
    public Company getCompany() {
        return company;
    }

    /**
     * <p>Setter for the field <code>company</code>.</p>
     *
     * @param company a {@link ru.extas.model.contacts.Company} object.
     */
    public void setCompany(final Company company) {
        this.company = company;
    }

    public Address getPosAddress() {
        return posAddress;
    }

    public void setPosAddress(final Address posAddress) {
        this.posAddress = posAddress;
    }
}
