/**
 *
 */
package ru.extas.web.users;

import com.vaadin.ui.Component;
import ru.extas.web.commons.AbstractTabView;
import ru.extas.web.commons.component.AbstractTabInfo;
import ru.extas.web.commons.component.TabInfo;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Реализует экран управления пользователями и правами доступа
 *
 * @author Valery Orlov
 * @version $Id: $Id
 */
public class UsersView extends AbstractTabView {

    private static final long serialVersionUID = -1272779672761523416L;

    /**
     * <p>Constructor for UsersView.</p>
     */
    public UsersView() {
        super("Пользователи");
    }

    /** {@inheritDoc} */
    @Override
    protected List<TabInfo> getTabComponentsInfo() {
        final ArrayList<TabInfo> ret = newArrayList();
        ret.add(new AbstractTabInfo("Пользователи") {
            private static final long serialVersionUID = 1L;

            @Override
            public Component createComponent() {
                return new UsersGrid();
            }
        });
        return ret;
    }

}
