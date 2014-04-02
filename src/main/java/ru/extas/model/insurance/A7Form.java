/**
 *
 */
package ru.extas.model.insurance;


import ru.extas.model.common.ChangeMarkedObject;
import ru.extas.model.contacts.Contact;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

/**
 * Данные о квитанции форма № А-7
 *
 * @author Valery Orlov
 * @version $Id: $Id
 * @since 0.3
 */
@Entity
@Table(name = "A7_FORM",
        indexes = {
                @Index(columnList = "OWNER_ID, STATUS, REG_NUM")
        })
public class A7Form extends ChangeMarkedObject {

    private static final long serialVersionUID = -4643812782207400426L;
    /** Constant <code>REG_NUM_LENGTH=20</code> */
    public static final int REG_NUM_LENGTH = 20;

    /**
     * Статусы формы А-7
     *
     * @author Valery Orlov
     */
    public enum Status {
        /**
         * Новый бланк
         */
        NEW,
        /**
         * Использованный бланк
         */
        SPENT,
        /**
         * Потерянный бланк
         */
        LOST,
        /**
         * Испорченный бланк
         */
        BROKEN
    }

    /**
     * Номер квитанции
     */
    @Column(name = "REG_NUM", length = REG_NUM_LENGTH, unique = true)
    @Max(REG_NUM_LENGTH)
    @NotNull
    private String regNum;

    /**
     * Статус квитанции
     */
    private Status status = Status.NEW;

    /**
     * Владелец квитанции
     */
    @OneToOne
    private Contact owner;

    /**
     * Создает новую запись о бланке
     *
     * @param regNum Номер бланка
     * @param owner  Владелец бланка
     */
    public A7Form(final String regNum, final Contact owner) {
        super();
        this.regNum = regNum;
        this.owner = owner;
    }

    /**
     * <p>Constructor for A7Form.</p>
     */
    public A7Form() {
    }

    /**
     * <p>Getter for the field <code>regNum</code>.</p>
     *
     * @return the regNum
     */
    public String getRegNum() {
        return regNum;
    }

    /**
     * <p>Setter for the field <code>regNum</code>.</p>
     *
     * @param regNum the regNum to set
     */
    public void setRegNum(final String regNum) {
        this.regNum = regNum;
    }

    /**
     * <p>Getter for the field <code>status</code>.</p>
     *
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * <p>Setter for the field <code>status</code>.</p>
     *
     * @param status the status to set
     */
    public void setStatus(final Status status) {
        this.status = status;
    }

    /**
     * <p>Getter for the field <code>owner</code>.</p>
     *
     * @return the owner
     */
    public Contact getOwner() {
        return owner;
    }

    /**
     * <p>Setter for the field <code>owner</code>.</p>
     *
     * @param owner the owner to set
     */
    public void setOwner(final Contact owner) {
        this.owner = owner;
    }

}