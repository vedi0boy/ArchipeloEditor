package net.hollowbit.archipeloeditor;

import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.NumberFormatter;

import net.hollowbit.archipeloeditor.entity.EntityType;
import net.hollowbit.archipeloshared.Direction;
import net.hollowbit.archipeloshared.PropertyDefinition;

public class EntityDefiner extends JFrame {
	
	private static final int SPACE_BETWEEN_ELEMENTS = 30;
	private static final int SPACE_OF_LABELS = 110;
	private static final int SPACE_FOR_DEFAULT_VALUES = 150;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public EntityDefiner (MainEditor editor, EntityType entityType, float x, float y) {
		setAlwaysOnTop(true);
		setResizable(false);
		setTitle("Entity Definer");
		setBounds(100, 100, 325, 500);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setIconImage(MainEditor.ICON);
		setLocationRelativeTo(null);
		getContentPane().setLayout(null);
		
		//TODO Add base entity settings
		//TODO Add default properties
		//TODO Add other properties
		
		JLabel requiredLabel = new JLabel("* Required");
		requiredLabel.setBounds(10, 10, 200, 30);
		getContentPane().add(requiredLabel);
		
		//ID
		JLabel idLabel = new JLabel("Unique ID*: ");
		idLabel.setBounds(10, 30, 200, 30);
		getContentPane().add(idLabel);
		
		JTextField idField = new JTextField();
		idField.setBounds(100, 35, 200, 20);
		getContentPane().add(idField);
		
		//Animation
		JLabel animLabel = new JLabel("Animation: ");
		animLabel.setBounds(10, 60, 200, 30);
		getContentPane().add(animLabel);
		
		JTextField animField = new JTextField();
		animField.setBounds(100, 65, 200, 20);
		getContentPane().add(animField);
		
		//Properties
		JLabel defaultsLabel = new JLabel("Properties");
		defaultsLabel.setBounds(10, SPACE_FOR_DEFAULT_VALUES - 30, 200, 20);
		getContentPane().add(defaultsLabel);
		
		//Add all properties
		ArrayList<PropertyDefinition> definitions = new ArrayList<PropertyDefinition>();
		for (PropertyDefinition def : entityType.getData().defaultProperties)
			definitions.add(def);
		definitions.addAll(entityType.getData().properties);
		
		for (int i = 0; i < definitions.size(); i++) {
			PropertyDefinition propertyDefinition = definitions.get(i);
			JLabel label = new JLabel(propertyDefinition.name + (propertyDefinition.required ? "*": "") + ":");
			label.setBounds(10, i * SPACE_BETWEEN_ELEMENTS + SPACE_FOR_DEFAULT_VALUES, 200, 20);
			getContentPane().add(label);
			
			switch (propertyDefinition.getType()) {
			case INTEGER:
				SpinnerModel model = new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
				JSpinner spinner = new JSpinner(model);
				spinner.setBounds(SPACE_OF_LABELS, i * SPACE_BETWEEN_ELEMENTS + SPACE_FOR_DEFAULT_VALUES, 200, 20);
				getContentPane().add(spinner);
				break;
			case FLOAT:
				NumberFormat format = NumberFormat.getInstance();
				NumberFormatter formatter = new NumberFormatter(format);
				formatter.setValueClass(Float.class);
				formatter.setMinimum(Float.MIN_VALUE);
				formatter.setMaximum(Float.MAX_VALUE);
				formatter.setAllowsInvalid(false);
				formatter.setCommitsOnValidEdit(true);
				JFormattedTextField field = new JFormattedTextField(formatter);
				field.setBounds(SPACE_OF_LABELS, i * SPACE_BETWEEN_ELEMENTS + SPACE_FOR_DEFAULT_VALUES, 200, 20);
				getContentPane().add(field);
				break;
			case DOUBLE:
				NumberFormat format2 = NumberFormat.getInstance();
				NumberFormatter formatter2 = new NumberFormatter(format2);
				formatter2.setValueClass(Double.class);
				formatter2.setMinimum(Double.MIN_VALUE);
				formatter2.setMaximum(Double.MAX_VALUE);
				formatter2.setAllowsInvalid(false);
				formatter2.setCommitsOnValidEdit(true);
				JFormattedTextField field2 = new JFormattedTextField(formatter2);
				field2.setBounds(SPACE_OF_LABELS, i * SPACE_BETWEEN_ELEMENTS + SPACE_FOR_DEFAULT_VALUES, 200, 20);
				getContentPane().add(field2);
				break;
			case POINT:
				
				break;
			case LOCATION:
				
				break;
			case RECTANGLE:
				
				break;
			case DIRECTION:
				JComboBox<Direction> comboBox = new JComboBox<Direction>();
				for (Direction direction : Direction.values())
					comboBox.addItem(direction);
				comboBox.setSelectedIndex(0);
				comboBox.setBounds(SPACE_OF_LABELS, i * SPACE_BETWEEN_ELEMENTS + SPACE_FOR_DEFAULT_VALUES, 200, 20);
				getContentPane().add(comboBox);
				break;
			case STRING:
				JTextField field3 = new JTextField();
				field3.setBounds(SPACE_OF_LABELS, i * SPACE_BETWEEN_ELEMENTS + SPACE_FOR_DEFAULT_VALUES, 200, 20);
				getContentPane().add(field3);
				break;
			case BOOLEAN:
				JCheckBox checkBox = new JCheckBox();
				checkBox.setBounds(SPACE_OF_LABELS, i * SPACE_BETWEEN_ELEMENTS + SPACE_FOR_DEFAULT_VALUES, 200, 20);
				getContentPane().add(checkBox);
				break;
			case JSON:
				
				break;
			case STYLE:
				JComboBox<Integer> comboBox2 = new JComboBox<Integer>();
				for (int u = 0; u < entityType.getData().numberOfStyles; u++)
					comboBox2.addItem(u);
				comboBox2.setSelectedIndex(0);
				comboBox2.setBounds(SPACE_OF_LABELS, i * SPACE_BETWEEN_ELEMENTS + SPACE_FOR_DEFAULT_VALUES, 200, 20);
				getContentPane().add(comboBox2);
				break;
			}
		}
		
	}

}
