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
 * @version $Id: IndexServiceLabelListener.java,v 1.1 2007/03/14 20:32:08 keshav
 *          Exp $
 */
public class IndexServiceLabelListener implements MouseListener {
	private static final String GRID_PORT_KEY = "gridPort";

	private static final String GRID_HOST_KEY = "gridHost";

	private Log log = LogFactory.getLog(this.getClass());

	private int DEFAULT_PORT = 8080;

	private String DEFAULT_HOST = "cagrid-index.nci.nih.gov";

	private JDialog indexServiceDialog = null;

	private String host = DEFAULT_HOST;

	private int port = DEFAULT_PORT;

	JLabel indexServiceLabel = null;

	JButton indexServiceButton = null;

	public IndexServiceLabelListener(JLabel indexServiceLabel) {
		super();
		this.indexServiceLabel = indexServiceLabel;

		indexServiceButton = new JButton("Grid Services");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		log.debug("changing url");

		// TODO can you refactor a lot of this into a pattern? See GuiTest.
		indexServiceDialog = new JDialog();
		DefaultFormBuilder indexServicePanelBuilder = new DefaultFormBuilder(
				new FormLayout("right:20dlu"));

		readProperties();

		final JTextField hostField = new JTextField(host);
		final JTextField portField = new JTextField("" + port);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				port = Integer.parseInt(portField.getText());
				host = hostField.getText();

				saveProperties();

				indexServiceDialog.dispose();

			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				indexServiceDialog.dispose();
			}
		});

		/* add to button panel */
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		/* the builder */
		indexServicePanelBuilder.appendColumn("5dlu");
		indexServicePanelBuilder.appendColumn("45dlu");

		indexServicePanelBuilder.append("host", hostField);
		indexServicePanelBuilder.append("port", portField);

		JPanel indexServicePanel = new JPanel(new BorderLayout());
		indexServicePanel.add(indexServicePanelBuilder.getPanel());
		indexServicePanel.add(buttonPanel, BorderLayout.SOUTH);
		indexServiceDialog.add(indexServicePanel);
		indexServiceDialog.setModal(true);
		indexServiceDialog.pack();
		Util.centerWindow(indexServiceDialog);
		indexServiceDialog.setVisible(true);
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
	 * 
	 * @return int
	 */
	public int getPort() {
		readProperties();
		return port;
	}

	/**
	 * @return JButton
	 */
	public JButton getIndexServiceButton() {
		return indexServiceButton;
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
			properties.setProperty(this.getClass(), GRID_PORT_KEY, String
					.valueOf(port));
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
		String savedPort = null;
		try {
			savedHost = pm.getProperty(this.getClass(), GRID_HOST_KEY, host);
			if (!StringUtils.isEmpty(savedHost)) {
				host = savedHost;
			}
			savedPort = pm.getProperty(this.getClass(), GRID_PORT_KEY, String
					.valueOf(port));
			if (!StringUtils.isEmpty(savedPort)) {
				port = Integer.parseInt(savedPort);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
