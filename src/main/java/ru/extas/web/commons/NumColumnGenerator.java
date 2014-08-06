package ru.extas.web.commons;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import ru.extas.model.lead.Lead;
import ru.extas.web.commons.converters.StringToJodaDTConverter;
import ru.extas.web.lead.LeadEditForm;

import static ru.extas.web.commons.GridDataDecl.*;
import static ru.extas.web.commons.GridItem.extractBean;

/**
* @author Valery Orlov
*         Date: 06.08.2014
*         Time: 17:48
*/
public abstract class NumColumnGenerator extends ComponentColumnGenerator {

    private StringToJodaDTConverter dtConverter = new StringToJodaDTConverter("EEE, dd MMM, HH:mm");

    private String numProperty;

    public NumColumnGenerator(String numProperty) {
        this.numProperty = numProperty;
    }

    public NumColumnGenerator() {
        this("num");
    }

    @Override
    public Object generateCell(Object columnId, final Item item) {
        Property numProp = item.getItemProperty(numProperty);
        VerticalLayout cell = new VerticalLayout();
        Button link = new Button(numProp.getValue().toString());
        link.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                fireClick(item);
            }
        });
        link.addStyleName("link");
        cell.addComponent(link);
        //cell.setComponentAlignment(link, Alignment.TOP_RIGHT);
        final Label createdAt = new Label(item.getItemProperty("createdAt"));
        createdAt.setConverter(dtConverter);
        cell.addComponent(createdAt);
        //cell.setComponentAlignment(createdAt, Alignment.BOTTOM_RIGHT);
        return cell;
    }

    public abstract void fireClick(final Item item);
}
