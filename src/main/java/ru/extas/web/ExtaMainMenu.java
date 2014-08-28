/**
 *
 */
package ru.extas.web;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import ru.extas.model.security.ExtaDomain;
import ru.extas.server.security.UserManagementService;
import ru.extas.web.commons.ExtaUri;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static ru.extas.server.ServiceLocator.lookup;

/**
 * Класс создает и управляей основным меню разделов
 *
 * @author Valery Orlov
 * @version $Id: $Id
 * @since 0.3
 */
public class ExtaMainMenu extends CssLayout implements Page.UriFragmentChangedListener {

    private static final long serialVersionUID = 4672093745206168652L;
    private final Navigator navigator;
    private final Map<String, Button> fragmentToButton;

    /**
     * <p>Constructor for ExtaMainMenu.</p>
     *
     * @param ui a {@link com.vaadin.ui.UI} object.
     * @param content a {@link com.vaadin.ui.ComponentContainer} object.
     */
    public ExtaMainMenu(UI ui, ComponentContainer content) {

        // URI навигатор
        navigator = new Navigator(ui, content);
        navigator.setErrorView(ErrorView.class);

        fragmentToButton = new HashMap<>();

	    ui.getPage().addUriFragmentChangedListener(this);
    }

	/**
	 * <p>addChapter.</p>
	 *  @param name a {@link String} object.
	 * @param desc a {@link String} object.
     * @param btnIcon a {@link String} object.
     * @param viewCls a {@link Class} object.
     * @param domain a {@link ru.extas.model.security.ExtaDomain} object.
     */
	public void addChapter(String name, // Имя раздела
	                       String desc, // Описание раздела
                           Resource btnIcon, // Стиль кнопки раздела
	                       Class<? extends View> viewCls, // Класс раздела
	                       ExtaDomain domain // Раздел
	) {
		addChapter(name, desc, btnIcon, viewCls, EnumSet.of(domain));
	}
    /**
     * Создает раздел основного меню
     *  @param name     Имя раздела
     * @param desc     Описание раздела
     * @param btnIcon Стиль кнопки раздела
     * @param viewCls  Класс раздела
     * @param domains  Раздел или подразделы системы
     */
    public void addChapter(String name, // Имя раздела
                           String desc, // Описание раздела
                           Resource btnIcon, // Стиль кнопки раздела
                           Class<? extends View> viewCls, // Класс раздела
                           Set<ExtaDomain> domains // Раздел или подразделы
    ) {
	    checkNotNull(domains);
	    checkState(!domains.isEmpty());

	    // Проверяем права доступа
	    UserManagementService userService = lookup(UserManagementService.class);
	    if (userService.isPermittedOneOf(domains)) {
		    // Фрагмент адреса
		    final String domainUrl = Iterables.getFirst(domains, null).getName();
		    String fragment = Iterables.getFirst(Splitter.on('/').split(domainUrl), domainUrl);

		    final String normFragment = fragment;

		    // Регистрируем в навигаторе
		    navigator.addView(fragment, viewCls);

		    // Кнопка раздела
		    Button b = new Button(name);
		    b.setIcon(btnIcon);
		    b.setDescription(desc);
            b.setPrimaryStyleName("valo-menu-item");
		    b.addClickListener(new ClickListener() {
			    private static final long serialVersionUID = 1L;

			    @Override
			    public void buttonClick(ClickEvent event) {
				    clearMenuSelection();
				    event.getButton().addStyleName("selected");
				    if (!navigator.getState().equals(normFragment))
					    navigator.navigateTo(normFragment);
			    }
		    });

		    // Добавляем кнопку
		    addComponent(b);
		    fragmentToButton.put(normFragment, b);
	    }
    }

    private void clearMenuSelection() {
        for (Component next : this) {
            if (next instanceof Button) {
                next.removeStyleName("selected");
            }
        }
    }

    /**
     * <p>processURI.</p>
     *
     * @param uriStr a {@link java.lang.String} object.
     */
    public void processURI(String uriStr) {
        ExtaUri uri = new ExtaUri(uriStr);
        String uriFragment;
        if (isNullOrEmpty(uri.getDomainPrefix())) {
            uriFragment = ExtaDomain.DASHBOARD.getName();
            navigator.navigateTo(uriFragment);
        }
        else uriFragment = uri.toString();

        Button selButton = fragmentToButton.get(uri.getDomainPrefix());
        if (selButton != null)
            selButton.addStyleName("selected");
    }

	/** {@inheritDoc} */
	@Override
	public void uriFragmentChanged(final Page.UriFragmentChangedEvent event) {
        processURI(event.getUriFragment());
    }

	private class DomainViewProvider implements ViewProvider {

		private final Set<ExtaDomain> domains;
		private final Class<? extends View> viewCls;

		public DomainViewProvider(final Set<ExtaDomain> domains, final Class<? extends View> viewCls) {
			this.domains = domains;
			this.viewCls = viewCls;
		}

		@Override
		public String getViewName(String navigationState) {
			if (null == navigationState) {
				return null;
			}
			if (navigationState.startsWith("!"))
				navigationState = navigationState.substring(1);
			for (ExtaDomain domain:domains) {
				final String viewName = domain.getName();
				if (navigationState.equals(viewName)
						|| navigationState.startsWith(viewName + "/")
						|| viewName.startsWith(navigationState + "/")) {
					return viewName;
				}
			}
			return null;
		}

		@Override
		public View getView(final String viewName) {
			if (isOurName(viewName)) {
				try {
					View view = viewCls.newInstance();
					return view;
				} catch (InstantiationException e) {
					throw Throwables.propagate(e);
				} catch (IllegalAccessException e) {
					throw Throwables.propagate(e);
				}
			}
			return null;
		}

		private boolean isOurName(final String viewName) {
			return Iterables.tryFind(domains, input -> input.getName().equals(viewName)).isPresent();
		}
	}
}
