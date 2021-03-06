package ru.extas.model.contacts;

import ru.extas.model.common.IdentifiedObject;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Доход физ. лица
 *
 * @author Valery Orlov
 *         Date: 11.09.2014
 *         Time: 3:40
 */
@Entity
@Table(name = "PERSON_INCOME")
public class PersonIncome extends IdentifiedObject {

    // Тип дохода (Основная зарплата, Зарплата по совместительству, Прочие)
    @Column(length = 50)
    @Size(max = 50)
    private String type;
    // Клиент
    @Column(precision = 32, scale = 4)
    private BigDecimal income;
    // Супруг(а)
    @Column(name = "SPOUSE_INCOME", precision = 32, scale = 4)
    private BigDecimal spouseIncome;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.DETACH})
    private Person owner;

    public PersonIncome() {
    }

    public PersonIncome(final Person owner) {
        this.owner = owner;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public void setIncome(final BigDecimal income) {
        this.income = income;
    }

    public BigDecimal getSpouseIncome() {
        return spouseIncome;
    }

    public void setSpouseIncome(final BigDecimal spouseIncome) {
        this.spouseIncome = spouseIncome;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(final Person owner) {
        this.owner = owner;
    }
}
