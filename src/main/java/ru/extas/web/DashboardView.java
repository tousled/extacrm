/**
 * DISCLAIMER
 * 
 * The quality of the code is such that you should not copy any of it as best
 * practice how to build Vaadin applications.
 * 
 * @author jouni@vaadin.com
 * 
 */

package ru.extas.web;

import java.text.DecimalFormat;
import java.util.Collection;

import ru.extas.model.Insurance;
import ru.extas.server.InsuranceRepository;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.RowHeaderMode;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class DashboardView extends VerticalLayout implements View {

	Table table;

	public DashboardView() {
	}

	private CssLayout createPanel(Component content) {
		CssLayout panel = new CssLayout();
		panel.addStyleName("layout-panel");
		panel.setSizeFull();

		Button configure = new Button();
		configure.addStyleName("configure");
		configure.addStyleName("icon-cog");
		configure.addStyleName("icon-only");
		configure.addStyleName("borderless");
		configure.setDescription("Конфигурация");
		configure.addStyleName("small");
		configure.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				Notification.show("Не реализовано пока");
			}
		});
		panel.addComponent(configure);

		panel.addComponent(content);
		return panel;
	}

	@Override
	public void enter(ViewChangeEvent event) {
		setSizeFull();
		addStyleName("dashboard-view");

		HorizontalLayout top = new HorizontalLayout();
		top.setWidth("100%");
		top.setSpacing(true);
		top.addStyleName("toolbar");
		addComponent(top);
		final Label title = new Label("Страхование техники");
		title.setSizeUndefined();
		title.addStyleName("h1");
		top.addComponent(title);
		top.setComponentAlignment(title, Alignment.MIDDLE_LEFT);
		top.setExpandRatio(title, 1);

		Button notify = new Button();
		notify.setDescription("Создать тестовые данные");
		// notify.addStyleName("borderless");
		notify.addStyleName("notifications");
		notify.addStyleName("icon-only");
		notify.addStyleName("icon-bell");
		notify.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				new InsuranceRepository().fillRegistry();
			}
		});
		top.addComponent(notify);
		top.setComponentAlignment(notify, Alignment.MIDDLE_LEFT);

		HorizontalLayout row = new HorizontalLayout();
		row.setMargin(true);
		row.setSizeFull();
		row.setSpacing(true);
		addComponent(row);
		setExpandRatio(row, 2);

		Collection<Insurance> insurances = new InsuranceRepository().getAll();
		BeanContainer<Long, Insurance> beans = new BeanContainer<Long, Insurance>(
				Insurance.class);
		beans.setBeanIdProperty("id");
		beans.addAll(insurances);

		table = new Table("Страховки", beans);

		table.setSizeFull();
		table.setPageLength(0);
		// t.addStyleName("plain");
		// t.addStyleName("borderless");
		// t.setSortEnabled(false);
		// t.setColumnAlignment("Revenue", Align.RIGHT);
		table.setRowHeaderMode(RowHeaderMode.INDEX);
		table.setSelectable(true);
		table.setColumnCollapsingAllowed(true);

		table.setColumnHeader("regNum", "Номер полиса");
		table.setColumnHeader("chekNum", "Номер счета");
		table.setColumnHeader("date", "Дата заключения полиса");
		table.setColumnHeader("clientName", "Клиент - ФИО");
		table.setColumnHeader("clientBirthday", "Клиент - Дата рождения");
		table.setColumnHeader("clientPhone", "Клиент - Телефон");
		table.setColumnHeader("clientMale", "Клиент - Пол");
		table.setColumnHeader("motorType", "Тип техники");
		table.setColumnHeader("motorBrand", "Марка техники");
		table.setColumnHeader("motorModel", "Модель техники");
		table.setColumnHeader("riskSum", "Страховая сумма");
		table.setColumnHeader("premium", "Страховая премия");
		table.setColumnHeader("paymentDate", "Дата оплаты страховой премии");
		table.setColumnHeader("startDate",
				"Дата начала срока действия договора");
		table.setColumnHeader("endDate",
				"Дата окончания срока действия договора");
		table.setColumnHeader("createdBy", "Сотрудник");
		table.setColumnHeader("resaler", "Салон");

		table.setVisibleColumns(new Object[] { "regNum", "date", "clientName",
				"motorType", "motorBrand", "motorModel", "riskSum", "premium", "createdBy" });

		// Опции отрбражения датасета: 
		// 1. порядок
		// 2. заголовок
		// 3. доступность
		// 4. видимость
		row.addComponent(createPanel(table));

	}

}
