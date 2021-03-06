/**
 *
 */
package ru.extas.web.insurance;

import ru.extas.web.commons.GridDataDecl;

/**
 * Опции отображения квитанций А-7
 *
 * @author Valery Orlov
 */
class FormTransferDataDecl extends GridDataDecl {

    /**
     * <p>Constructor for FormTransferDataDecl.</p>
     */
    public FormTransferDataDecl() {
        addMapping("fromContact.name", "От кого");
        addMapping("toContact.name", "Кому");
        addMapping("transferDate", "Дата");
        super.addDefaultMappings();
    }

}
