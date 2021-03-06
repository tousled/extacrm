/**
 *
 */
package ru.extas.web.contacts;

import ru.extas.web.commons.EmailLinkColumnGen;
import ru.extas.web.commons.GridDataDecl;
import ru.extas.web.commons.UrlLinkColumnGen;
import ru.extas.web.commons.converters.PhoneConverter;

/**
 * Опции отображения контактов в списке
 *
 * @author Valery Orlov
 * @version $Id: $Id
 * @since 0.3
 */
public class ContactDataDecl extends GridDataDecl {

	/**
	 * <p>Constructor for ContactDataDecl.</p>
	 */
	public ContactDataDecl() {
		super();
		addMapping("name", "Имя");
		addMapping("phone", "Телефон", PhoneConverter.class);
		addMapping("email", "E-Mail", new EmailLinkColumnGen());
		addMapping("www", "WWW", new UrlLinkColumnGen());
		super.addDefaultMappings();
	}

}
