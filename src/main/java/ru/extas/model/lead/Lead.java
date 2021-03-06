package ru.extas.model.lead;

import ru.extas.model.common.Address;
import ru.extas.model.common.Comment;
import ru.extas.model.common.ModelUtils;
import ru.extas.model.common.OwnedFileContainer;
import ru.extas.model.contacts.Client;
import ru.extas.model.contacts.Contact;
import ru.extas.model.contacts.Employee;
import ru.extas.model.contacts.SalePoint;
import ru.extas.model.motor.MotorInstance;
import ru.extas.model.product.ProductInstance;
import ru.extas.model.security.SecuredObject;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Модель данных для Лида
 *
 * @author Valery Orlov
 *         Date: 03.10.13
 *         Time: 10:09
 * @version $Id: $Id
 * @since 0.3
 */
@Entity
@Table(name = "LEAD")
public class Lead extends SecuredObject {

    /**
     * Статусы лида
     */
    public enum Status {
        NEW,
        QUALIFIED,
        CLOSED
    }

    /**
     * Результат завершения лида
     */
    public enum Result {
        /**
         * Успешное выполнение (кредит получен).
         */
        SUCCESSFUL,
        /**
         * Отказ контрагента (отказ банка).
         */
        VENDOR_REJECTED,
        /**
         * Отказ клиента.
         */
        CLIENT_REJECTED,
        /**
         * Отмененная ошибочно введенная дублирующая заявка
         */
        DOUBLE_REJECTED
    }

    @Column(unique = true,
            insertable = false,
            updatable = false,
            nullable = false,
            columnDefinition = "BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE")
    private Long num;

    // Регион покупки техники
    @Column(name = "REGION", length = Address.REGION_WITH_TYPE_LEN)
    @Size(max = Address.REGION_WITH_TYPE_LEN)
    private String region;

    @OneToMany(mappedBy = "lead", targetEntity = LeadMotor.class, orphanRemoval = true, fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private List<MotorInstance> motorInstances = newArrayList();

    // Мотосалон
    @Column(name = "POINT_OF_SALE", length = Contact.NAME_LENGTH * 4)
    @Size(max = Contact.NAME_LENGTH * 4)
    private String pointOfSale;

    @Column(name = "COMMENT")
    @Size(max = 255)
    private String comment;

    //    Персональные данные
    //-----------------------------------

    // Имя контакта
    @Column(name = "CONTACT_NAME", length = Contact.NAME_LENGTH)
    @Size(max = Contact.NAME_LENGTH)
    private String contactName;
    // Телефон
    @Column(name = "CONTACT_PHONE", length = Contact.PHONE_LINGHT)
    @Size(max = Contact.PHONE_LINGHT)
    private String contactPhone;
    // Эл. почта
    @Column(name = "CONTACT_EMAIL", length = Contact.EMAIL_LENGTH)
    @Size(max = Contact.EMAIL_LENGTH)
    private String contactEmail;
    // Регион проживания.
    @Column(name = "CONTACT_REGION", length = Address.REGION_WITH_TYPE_LEN)
    @Size(max = Address.REGION_WITH_TYPE_LEN)
    private String contactRegion;
    // источник
    @Column(name = "SOURCE", length = 50)
    @Size(max = 50)
    private String source;


    // Квалифицированные данные
    // -----------------------------------

    // Клиент
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "CLIENT", referencedColumnName = "ID")
    private Client client;

    // Продавец (дилер, страх. компания)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.DETACH})
    private SalePoint vendor;

    // Ответственный
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.DETACH})
    private Employee responsible;

    // Помощник ответственного с нашей стороны
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "RESPONSIBLE_ASSIST_ID")
    private Employee responsibleAssist;

    // Ответственный со стороны дилера
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "DEALER_MANAGER_ID")
    private Employee dealerManager;

    @Enumerated(EnumType.STRING)
    @Column(length = ModelUtils.ENUM_STRING_LENGTH)
    private Status status = Status.NEW;

    @Column(name = "PROCESS_ID", length = ModelUtils.ACTIVITI_ID_LENGTH)
    private String processId;

    @Enumerated(EnumType.STRING)
    @Column(length = ModelUtils.ENUM_STRING_LENGTH)
    private Result result;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = OwnedFileContainer.OWNER_ID_COLUMN)
    private List<LeadFileContainer> files = newArrayList();

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = Comment.OWNER_ID_COLUMN)
    @OrderBy("createdDate")
    private List<LeadComment> comments = newArrayList();

    @OneToMany(mappedBy = "lead", targetEntity = ProductInstance.class, fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<ProductInstance> productInstances = newArrayList();

    public List<MotorInstance> getMotorInstances() {
        return motorInstances;
    }

    public void setMotorInstances(final List<MotorInstance> motorInstances) {
        this.motorInstances = motorInstances;
    }

    public List<LeadFileContainer> getFiles() {
        return files;
    }

    public void setFiles(final List<LeadFileContainer> files) {
        this.files = files;
    }

    public Employee getResponsible() {
        return responsible;
    }

    public void setResponsible(final Employee responsible) {
        this.responsible = responsible;
    }

    public Long getNum() {
        return num;
    }

    public void setNum(final Long num) {
        this.num = num;
    }

    public Employee getResponsibleAssist() {
        return responsibleAssist;
    }

    public void setResponsibleAssist(final Employee responsibleAssist) {
        this.responsibleAssist = responsibleAssist;
    }

    public Employee getDealerManager() {
        return dealerManager;
    }

    public void setDealerManager(final Employee dealerManager) {
        this.dealerManager = dealerManager;
    }

    /**
     * <p>Getter for the field <code>result</code>.</p>
     *
     * @return a {@link ru.extas.model.lead.Lead.Result} object.
     */
    public Result getResult() {
        return result;
    }

    /**
     * <p>Setter for the field <code>result</code>.</p>
     *
     * @param result a {@link ru.extas.model.lead.Lead.Result} object.
     */
    public void setResult(final Result result) {
        this.result = result;
    }

    /**
     * <p>Getter for the field <code>processId</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getProcessId() {
        return processId;
    }

    /**
     * <p>Setter for the field <code>processId</code>.</p>
     *
     * @param processId a {@link java.lang.String} object.
     */
    public void setProcessId(final String processId) {
        this.processId = processId;
    }

    /**
     * <p>Getter for the field <code>comment</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getComment() {
        return comment;
    }

    /**
     * <p>Setter for the field <code>comment</code>.</p>
     *
     * @param comment a {@link java.lang.String} object.
     */
    public void setComment(final String comment) {
        this.comment = comment;
    }

    /**
     * <p>Getter for the field <code>region</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRegion() {
        return region;
    }

    /**
     * <p>Setter for the field <code>region</code>.</p>
     *
     * @param region a {@link java.lang.String} object.
     */
    public void setRegion(final String region) {
        this.region = region;
    }

    /**
     * <p>Getter for the field <code>motorType</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */

    /**
     * <p>Getter for the field <code>pointOfSale</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPointOfSale() {
        return pointOfSale;
    }

    /**
     * <p>Setter for the field <code>pointOfSale</code>.</p>
     *
     * @param pointOfSale a {@link java.lang.String} object.
     */
    public void setPointOfSale(final String pointOfSale) {
        this.pointOfSale = pointOfSale;
    }

    /**
     * <p>Getter for the field <code>contactName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getContactName() {
        return contactName;
    }

    /**
     * <p>Setter for the field <code>contactName</code>.</p>
     *
     * @param contactName a {@link java.lang.String} object.
     */
    public void setContactName(final String contactName) {
        this.contactName = contactName;
    }

    /**
     * <p>Getter for the field <code>contactPhone</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getContactPhone() {
        return contactPhone;
    }

    /**
     * <p>Setter for the field <code>contactPhone</code>.</p>
     *
     * @param contactPhone a {@link java.lang.String} object.
     */
    public void setContactPhone(final String contactPhone) {
        this.contactPhone = contactPhone;
    }

    /**
     * <p>Getter for the field <code>contactEmail</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getContactEmail() {
        return contactEmail;
    }

    /**
     * <p>Setter for the field <code>contactEmail</code>.</p>
     *
     * @param contactEmail a {@link java.lang.String} object.
     */
    public void setContactEmail(final String contactEmail) {
        this.contactEmail = contactEmail;
    }

    /**
     * <p>Getter for the field <code>client</code>.</p>
     *
     * @return a {@link ru.extas.model.contacts.Person} object.
     */
    public Client getClient() {
        return client;
    }

    /**
     * <p>Setter for the field <code>client</code>.</p>
     *
     * @param client a {@link ru.extas.model.contacts.Person} object.
     */
    public void setClient(final Client client) {
        this.client = client;
    }

    /**
     * <p>Getter for the field <code>vendor</code>.</p>
     *
     * @return a {@link ru.extas.model.contacts.SalePoint} object.
     */
    public SalePoint getVendor() {
        return vendor;
    }

    /**
     * <p>Setter for the field <code>vendor</code>.</p>
     *
     * @param vendor a {@link ru.extas.model.contacts.SalePoint} object.
     */
    public void setVendor(final SalePoint vendor) {
        this.vendor = vendor;
    }

    /**
     * <p>Getter for the field <code>status</code>.</p>
     *
     * @return a {@link ru.extas.model.lead.Lead.Status} object.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * <p>Setter for the field <code>status</code>.</p>
     *
     * @param status a {@link ru.extas.model.lead.Lead.Status} object.
     */
    public void setStatus(final Status status) {
        this.status = status;
    }

    /**
     * <p>Getter for the field <code>contactRegion</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getContactRegion() {
        return contactRegion;
    }

    /**
     * <p>Setter for the field <code>contactRegion</code>.</p>
     *
     * @param contactRegion a {@link java.lang.String} object.
     */
    public void setContactRegion(final String contactRegion) {
        this.contactRegion = contactRegion;
    }

    public String getSource() {
        return source;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public List<LeadComment> getComments() {
        return comments;
    }

    public void setComments(final List<LeadComment> comments) {
        this.comments = comments;
    }

    public List<ProductInstance> getProductInstances() {
        return productInstances;
    }

    public void setProductInstances(final List<ProductInstance> productInSales) {
        this.productInstances = productInSales;
    }
}
