package org.vaadin.example;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.select.Select;

public class CustomSelectComponent extends CustomField<String> {
	
	private Select<String> select;
	
	
	public CustomSelectComponent() {
		Icon icon = VaadinIcon.EXCLAMATION_CIRCLE.create();
		icon.setSize("16px");
		select = new Select<>();
		select.setWidthFull();
		select.getElement().addEventListener("click", evt -> {}).addEventData("event.stopPropagation()");
		Div iconPart = new Div(icon, select);
		iconPart.setClassName("alert-cell");
		iconPart.setWidthFull();
		add(iconPart);
		setWidthFull();
	}

	@Override
	protected String generateModelValue() {
		return select.getValue();
	}

	@Override
	protected void setPresentationValue(String newPresentationValue) {
		select.setValue(newPresentationValue);
	}

	public void setItems(String... strings) {
		select.setItems(strings);
	}
	

}
