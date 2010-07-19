/*
 * The analysis project
 * 
 * Copyright (c) 2008 Columbia University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.geworkbench.components.cagrid.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.engine.properties.PropertiesManager;
import org.geworkbench.util.Util;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * @author keshav
 * @version $Id: dispatcherLabelListener.java,v 1.1 2007/03/14 20:32:08 keshav
 *          Exp $
 */
public class DispatcherLabelListener implements MouseListener {

	private static final String GRID_HOST_KEY = "dispatcherURL";

	private Log log = LogFactory.getLog(this.getClass());

	private JDialog dispatcherDialog = null;

	final static String DISPATCHER_URL = "dispatcher.url"; // used to get
															// dispatcher url
															// from
															// application.properties,
															// used as default
															// value.

	private String host = System.getProperty(DISPATCHER_URL);;

	JLabel dispatcherLabel = null;

	JButton dispatcherButton = null;

	public DispatcherLabelListener(JLabel dispatcherLabel) {
		super();
		this.dispatcherLabel = dispatcherLabel;
		dispatcherButton = new JButton("Grid Services");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		log.debug("changing url");

		// TODO can you refactor a lot of this into a pattern? See GuiTest.
		dispatcherDialog = new JDialog();
		DefaultFormBuilder dispatcherPanelBuilder = new DefaultFormBuilder(
				new FormLayout("right:20dlu"));

		readProperties();

		final JTextField hostField = new JTextField(host);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				host = hostField.getText();

				saveProperties();

				dispatcherDialog.dispose();

			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispatcherDialog.dispose();
			}
		});

		/* add to button panel */
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		/* the builder */
		dispatcherPanelBuilder.appendColumn("5dlu");
		dispatcherPanelBuilder.appendColumn("250dlu");

		dispatcherPanelBuilder.append("URL", hostField);

		JPanel dispatcherPanel = new JPanel(new BorderLayout());
		dispatcherPanel.add(dispatcherPanelBuilder.getPanel());
		dispatcherPanel.add(buttonPanel, BorderLayout.SOUTH);
		dispatcherDialog.add(dispatcherPanel);
		dispatcherDialog.setModal(true);
		dispatcherDialog.pack();
		Util.centerWindow(dispatcherDialog);
		dispatcherDialog.setVisible(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 * @return String
	 */
	public String getHost() {
		readProperties();
		return host;
	}

	/**
	 * @return JButton
	 */
	public JButton getDispatcherButton() {
		return dispatcherButton;
	}

	/**
	 * 
	 * 
	 */
	private void saveProperties() {

		PropertiesManager properties = PropertiesManager.getInstance();
		try {
			properties.setProperty(this.getClass(), GRID_HOST_KEY, String
					.valueOf(host));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * 
	 */
	private void readProperties() {
		PropertiesManager pm = PropertiesManager.getInstance();
		String savedHost = null;
		try {
			savedHost = pm.getProperty(this.getClass(), GRID_HOST_KEY, host);
			if (!StringUtils.isEmpty(savedHost)) {
				host = savedHost;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}