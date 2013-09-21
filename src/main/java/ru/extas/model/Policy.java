/**
 *
 */
package ru.extas.model;

import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * Полис страхования в БСО
 *
 * @author Valery Orlov
 */
@Entity
@Table(name = "POLICY")
public class Policy extends AbstractExtaObject {

    private static final long serialVersionUID = 3160576591591414719L;

    // Номер полиса
    @Column(name = "REG_NUM")
    private String regNum;

    // Время бронирования полиса
    @Column(name = "BOOK_TIME")
    private DateTime bookTime;

    // Время реализации полиса
    @Column(name = "ISSUE_DATE")
    private DateTime issueDate;

    /**
     * @return the regNum
     */
    public String getRegNum() {
        return regNum;
    }

    /**
     * @param regNum the regNum to set
     */
    public void setRegNum(String regNum) {
        this.regNum = regNum;
    }

    /**
     * @return the bookTime
     */
    public DateTime getBookTime() {
        return bookTime;
    }

    /**
     * @param bookTime the bookTime to set
     */
    public void setBookTime(DateTime bookTime) {
        this.bookTime = bookTime;
    }

    /**
     * @return the issueDate
     */
    public DateTime getIssueDate() {
        return issueDate;
    }

    /**
     * @param issueDate the issueDate to set
     */
    public void setIssueDate(DateTime issueDate) {
        this.issueDate = issueDate;
    }
}
