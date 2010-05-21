/**
 * 
 */
package org.geworkbench.components.filtering;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author zji
 * @version $Id$
 *
 */
public class FilterOptionPanel extends JPanel {
	private static Log log = LogFactory.getLog(FilterOptionPanel.class);
	enum Option { PERCENT_REMOVAL, NUMBER_REMOVAL };

	private static final long serialVersionUID = -580698616635439456L;
	JRadioButton percentRemovalButton;
	JRadioButton numberRemovalButton;
	
	JTextField percentField = null;
	JTextField numberField = null;
	
	public static int arrayNumber =0;
	
	FilterOptionPanel() {
		super(new GridBagLayout());
		
		percentRemovalButton = new JRadioButton("Remove the marker if the percentage of matching arrays is more than");
		numberRemovalButton = new JRadioButton("Remove the marker if the number of matching arrays is more than");
		
		percentField = new JTextField();
		percentField.setText("40");	
		percentField.setColumns(5);		 
		numberField = new JTextField();
		numberField.setText("0");
		numberField.setColumns(5);

		GridBagConstraints c = new GridBagConstraints();
		add(percentRemovalButton);
		add(percentField);
		add(new JLabel("%"));
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_START;
		add(numberRemovalButton, c);
		add(numberField, c);
		c.gridy = 2;
		c.anchor = GridBagConstraints.LINE_START;
		
		//Group the radio buttons.
	    ButtonGroup group = new ButtonGroup();
	    group.add(percentRemovalButton);
	    group.add(numberRemovalButton);
	    
	    // default choice 
	    percentRemovalButton.setSelected(true);	    
	    numberField.setEnabled(false);
	    
	    percentRemovalButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
		    			percentField.setEnabled(true);
		    			numberField.setEnabled(false);
					}
				}
	    });
	    numberRemovalButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
		    			numberField.setEnabled(true);
		    			percentField.setEnabled(false);
					}
				}
	    });
	    Border border1 = BorderFactory.createEtchedBorder(Color.white,
	            new Color(165, 163, 151));
	    setBorder(new TitledBorder(border1, "Filtering Options"));

	    percentRemovalButton.setSelected(true);
	}
	
	Option getSelectedOption() {
		if(percentRemovalButton.isSelected()) return Option.PERCENT_REMOVAL;
		else if(numberRemovalButton.isSelected()) return Option.NUMBER_REMOVAL;
		else {
			log.error("Invalid selection of filter option");
			return null;
		}
		
	}
	
	int getNumberThreshold() {
		return Integer.parseInt(numberField.getText().trim());
	}

	double getPercentThreshold() {
		return Double.parseDouble(percentField.getText().trim())*0.01;
	}
	
	public String validateParameters()
	{
		String errorMessage = null;
		if(percentRemovalButton.isSelected()) 
		{	 
			Double percentNum = getDouble((percentField.getText()));
			 if (percentNum == null || percentNum < 0 || percentNum > 100) {
				 errorMessage = "Please enter 0 to 100 for percentage of matching arrays.";
			 }
	            
		}
		else if (numberRemovalButton.isSelected())
		{
			Integer num = getInteger((numberField.getText()));
			 if (num == null || num < 0 || num > arrayNumber-1) {
				 errorMessage = "Please enter 0 to " + (arrayNumber-1) + " for number of matching arrays.";
			 }
		}
			
		return errorMessage;
	}
	
	
	private Double getDouble(String s)
	{
		try
		{
			if (s==null)
				return null;
			else
				return new Double(s.trim());
		}catch(Exception ex)
		{
			return null;
			
		}
		 
	}
	
	private Integer getInteger(String s)
	{
		try
		{
			if (s==null)
				return null;
			else
				return new Integer(s.trim());
		}catch(Exception ex)
		{
			return null;
			
		}
		 
	}
	 

}
