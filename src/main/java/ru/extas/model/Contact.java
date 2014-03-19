/**
 *
 */
package ru.extas.model;


import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

/**
 * Контактное лицо контрагента, клиент физик или сотрудник
 *
 * @author Valery Orlov
 * @version $Id: $Id
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "TYPE")
@Table(name = "CONTACT", indexes = {
		@Index(columnList = "NAME"),
		@Index(columnList = "TYPE, NAME")
})
public abstract class Contact extends AbstractExtaObject implements Cloneable {

	private static final long serialVersionUID = -2543373135823969745L;

	/** Constant <code>NAME_LENGTH=50</code> */
	public static final int NAME_LENGTH = 50;
	/** Constant <code>PHONE_LINGHT=20</code> */
	public static final int PHONE_LINGHT = 20;

	// Фактический адрес
	@Embedded
	private AddressInfo actualAddress;

	// Имя контакта
	@Column(length = NAME_LENGTH)
	@Max(NAME_LENGTH)
	@NotNull
	private String name;

	// Телефон
	@Column(name = "CELL_PHONE", length = PHONE_LINGHT)
	@Max(20)
	private String phone;

	// Эл. почта
	@Column(length = 50)
	@Max(50)
	private String email;

	// Сайт
	@Column(length = 50)
	@Max(50)
	private String www;

	// Вышестоящая организация
	@OneToOne
	private Company affiliation;

	/**
	 * <p>copyTo.</p>
	 *
	 * @param toObj a {@link ru.extas.model.Contact} object.
	 */
	protected void copyTo(Contact toObj) {
		if (actualAddress != null)
			toObj.actualAddress = actualAddress.clone();
		toObj.name = name;
		toObj.phone = phone;
		toObj.email = email;
		toObj.www = www;
		if (affiliation != null)
			toObj.affiliation = affiliation.clone();
	}

	/**
	 * <p>Getter for the field <code>affiliation</code>.</p>
	 *
	 * @return a {@link ru.extas.model.Company} object.
	 */
	public Company getAffiliation() {
		return affiliation;
	}

	/**
	 * <p>Setter for the field <code>affiliation</code>.</p>
	 *
	 * @param affiliation a {@link ru.extas.model.Company} object.
	 */
	public void setAffiliation(final Company affiliation) {
		this.affiliation = affiliation;
	}

	/**
	 * <p>Getter for the field <code>name</code>.</p>
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>Setter for the field <code>name</code>.</p>
	 *
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * <p>Getter for the field <code>phone</code>.</p>
	 *
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * <p>Setter for the field <code>phone</code>.</p>
	 *
	 * @param phone the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * <p>Getter for the field <code>email</code>.</p>
	 *
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * <p>Setter for the field <code>email</code>.</p>
	 *
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * <p>Setter for the field <code>actualAddress</code>.</p>
	 *
	 * @param actualAddress a {@link ru.extas.model.AddressInfo} object.
	 */
	public void setActualAddress(final AddressInfo actualAddress) {
		this.actualAddress = actualAddress;
	}

	/**
	 * <p>Getter for the field <code>actualAddress</code>.</p>
	 *
	 * @return a {@link ru.extas.model.AddressInfo} object.
	 */
	public AddressInfo getActualAddress() {
		return actualAddress;
	}

	/**
	 * <p>Getter for the field <code>www</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getWww() {
		return www;
	}

	/**
	 * <p>Setter for the field <code>www</code>.</p>
	 *
	 * @param www a {@link java.lang.String} object.
	 */
	public void setWww(final String www) {
		this.www = www;
	}
}
