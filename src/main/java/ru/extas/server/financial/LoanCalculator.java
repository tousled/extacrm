package ru.extas.server.financial;

import ru.extas.model.sale.ProdCredit;

import java.math.BigDecimal;

/**
 * Сервис рассчета параметров кредита
 *
 * @author Valery Orlov
 *         Date: 05.12.2014
 *         Time: 13:18
 */
public interface LoanCalculator {

    /**
     * Рассчитывает различные параметры кредита
     *
     * @param product     кредитный продукт
     * @param price       цена товара
     * @param downPayment первоначальный платеж (руб.)
     * @param period      срок кредита (месяцы)
     * @return Объект со всеми рассчитанными параметрами кредита
     */
    LoanInfo calc(ProdCredit product, BigDecimal price, BigDecimal downPayment, int period);

    /**
     * Рассчитывает сумму кыредита исходя из продукта, цены товара, первоначального взноса и срока
     *
     * @param product        кредитный продукт
     * @param price          цена товара
     * @param downPaymentSum сумма первоначального платежа
     * @return
     */
    BigDecimal calcCreditSum(ProdCredit product, BigDecimal price, BigDecimal downPaymentSum);

    /**
     * Подбирает процентную ставку продукта исходя из первоначального платежа и срока
     *
     * @param product     продукт
     * @param downPayment первоначальный платеж (%)
     * @param period      срок кредита в месяцах
     * @return процентная ставка или null, если нет подходящей ставки
     */
    BigDecimal calcInterest(ProdCredit product, BigDecimal downPayment, int period);

    /**
     * Рассчитывает сумму первоначального взноса исходя из продукта, цены товара и суммы кредита
     *
     * @param credit    продукт
     * @param price     цена товара
     * @param creditSum сумма кредита
     * @return сумма первоначальнго взноса
     */
    BigDecimal calcDownPayment(ProdCredit credit, BigDecimal price, BigDecimal creditSum);
}
