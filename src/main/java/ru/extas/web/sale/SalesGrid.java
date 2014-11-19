package ru.extas.web.sale;

import com.google.common.base.Throwables;
import com.vaadin.addon.tableexport.CustomTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Container;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.UI;
import org.joda.time.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;
import ru.extas.model.sale.Sale;
import ru.extas.model.security.ExtaDomain;
import ru.extas.server.sale.SaleRepository;
import ru.extas.web.commons.*;
import ru.extas.web.commons.window.DownloadFileWindow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static ru.extas.server.ServiceLocator.lookup;

/**
 * <p>SalesGrid class.</p>
 *
 * @author Valery Orlov
 *         Date: 15.10.13
 *         Time: 12:24
 * @version $Id: $Id
 * @since 0.3
 */
public class SalesGrid extends ExtaGrid<Sale> {
    private static final long serialVersionUID = 4876073256421755574L;
    private final static Logger logger = LoggerFactory.getLogger(SalesGrid.class);
    private final ExtaDomain domain;

    /**
     * <p>Constructor for SalesGrid.</p>
     *
     * @param domain a {@link ru.extas.model.security.ExtaDomain} object.
     */
    public SalesGrid(final ExtaDomain domain) {
        super(Sale.class);
        this.domain = domain;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GridDataDecl createDataDecl() {
        return new SaleDataDecl();
    }

    @Override
    public ExtaEditForm<Sale> createEditForm(final Sale sale, final boolean isInsert) {
        final SaleEditForm saleEditForm = new SaleEditForm(sale);
        saleEditForm.setReadOnly(domain != ExtaDomain.SALES_OPENED);
        return saleEditForm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initTable(final Mode mode) {
        super.initTable(mode);
        if (domain == ExtaDomain.SALES_CANCELED)
            table.setColumnCollapsed("result", false);

        // Раскрашиваем "протухшие" продажи
        if (domain == ExtaDomain.SALES_OPENED)
            table.setCellStyleGenerator((source, itemId, propertyId) -> {
                final Sale sale = GridItem.extractBean(table.getItem(itemId));
                final DateTime curDate = DateTime.now(DateTimeZone.UTC);
                final DateTime modifiedDate = sale.getLastModifiedDate();
                if (modifiedDate.plus(Days.days(10)).isBeforeNow())
                    return "highlight-red"; // Красненькие
                else if (modifiedDate.plus(Days.days(5)).isBeforeNow())
                    return "highlight-yellow"; // Желтенькие
                return null;
            });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Container createContainer() {
        // Запрос данных
        final ExtaDataContainer<Sale> container = new SecuredDataContainer<>(Sale.class, domain);
        container.addNestedContainerProperty("client.name");
        container.addNestedContainerProperty("client.phone");
        container.addNestedContainerProperty("dealer.name");
        container.addNestedContainerProperty("responsible.name");
        container.addNestedContainerProperty("responsibleAssist.name");
        container.addNestedContainerProperty("dealerManager.name");
        container.addNestedContainerProperty("bankManager.name");
        container.addContainerFilter(new Compare.Equal("status",
                domain == ExtaDomain.SALES_CANCELED ? Sale.Status.CANCELED :
                        domain == ExtaDomain.SALES_OPENED ? Sale.Status.NEW : Sale.Status.FINISHED));
        if (domain != ExtaDomain.SALES_OPENED)
            container.sort(new Object[]{"createdDate"}, new boolean[]{false});
        else
            container.sort(new Object[]{"lastModifiedDate"}, new boolean[]{true});
        return container;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<UIAction> createActions() {
        final List<UIAction> actions = newArrayList();

        if (domain == ExtaDomain.SALES_OPENED)
            actions.add(new NewObjectAction("Новый", "Ввод новой продажи"));

        actions.add(new EditObjectAction(domain == ExtaDomain.SALES_OPENED ? "Изменить" : "Просмотреть", "Редактировать выделенную в списке продажу"));

        if (domain != ExtaDomain.SALES_OPENED)
            actions.add(new ItemAction("Возобновить", "Вернуть продажу в открытые, чтобы продолжить работу по ней", FontAwesome.UNDO) {
                @Override
                public void fire(Object itemId) {
                    final Sale sale = GridItem.extractBean(table.getItem(itemId));
                    ConfirmDialog.show(UI.getCurrent(),
                            "Подтвердите действие...",
                            MessageFormat.format("Вы уверены, что хотите возобновить продажу № {0} и переместить ее в открытые?", sale.getNum()),
                            "Да", "Нет", () -> {
                                lookup(SaleRepository.class).reopenSale(sale);
                                refreshContainer();
                                NotificationUtil.showSuccess("Продажа успешно возобновлена");
                            });
                }
            });

        if (domain == ExtaDomain.SALES_OPENED) {
            actions.add(new ItemAction("Завершить", "Успешное завершение продажи", FontAwesome.FLAG_CHECKERED) {
                @Override
                public void fire(final Object itemId) {
                    final Sale sale = GridItem.extractBean(table.getItem(itemId));
                    ConfirmDialog.show(UI.getCurrent(),
                            "Подтвердите действие...",
                            MessageFormat.format("Вы уверены, что хотите завершить продажу № {0}?", sale.getNum()),
                            "Да", "Нет", () -> {
                                lookup(SaleRepository.class).finishSale(sale, Sale.Result.SUCCESSFUL);
                                refreshContainer();
                                NotificationUtil.showSuccess("Продажа успешно завершена");
                            });
                }
            });

            actions.add(new UIActionGroup("Отменить", "Отмена продажи", Fontello.CANCEL) {
                @Override
                protected List<UIAction> makeActionsGroup() {
                    final List<UIAction> group = newArrayList();
                    group.add(new ItemAction("Отказ контрагента (банка, дилера)", "Отказ банка или дилера в предоставлении услуги", FontAwesome.BANK) {
                        @Override
                        public void fire(final Object itemId) {
                            final Sale sale = GridItem.extractBean(table.getItem(itemId));
                            ConfirmDialog.show(UI.getCurrent(),
                                    "Подтвердите действие...",
                                    MessageFormat.format("Вы уверены, что хотите отменить продажу № {0} по причине отказа контрагента (банка, дилера)?", sale.getNum()),
                                    "Да", "Нет", () -> {
                                        lookup(SaleRepository.class).finishSale(sale, Sale.Result.VENDOR_REJECTED);
                                        refreshContainer();
                                        NotificationUtil.showSuccess("Продажа отменена контрагентом");
                                    });
                        }
                    });

                    group.add(new ItemAction("Отказ клиента", "Отказ клиента от услуги", FontAwesome.USER) {
                        @Override
                        public void fire(final Object itemId) {
                            final Sale sale = GridItem.extractBean(table.getItem(itemId));
                            ConfirmDialog.show(UI.getCurrent(),
                                    "Подтвердите действие...",
                                    MessageFormat.format("Вы уверены, что отменить продажу № {0} по причине отказа клиента?", sale.getNum()),
                                    "Да", "Нет", () -> {
                                        lookup(SaleRepository.class).finishSale(sale, Sale.Result.CLIENT_REJECTED);
                                        refreshContainer();
                                        NotificationUtil.showSuccess("Продажа отменена клиентом");
                                    });
                        }
                    });

                    return group;
                }
            });
        }

//		actions.add(new ItemAction("Статус БП", "Показать панель статуса бизнес процесса к которому привязана текущая продажа", Fontello.SITEMAP) {
//            @Override
//            public void fire(Object itemId) {
//                final Sale curObj = extractBean(table.getItem(itemId));
//
//                // Ищем процесс к которому привязана текущая продажа
//                RuntimeService runtimeService = lookup(RuntimeService.class);
//                ProcessInstance process =
//                        runtimeService.createProcessInstanceQuery()
//                                .includeProcessVariables()
//                                .variableValueEquals("sale", curObj)
//                                .singleResult();
//
//                if (process != null) {
//                    // Показать статус выполнения процесса
//                    BPStatusForm statusForm = new BPStatusForm(process.getProcessInstanceId());
//                    statusForm.showModal();
//                } else {
//                    NotificationUtil.showWarning("Нет бизнес процесса с которым связана текущая продажа.");
//                }
//            }
//        });

        return actions;
    }

}
