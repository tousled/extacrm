package ru.extas.model.insurance;

import org.joda.time.LocalDate;
import ru.extas.model.common.Comment;
import ru.extas.model.common.OwnedFileContainer;
import ru.extas.model.contacts.Client;
import ru.extas.model.contacts.Contact;
import ru.extas.model.contacts.SalePoint;
import ru.extas.model.motor.MotorBrand;
import ru.extas.model.motor.MotorModel;
import ru.extas.model.motor.MotorType;
import ru.extas.model.security.SecuredObject;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Полис страхования
 *
 * @author Valery Orlov
 * @version $Id: $Id
 * @since 0.3
 */
@Entity
@Table(name = "INSURANCE",
        indexes = {
                @Index(columnList = "REG_NUM"),
                @Index(columnList = "A7_NUM"),
                @Index(columnList = "\"DATE\"")
        })
public class Insurance extends SecuredObject {

    private static final long serialVersionUID = -1289533183659860816L;

    // признак пролонгации договора
    @Column(name = "IS_USED_MOTOR")
    private boolean usedMotor;

    // Номер полиса
    @Column(name = "REG_NUM", length = Policy.REG_NUM_LENGTH, unique = true)
    @Size(max = Policy.REG_NUM_LENGTH)
    private String regNum;

    // Номер квитанции А-7
    @Column(name = "A7_NUM", length = A7Form.REG_NUM_LENGTH, unique = true)
    @Size(max = A7Form.REG_NUM_LENGTH)
    private String a7Num;

    // Дата заключения полиса
    @Column(name = "\"DATE\"")
    private LocalDate date;

    // Клиент может быть физ. или юр. лицом
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "CLIENT_ID", referencedColumnName = "ID")
    private Client client;

    @Column(name = "BENEFICIARY", length = Contact.NAME_LENGTH)
    @Size(max = Contact.NAME_LENGTH)
    private String beneficiary;

    // Предмет страхования - тип
    @Column(name = "MOTOR_TYPE", length = MotorType.NAME_LENGTH)
    @Size(max = MotorType.NAME_LENGTH)
    private String motorType;

    // Предмет страхования - марка
    @Column(name = "MOTOR_BRAND", length = MotorBrand.NAME_LENGTH)
    @Size(max = MotorBrand.NAME_LENGTH)
    private String motorBrand;

    // Предмет страхования - модель
    @Column(name = "MOTOR_MODEL", length = MotorModel.NAME_LENGTH)
    @Size(max = MotorModel.NAME_LENGTH)
    private String motorModel;

    // Серийный номер
    @Column(name = "MOTOR_VIN", length = 50)
    @Size(max = 50)
    private String motorVin;

    // Номер договора купли-продажи
    @Column(name = "SALE_NUM", length = 50)
    @Size(max = 50)
    private String saleNum;

    // Дата договора купли-продажи
    @Column(name = "SALE_DATE")
    private LocalDate saleDate;

    // Страховая сумма, руб.
    @Column(name = "RISK_SUM", precision = 32, scale = 4)
    private BigDecimal riskSum;

    // Период покрытия
    @Column(name = "COVER_TIME")
    private PeriodOfCover coverTime;

    // Страховая премия, руб.
    @Column(precision = 32, scale = 4)
    private BigDecimal premium;

    // Дата оплаты страховой премии
    @Column(name = "PAYMENT_DATE")
    private LocalDate paymentDate;

    // Дата начала срока действия договора
    @Column(name = "START_DATE")
    private LocalDate startDate;

    // Дата окончания срока действия договора
    @Column(name = "END_DATE")
    private LocalDate endDate;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.DETACH})
    private SalePoint dealer;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = OwnedFileContainer.OWNER_ID_COLUMN)
    private List<InsuranceFileContainer> files = newArrayList();

    @Column(name = "IS_DOC_COMPLETE")
    private boolean docComplete;

    // Комментарии к договору страхования
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = Comment.OWNER_ID_COLUMN)
    @OrderBy("createdDate")
    private List<InsuranceComment> comments = newArrayList();

    public boolean isCredit() {
        return !isNullOrEmpty(getBeneficiary()) && !getBeneficiary().equals(getClient().getName());
    }

    public enum PeriodOfCover {
        YEAR,
        HALF_A_YEAR
    }

    /**
     * <p>Constructor for Insurance.</p>
     */
    public Insurance() {
    }

    /**
     * <p>Getter for the field <code>coverTime</code>.</p>
     *
     * @return a {@link ru.extas.model.insurance.Insurance.PeriodOfCover} object.
     */
    public PeriodOfCover getCoverTime() {
        return coverTime;
    }

    /**
     * <p>Setter for the field <code>coverTime</code>.</p>
     *
     * @param coverTime a {@link ru.extas.model.insurance.Insurance.PeriodOfCover} object.
     */
    public void setCoverTime(final PeriodOfCover coverTime) {
        this.coverTime = coverTime;
    }

    /**
     * <p>Constructor for Insurance.</p>
     *
     * @param motorBrand  a {@link java.lang.String} object.
     * @param riskSum     a {@link java.math.BigDecimal} object.
     * @param coverPeriod a {@link ru.extas.model.insurance.Insurance.PeriodOfCover} object.
     * @param usedMotor   a boolean.
     */
    public Insurance(final String motorBrand, final BigDecimal riskSum, final PeriodOfCover coverPeriod, final boolean usedMotor) {
        this.motorBrand = motorBrand;
        this.riskSum = riskSum;
        this.coverTime = coverPeriod;
        this.usedMotor = usedMotor;
    }

    /**
     * <p>Getter for the field <code>regNum</code>.</p>
     *
     * @return the regNum
     */
    public final String getRegNum() {
        return regNum;
    }

    /**
     * <p>Setter for the field <code>regNum</code>.</p>
     *
     * @param regNum the regNum to set
     */
    public final void setRegNum(final String regNum) {
        this.regNum = regNum;
    }

    /**
     * <p>Getter for the field <code>motorType</code>.</p>
     *
     * @return the motorType
     */
    public final String getMotorType() {
        return motorType;
    }

    /**
     * <p>Setter for the field <code>motorType</code>.</p>
     *
     * @param motorType the motorType to set
     */
    public final void setMotorType(final String motorType) {
        this.motorType = motorType;
    }

    /**
     * <p>Getter for the field <code>motorBrand</code>.</p>
     *
     * @return the motorBrand
     */
    public final String getMotorBrand() {
        return motorBrand;
    }

    /**
     * <p>Setter for the field <code>motorBrand</code>.</p>
     *
     * @param motorBrand the motorBrand to set
     */
    public final void setMotorBrand(final String motorBrand) {
        this.motorBrand = motorBrand;
    }

    /**
     * <p>Getter for the field <code>motorModel</code>.</p>
     *
     * @return the motorModel
     */
    public final String getMotorModel() {
        return motorModel;
    }

    /**
     * <p>Setter for the field <code>motorModel</code>.</p>
     *
     * @param motorModel the motorModel to set
     */
    public final void setMotorModel(final String motorModel) {
        this.motorModel = motorModel;
    }

    /**
     * <p>Getter for the field <code>riskSum</code>.</p>
     *
     * @return the riskSum
     */
    public final BigDecimal getRiskSum() {
        return riskSum;
    }

    /**
     * <p>Setter for the field <code>riskSum</code>.</p>
     *
     * @param riskSum the riskSum to set
     */
    public final void setRiskSum(final BigDecimal riskSum) {
        this.riskSum = riskSum;
    }

    /**
     * <p>Getter for the field <code>premium</code>.</p>
     *
     * @return the premium
     */
    public final BigDecimal getPremium() {
        return premium;
    }

    /**
     * <p>Setter for the field <code>premium</code>.</p>
     *
     * @param premium the premium to set
     */
    public final void setPremium(final BigDecimal premium) {
        this.premium = premium;
    }

    /**
     * <p>Getter for the field <code>date</code>.</p>
     *
     * @return the date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * <p>Setter for the field <code>date</code>.</p>
     *
     * @param date the date to set
     */
    public void setDate(final LocalDate date) {
        this.date = date;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(final Client client) {
        this.client = client;
    }

    /**
     * <p>Getter for the field <code>paymentDate</code>.</p>
     *
     * @return the paymentDate
     */
    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    /**
     * <p>Setter for the field <code>paymentDate</code>.</p>
     *
     * @param paymentDate the paymentDate to set
     */
    public void setPaymentDate(final LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    /**
     * <p>Getter for the field <code>startDate</code>.</p>
     *
     * @return the startDate
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * <p>Setter for the field <code>startDate</code>.</p>
     *
     * @param startDate the startDate to set
     */
    public void setStartDate(final LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * <p>Getter for the field <code>endDate</code>.</p>
     *
     * @return the endDate
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * <p>Setter for the field <code>endDate</code>.</p>
     *
     * @param endDate the endDate to set
     */
    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * <p>Getter for the field <code>a7Num</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getA7Num() {
        return a7Num;
    }

    /**
     * <p>Setter for the field <code>a7Num</code>.</p>
     *
     * @param a7Num a {@link java.lang.String} object.
     */
    public void setA7Num(final String a7Num) {
        this.a7Num = a7Num;
    }

    /**
     * <p>Getter for the field <code>dealer</code>.</p>
     *
     * @return a {@link ru.extas.model.contacts.SalePoint} object.
     */
    public SalePoint getDealer() {
        return dealer;
    }

    /**
     * <p>Setter for the field <code>dealer</code>.</p>
     *
     * @param dealer a {@link ru.extas.model.contacts.SalePoint} object.
     */
    public void setDealer(final SalePoint dealer) {
        this.dealer = dealer;
    }

    /**
     * <p>Getter for the field <code>saleNum</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSaleNum() {
        return saleNum;
    }

    /**
     * <p>Setter for the field <code>saleNum</code>.</p>
     *
     * @param saleNum a {@link java.lang.String} object.
     */
    public void setSaleNum(final String saleNum) {
        this.saleNum = saleNum;
    }

    /**
     * <p>Getter for the field <code>saleDate</code>.</p>
     *
     * @return a {@link org.joda.time.LocalDate} object.
     */
    public LocalDate getSaleDate() {
        return saleDate;
    }

    /**
     * <p>Setter for the field <code>saleDate</code>.</p>
     *
     * @param saleDate a {@link org.joda.time.LocalDate} object.
     */
    public void setSaleDate(final LocalDate saleDate) {
        this.saleDate = saleDate;
    }

    /**
     * <p>isUsedMotor.</p>
     *
     * @return a boolean.
     */
    public boolean isUsedMotor() {
        return usedMotor;
    }

    /**
     * <p>Setter for the field <code>usedMotor</code>.</p>
     *
     * @param used a boolean.
     */
    public void setUsedMotor(final boolean used) {
        this.usedMotor = used;
    }

    /**
     * <p>Getter for the field <code>motorVin</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMotorVin() {
        return motorVin;
    }

    /**
     * <p>Setter for the field <code>motorVin</code>.</p>
     *
     * @param motorVin a {@link java.lang.String} object.
     */
    public void setMotorVin(final String motorVin) {
        this.motorVin = motorVin;
    }

    /**
     * <p>Getter for the field <code>beneficiary</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBeneficiary() {
        return beneficiary;
    }

    /**
     * <p>Setter for the field <code>beneficiary</code>.</p>
     *
     * @param beneficiary a {@link java.lang.String} object.
     */
    public void setBeneficiary(final String beneficiary) {
        this.beneficiary = beneficiary;
    }

    /**
     * <p>Getter for the field <code>files</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<InsuranceFileContainer> getFiles() {
        return files;
    }

    /**
     * <p>Setter for the field <code>files</code>.</p>
     *
     * @param files a {@link java.util.List} object.
     */
    public void setFiles(final List<InsuranceFileContainer> files) {
        this.files = files;
    }

    /**
     * <p>isDocComplete.</p>
     *
     * @return a boolean.
     */
    public boolean isDocComplete() {
        return docComplete;
    }

    /**
     * <p>Setter for the field <code>docComplete</code>.</p>
     *
     * @param docComplete a boolean.
     */
    public void setDocComplete(final boolean docComplete) {
        this.docComplete = docComplete;
    }

    public List<InsuranceComment> getComments() {
        return comments;
    }

    public void setComments(final List<InsuranceComment> comments) {
        this.comments = comments;
    }
}
