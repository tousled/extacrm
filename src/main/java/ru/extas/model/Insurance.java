package ru.extas.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * Полис страхования
 * 
 * @author Valery Orlov
 * 
 */
@PersistenceCapable(detachable = "true")
public class Insurance extends AbstractExtaObject {

	// Номер полиса
	@Persistent
	private String regNum;

	// Номер счета
	@Persistent
	private String chekNum;

	// Дата заключения полиса
	@Persistent
	private Date date;

	// Клиент - ФИО
	@Persistent
	private String clientName;

	// Клиент - Дата рождения
	@Persistent
	private Date clientBirthday;

	// Клиент - Телефон
	@Persistent
	private String clientPhone;

	// Клиент - Пол
	@Persistent
	private Boolean clientMale;

	// Предмет страхования - тип
	@Persistent
	private String motorType;

	// Предмет страхования - марка
	@Persistent
	private String motorBrand;

	// Предмет страхования - модель
	@Persistent
	private String motorModel;

	// Страховая сумма, руб.
	@Persistent
	private BigDecimal riskSum;

	// Страховая премия, руб.
	@Persistent
	private BigDecimal premium;

	// Дата оплаты страховой премии
	@Persistent
	private Date paymentDate;

	// Дата начала срока действия договора
	@Persistent
	private Date startDate;

	// Дата окончания срока действия договора
	@Persistent
	private Date endDate;

	// Сотрудник
	@Persistent
	private String createdBy;

	// Салон
	@Persistent
	private String reseller;

	public Insurance() {
	}

	/**
	 * @param regNum
	 * @param chekNum
	 * @param date
	 * @param clientName
	 * @param clientBirthday
	 * @param clientPhone
	 * @param clientMale
	 * @param motorType
	 * @param motorBrand
	 * @param motorModel
	 * @param riskSum
	 * @param premium
	 * @param paymentDate
	 * @param startDate
	 * @param endDate
	 * @param createdBy
	 * @param resaler
	 */
	public Insurance(String regNum, String chekNum, Date date, String clientName, Date clientBirthday, String clientPhone, Boolean clientMale,
			String motorType, String motorBrand, String motorModel, BigDecimal riskSum, BigDecimal premium, Date paymentDate, Date startDate,
			Date endDate, String createdBy, String reseller) {
		super();
		this.regNum = regNum;
		this.chekNum = chekNum;
		this.date = date;
		this.clientName = clientName;
		this.clientBirthday = clientBirthday;
		this.clientPhone = clientPhone;
		this.clientMale = clientMale;
		this.motorType = motorType;
		this.motorBrand = motorBrand;
		this.motorModel = motorModel;
		this.riskSum = riskSum;
		this.premium = premium;
		this.paymentDate = paymentDate;
		this.startDate = startDate;
		this.endDate = endDate;
		this.createdBy = createdBy;
		this.reseller = reseller;
	}

	/**
	 * @return the regNum
	 */
	public final String getRegNum() {
		return regNum;
	}

	/**
	 * @param regNum
	 *            the regNum to set
	 */
	public final void setRegNum(String regNum) {
		this.regNum = regNum;
	}

	/**
	 * @return the date
	 */
	public final Date getDate() {
		return date;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public final void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @return the clientName
	 */
	public final String getClientName() {
		return clientName;
	}

	/**
	 * @param clientName
	 *            the clientName to set
	 */
	public final void setClientName(String clientName) {
		this.clientName = clientName;
	}

	/**
	 * @return the clientBirthday
	 */
	public final Date getClientBirthday() {
		return clientBirthday;
	}

	/**
	 * @param clientBirthday
	 *            the clientBirthday to set
	 */
	public final void setClientBirthday(Date clientBirthday) {
		this.clientBirthday = clientBirthday;
	}

	/**
	 * @return the clientPhone
	 */
	public final String getClientPhone() {
		return clientPhone;
	}

	/**
	 * @param clientPhone
	 *            the clientPhone to set
	 */
	public final void setClientPhone(String clientPhone) {
		this.clientPhone = clientPhone;
	}

	/**
	 * @return the clientMale
	 */
	public final Boolean getClientMale() {
		return clientMale;
	}

	/**
	 * @param clientMale
	 *            the clientMale to set
	 */
	public final void setClientMale(Boolean clientMale) {
		this.clientMale = clientMale;
	}

	/**
	 * @return the motorType
	 */
	public final String getMotorType() {
		return motorType;
	}

	/**
	 * @param motorType
	 *            the motorType to set
	 */
	public final void setMotorType(String motorType) {
		this.motorType = motorType;
	}

	/**
	 * @return the motorBrand
	 */
	public final String getMotorBrand() {
		return motorBrand;
	}

	/**
	 * @param motorBrand
	 *            the motorBrand to set
	 */
	public final void setMotorBrand(String motorBrand) {
		this.motorBrand = motorBrand;
	}

	/**
	 * @return the motorModel
	 */
	public final String getMotorModel() {
		return motorModel;
	}

	/**
	 * @param motorModel
	 *            the motorModel to set
	 */
	public final void setMotorModel(String motorModel) {
		this.motorModel = motorModel;
	}

	/**
	 * @return the riskSum
	 */
	public final BigDecimal getRiskSum() {
		return riskSum;
	}

	/**
	 * @param riskSum
	 *            the riskSum to set
	 */
	public final void setRiskSum(BigDecimal riskSum) {
		this.riskSum = riskSum;
	}

	/**
	 * @return the premium
	 */
	public final BigDecimal getPremium() {
		return premium;
	}

	/**
	 * @param premium
	 *            the premium to set
	 */
	public final void setPremium(BigDecimal premium) {
		this.premium = premium;
	}

	/**
	 * @return the paymentDate
	 */
	public final Date getPaymentDate() {
		return paymentDate;
	}

	/**
	 * @param paymentDate
	 *            the paymentDate to set
	 */
	public final void setPaymentDate(Date paymentDate) {
		this.paymentDate = paymentDate;
	}

	/**
	 * @return the startDate
	 */
	public final Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate
	 *            the startDate to set
	 */
	public final void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public final Date getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate
	 *            the endDate to set
	 */
	public final void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the chekNum
	 */
	public String getChekNum() {
		return chekNum;
	}

	/**
	 * @param chekNum
	 *            the chekNum to set
	 */
	public void setChekNum(String chekNum) {
		this.chekNum = chekNum;
	}

	/**
	 * @return the createdBy
	 */
	@Override
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @param createdBy
	 *            the createdBy to set
	 */
	@Override
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return the reseller
	 */
	public final String getReseller() {
		return reseller;
	}

	/**
	 * @param reseller
	 *            the reseller to set
	 */
	public final void setReseller(String reseller) {
		this.reseller = reseller;
	}

}
