package net.hollowbit.archipeloeditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import net.hollowbit.archipeloeditor.changes.Change;
import net.hollowbit.archipeloeditor.changes.ResizeChange;
import net.hollowbit.archipeloeditor.changes.SettingsChange;


public class MapSettingsEditor extends JFrame {

	private static final long serialVersionUID = 1L;
	private JTextField textFieldName;
	private JTextField textFieldDisplayName;
	private JTextField textFieldMusic;
	
	//Editor for changing map settings
	public MapSettingsEditor (MainEditor editor, MapSettingsEditorListener listener) {
		setAlwaysOnTop(true);
		setResizable(false);
		setTitle("Map Settings");
		setBounds(100, 100, 450, 375);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setIconImage(MainEditor.ICON);
		setLocationRelativeTo(null);
		getContentPane().setLayout(null);
		
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addMouseListener(new MouseAdapter(){//Close dialog without saving

			@Override
			public void mouseReleased(MouseEvent arg0) {
				setVisible(false);
				dispose();
			}
			
		});
		btnCancel.setBounds(345, 288, 89, 23);
		getContentPane().add(btnCancel);
		
		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(10, 11, 198, 14);
		getContentPane().add(lblName);
		
		textFieldName = new JTextField();
		textFieldName.setText(new String(editor.getMap().getName()));
		textFieldName.setBounds(10, 36, 198, 20);
		getContentPane().add(textFieldName);
		textFieldName.setColumns(10);
		
		JLabel lblDisplayName = new JLabel("Display Name:");
		lblDisplayName.setBounds(10, 70, 198, 14);
		getContentPane().add(lblDisplayName);
		
		textFieldDisplayName = new JTextField();
		textFieldDisplayName.setText(new String(editor.getMap().getDisplayName()));
		textFieldDisplayName.setBounds(10, 90, 198, 20);
		textFieldDisplayName.setColumns(10);
		getContentPane().add(textFieldDisplayName);
		
		JLabel lblNaturalLighting = new JLabel("Natural Lighting:");
		lblNaturalLighting.setBounds(10, 213, 86, 14);
		getContentPane().add(lblNaturalLighting);
		
		JCheckBox chckbxYesno = new JCheckBox("Yes/No");
		chckbxYesno.setBounds(6, 224, 97, 23);
		getContentPane().add(chckbxYesno);
		
		JLabel lblWidth = new JLabel("Width:");
		lblWidth.setBounds(278, 813, 46, 14);
		getContentPane().add(lblWidth);
		
		JLabel lblHeight = new JLabel("Height:");
		lblHeight.setBounds(278, 160, 46, 14);
		getContentPane().add(lblHeight);
		
		JLabel lblMusic = new JLabel("Music:");
		lblMusic.setBounds(162, 203, 46, 14);
		getContentPane().add(lblMusic);
		
		textFieldMusic = new JTextField();
		textFieldMusic.setBounds(162, 225, 162, 20);
		getContentPane().add(textFieldMusic);
		textFieldMusic.setColumns(10);
		
		final JSpinner spinnerWidth = new JSpinner();
		spinnerWidth.setValue(new Integer(editor.getMap().getWidth()));
		spinnerWidth.setBounds(345, 128, 89, 20);
		getContentPane().add(spinnerWidth);
		
		final JSpinner spinnerHeight = new JSpinner();
		spinnerHeight.setValue(new Integer(editor.getMap().getHeight()));
		spinnerHeight.setBounds(345, 160, 89, 20);
		getContentPane().add(spinnerHeight);
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {//Save changes to map
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Change[] changes;
				
				//Makes sure to only resize the map if the width and height are different
				if ((int) spinnerWidth.getValue() != editor.getMap().getWidth() || (int) spinnerHeight.getValue() != editor.getMap().getHeight()) {
					changes = new Change[2];
					changes[1] = new ResizeChange(editor.getMap());
					editor.getMap().resize((int) spinnerWidth.getValue(), (int) spinnerHeight.getValue());
				} else
					changes = new Change[1];
				
				changes[0] = new SettingsChange(editor.getMap());
				
				//Applies settings to map
				editor.getChangeList().addChanges(changes);
				editor.getMap().setName(textFieldName.getText());
				editor.getMap().setDisplayName(textFieldDisplayName.getText());
				editor.getMap().setMusic(textFieldMusic.getText());
				
				listener.mapSettingsChanged();
				setVisible(false);
				dispose();
			}
		});
		btnSave.setBounds(246, 288, 89, 23);
		getContentPane().add(btnSave);
		getRootPane().setDefaultButton(btnSave);
	}
	
	public interface MapSettingsEditorListener {
		
		public void mapSettingsChanged ();
		
	}
	
}
