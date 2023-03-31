package org.vaadin.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.gridpro.EditColumnConfigurator;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.shared.ui.Transport;

/**
 * A sample Vaadin view class.
 * <p>
 * To implement a Vaadin view just extend any Vaadin component and
 * use @Route annotation to announce it in a URL as a Spring managed
 * bean.
 * Use the @PWA annotation make the application installable on phones,
 * tablets and some desktop browsers.
 * <p>
 * A new instance of this class is created for every new user and every
 * browser tab/window.
 */
@Route
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/gridpro-styles.css", themeFor = "vaadin-grid-pro")
@JsModule("./src/grid-fix.js")
@Push(transport = Transport.LONG_POLLING, value = PushMode.AUTOMATIC)
public class MainView extends VerticalLayout {

	private GridPro<Person> grid = new GridPro<>();

    private Collection<Person> createExamplePersons(int count) {
    	List<Person> list = new ArrayList<>();
    	for (int i = 0; i < count; i++) {
			Person p = new Person();
			p.setFirstName("First name " + i);
			p.setLastName("Lastname " + i);
			p.setEmail("Email" + i);
			list.add(p);
		}
        return list;
    }

    public MainView() {
        // Setup a grid with random data
        grid.setItems(createExamplePersons(100));
        grid.setEditOnClick(true);
        
        for (int i = 0; i < 30; i++) {
        	EditColumnConfigurator<Person> fnCol = grid.addEditColumn(Person::getFirstName, TemplateRenderer .<Person> of("<span>[[item.valuefn"+i+"]]</span>").withProperty("valuefn" + i, Person::getFirstName));
        	
        	TextField tf1 = new TextField();
        	tf1.addFocusListener(evt -> tf1.getElement().executeJs("$0.inputElement.select()", tf1.getElement()));
        	fnCol.custom(tf1, (item, newValue) -> {
        		item.setFirstName(newValue);
        		updateItem(item);
        	}).setHeader("First name");
        	
        	EditColumnConfigurator<Person> lnCol = grid.addEditColumn(Person::getLastName, TemplateRenderer .<Person> of("<span>[[item.valueln"+i+"]]</span>").withProperty("valueln" + i, Person::getLastName));
        	TextField tf2 = new TextField();
        	tf2.addFocusListener(evt -> tf2.getElement().executeJs("$0.inputElement.select()", tf2.getElement()));
        	lnCol.custom(tf2,(item, newValue) -> {
        		item.setLastName(newValue);
        		updateItem(item);
        	}).setHeader("Last name");
        	
        	grid.addColumn(TemplateRenderer .<Person> of("<div onclick$=\"\">XXX</div>")).setHeader("R/O");
        	
        	CustomSelectComponent select = new CustomSelectComponent();
        	select.setWidthFull();
        	select.setItems("0", "1", "2");
        	
        	EditColumnConfigurator<Person> modeCol = grid.addEditColumn(Person::getMode, TemplateRenderer .<Person> of("<span class='alert-cell' style='min-width:50px'>[[item.valuemd"+i+"]]</span>").withProperty("valuemd" + i, Person::getMode));
        	modeCol.custom(select, (item, newValue) -> {
        		item.setMode(newValue);
        		updateItem(item);
        	})
        	.setWidth("200px")
        	.setResizable(true).setHeader("Mode").setClassNameGenerator(x -> "alert-cell");
		}
        

        add(grid);
    }
    
    

    private void updateItem(Person item) {
		if (item.getMode() == null) {
			item.setMode("0");
		}
		item.setMode(Integer.toString((Integer.parseInt(item.getMode()) + 1 ) % 3));
		getUI().ifPresent(ui -> {
			final UI finalUI = ui;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					finalUI.access(() -> {
						grid.getDataProvider().refreshItem(item);
					});
				}
			};
			Thread t = new Thread(runnable);
			try {
				t.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			t.start();
		});
	}

	public static class Person {
        private String firstName, lastName;
        private String email = "";
        private String mode = "0";


        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        
        @Override
        public boolean equals(Object obj) {
        	if (obj == null) {
        		return false;
        	}
        	
        	Person p2 = (Person) obj;
        	return Objects.equals(firstName, p2.firstName);
        }

		public String getMode() {
			return mode;
		}

		public void setMode(String mode) {
			this.mode = mode;
		}
    }
}