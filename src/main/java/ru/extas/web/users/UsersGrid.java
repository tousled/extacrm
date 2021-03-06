/**
 *
 */
package ru.extas.web.users;

import com.vaadin.data.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.extas.model.security.UserProfile;
import ru.extas.web.commons.*;
import ru.extas.web.commons.container.ExtaDbContainer;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Таблица пользователей
 *
 * @author Valery Orlov
 * @version $Id: $Id
 * @since 0.3
 */
public class UsersGrid extends ExtaGrid<UserProfile> {

	private static final long serialVersionUID = -4385482673967616119L;
	private final static Logger logger = LoggerFactory.getLogger(UsersGrid.class);

	/**
	 * <p>Constructor for UsersGrid.</p>
	 */
	public UsersGrid() {
		super(UserProfile.class);

	}

    @Override
    public ExtaEditForm<UserProfile> createEditForm(final UserProfile userProfile, final boolean isInsert) {
        return new UserEditForm(userProfile);
    }

    /** {@inheritDoc} */
	@Override
	protected GridDataDecl createDataDecl() {
		return new UsersDataDecl();
	}

	/** {@inheritDoc} */
	@Override
	protected Container createContainer() {
		// Запрос данных
		final ExtaDbContainer<UserProfile> container = new ExtaDbContainer<>(UserProfile.class);
		container.addNestedContainerProperty("employee.name");
        container.sort(new Object[]{"employee.name"}, new boolean[]{true});
		return container;
	}

	/** {@inheritDoc} */
	@Override
	protected List<UIAction> createActions() {
		final List<UIAction> actions = newArrayList();

		actions.add(new NewObjectAction("Новый", "Ввод нового пользователя в систему", Fontello.USER_ADD));
		actions.add(new EditObjectAction("Изменить", "Редактирование данных пользователя", Fontello.USER_1));

    	return actions;
	}

}
