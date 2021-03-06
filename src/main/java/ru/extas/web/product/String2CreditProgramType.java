package ru.extas.web.product;

import com.google.common.collect.HashBiMap;
import org.springframework.stereotype.Component;
import ru.extas.model.product.ProdCredit;
import ru.extas.web.commons.converters.String2EnumConverter;

/**
 * Строковое представление перечисления типов кредитных программ
 *
 * @author Valery Orlov
 *         Date: 07.02.14
 *         Time: 20:01
 * @version $Id: $Id
 * @since 0.3
 */
@Component
public class String2CreditProgramType extends String2EnumConverter {

	/**
	 * <p>Constructor for String2CreditProgramType.</p>
	 */
	public String2CreditProgramType() {
		super(ProdCredit.ProgramType.class);
	}

	/** {@inheritDoc} */
	@Override
	protected HashBiMap createEnum2StringMap() {
        final HashBiMap<ProdCredit.ProgramType, String> map = HashBiMap.create();
		map.put(ProdCredit.ProgramType.NO_COLLATERAL, "Незалоговая");
		map.put(ProdCredit.ProgramType.COLLATERAL_WITH_PTS, "Залоговая без передачи ПТС (ПСМ) в Банк");
		map.put(ProdCredit.ProgramType.COLLATERAL_WITHOUT_PTS, "Залоговая с передачей ПТС (ПСМ) в Банк");
		return map;
	}


}
