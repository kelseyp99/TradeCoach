package com.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.oracle.tutorial.jdbc.ParametersTable;
import com.workers.PortfoliosGroup;

import java.awt.Toolkit;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;

import java.text.Format;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class Settings extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private GUI belongsToGUI;
	private JCheckBox chckbxRefreshYql;
	private JSpinner spinner;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			Settings dialog = new Settings(new GUI());
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public Settings(GUI parent) {
	    super(parent.getFrmTradecoach(), "About Dialog", true);
	    this.setBelongsToGUI(parent);
	    setTitle("Settings");
	    setIconImage(Toolkit.getDefaultToolkit().getImage(AboutDialog.class.getResource("/com/gui/images/hat-bowlhat-icon.png")));

		setIconImage(Toolkit.getDefaultToolkit().getImage(Settings.class.getResource("/com/gui/images/hat-bowlhat-icon.png")));
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			panel.setLayout(null);
			contentPanel.add(panel);
			{
				JFormattedTextField formattedTextField = new JFormattedTextField((Format) null);
				formattedTextField.setHorizontalAlignment(SwingConstants.CENTER);
				formattedTextField.setBounds(118, 5, 218, 20);
				panel.add(formattedTextField);
			}
			{
				JLabel label = new JLabel("Max Initial Capital");
				label.setVerticalAlignment(SwingConstants.TOP);
				label.setBounds(22, 8, 85, 14);
				panel.add(label);
			}
			{
				textField = new JTextField();
				textField.setText("C:\\Users\\Phil\\Google Drive\\Stock%20Market\\Trades2.csv");
				textField.setColumns(10);
				textField.setBounds(118, 37, 218, 20);
				panel.add(textField);
			}
			{
				JLabel label = new JLabel("Initial Portfolio File:");
				label.setBounds(22, 40, 166, 14);
				panel.add(label);
			}
			{
				JButton button = new JButton("...");
				button.setBounds(346, 36, 45, 23);
				panel.add(button);
			}
			
			chckbxRefreshYql = new JCheckBox("Refresh YQL");
			chckbxRefreshYql.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {   
						@Override
						public void run() {	
							try {
								getParametersTable().setRefreshOnlineDafault(chckbxRefreshYql.isSelected());
							} catch (SQLException e) {
								e.printStackTrace();
							};
						}
					});
				}
			});
			chckbxRefreshYql.setBounds(22, 66, 97, 23);
			panel.add(chckbxRefreshYql);
			
			setSpinner(new JSpinner());
			getSpinner().addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent arg0) {
					SwingUtilities.invokeLater(new Runnable() {   
						@Override
						public void run() {	
							try {
								getParametersTable().setMaxTrailingStopLayers((int) getSpinner().getValue());
							} catch (SQLException e) {
								e.printStackTrace();
							};
						}
					});
				}
			});
			getSpinner().setBounds(22, 96, 29, 20);
			panel.add(getSpinner());
			
			JLabel lblPerferredMaxLayers = new JLabel("Perferred Max Layers");
			lblPerferredMaxLayers.setBounds(61, 99, 150, 14);
			panel.add(lblPerferredMaxLayers);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		intialize();
	}

	private void intialize() {
		this.getChckbxRefreshYql().setSelected(this.getParametersTable().isRefreshOnlineDafault());
	}

	public JPanel getContentPanel() {
		return contentPanel;
	}

	public GUI getBelongsToGUI() {
		return belongsToGUI;
	}

	public void setBelongsToGUI(GUI belongsToGUI) {
		 this.belongsToGUI = belongsToGUI;
	}
	
	private ParametersTable getParametersTable() {
		return this.getPortfolios().getParametersTable();		
	}

	private PortfoliosGroup getPortfolios() {
		return this.getBelongsToGUI().getPortfoliosGroup();
	}

	public JCheckBox getChckbxRefreshYql() {
		return chckbxRefreshYql;
	}

	public void setChckbxRefreshYql(JCheckBox chckbxRefreshYql) {
		this.chckbxRefreshYql = chckbxRefreshYql;
	}

	public JSpinner getSpinner() {
		return spinner;
	}

	public void setSpinner(JSpinner spinner) {
		this.spinner = spinner;
	}
}
