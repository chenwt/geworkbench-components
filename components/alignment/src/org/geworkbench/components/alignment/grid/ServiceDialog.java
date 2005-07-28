package org.geworkbench.components.alignment.grid;

import com.borland.jbcl.layout.VerticalFlowLayout;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;


/**
 * <p>Title: Grid Service Dialog</p>
 * <p>Description: Class S ServiceDialog takes user's input for creating
 * a new session.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 *
 * @author Xiaoqing Zhang,  Aner
 * @version 1.0
 */

class ServiceDialog extends JDialog {
    BorderLayout borderLayout2 = new BorderLayout();
    Border border1;
    private boolean canceled = false;
    Border border2;
    TitledBorder titledBorder1;
    Border border3;
    JPanel jPanel1 = new JPanel();
    JButton cancelButton = new JButton();
    JButton connectButton = new JButton();
    VerticalFlowLayout verticalFlowLayout1 = new VerticalFlowLayout();
    JPanel jPanel2 = new JPanel();
    Border border4;
    JPanel jPanel3 = new JPanel();
    VerticalFlowLayout verticalFlowLayout2 = new VerticalFlowLayout();
    Border border5;
    TitledBorder titledBorder2;
    Border border6;
    GridBagLayout gridBagLayout3 = new GridBagLayout();
    Border border7;

    //value to be returned
    public static int CANCEL_OPTION = 0;
    public static int CONNECT_OPTION = 1;
    private int returnValue = CANCEL_OPTION;
    private static int sessionNo = 1;
    JPanel jPanel5 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    ServiceDataViewPanel loginPanel = null;
    JPanel jPanel4 = new JPanel();
    JLabel serviceL = new JLabel();
    Border border8;
    TitledBorder titledBorder3;
    Border border9;
    JComboBox serviceName = new JComboBox();
    GridBagLayout gridBagLayout1 = new GridBagLayout();

    public ServiceDialog(Frame frame, String title, ServiceDataModel model, boolean modal) {
        super(frame, title, modal);

        loginPanel = new ServiceDataViewPanel(model);
        try {
            jbInit();
            pack();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        initDialog();
    }

    public ServiceDialog(ServiceDataModel model) {
        this(null, "Service Information", model, true);
    }

    public ServiceDialog(String title, ServiceDataModel model) {
        this(null, title, model, true);
    }

    private void jbInit() throws Exception {
        border1 = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), BorderFactory.createEmptyBorder(5, 5, 5, 5));
        border2 = BorderFactory.createLineBorder(Color.white, 1);
        titledBorder1 = new TitledBorder(BorderFactory.createLineBorder(Color.white, 1), "Server Login");
        border3 = BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(Color.gray, 1), "Services"), BorderFactory.createEmptyBorder(2, 2, 2, 2));
        border4 = BorderFactory.createCompoundBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED, Color.white, new Color(165, 163, 151)), "Session's Server"), BorderFactory.createEmptyBorder(1, 1, 1, 1));
        border5 = new EtchedBorder(EtchedBorder.RAISED, Color.white, new Color(165, 163, 151));
        titledBorder2 = new TitledBorder(border5, "Please ");
        border6 = BorderFactory.createCompoundBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED, Color.white, new Color(165, 163, 151)), "Services"), BorderFactory.createEmptyBorder(2, 1, 1, 1));
        border7 = new TitledBorder(BorderFactory.createEmptyBorder(), "");
        border8 = BorderFactory.createEmptyBorder(1, 1, 1, 1);
        titledBorder3 = new TitledBorder(new EtchedBorder(EtchedBorder.RAISED, Color.white, new Color(142, 142, 142)), "Service Data");
        border9 = BorderFactory.createCompoundBorder(titledBorder3, BorderFactory.createEmptyBorder(1, 1, 1, 1));
        this.getContentPane().setLayout(borderLayout2);
        jPanel1.setLayout(gridBagLayout3);
        jPanel1.setBorder(border1);
        jPanel1.setMinimumSize(new Dimension(500, 250));
        jPanel1.setPreferredSize(new Dimension(500, 250));
        jPanel1.setRequestFocusEnabled(true);
        this.setResizable(false);
        ServiceDialog_this_keyAdapter keyAdapter = new ServiceDialog_this_keyAdapter(this);
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new ServiceDialog_cancelButton_actionAdapter(this));
        cancelButton.addKeyListener(keyAdapter);
        cancelButton.setActionCommand("cancelAction");
        connectButton.setMaximumSize(new Dimension(73, 25));
        connectButton.setToolTipText("");
        connectButton.setActionCommand("connectAction");
        connectButton.setText("Create");
        connectButton.addActionListener(new ServiceDialog_connectButton_actionAdapter(this));
        connectButton.addKeyListener(keyAdapter);
        connectButton.addActionListener(new ServiceDialog_connectButton_actionAdapter(this));
        jPanel2.setLayout(verticalFlowLayout1);
        jPanel3.setLayout(verticalFlowLayout2);
        jPanel2.setBackground(UIManager.getColor("Menu.background"));
        jPanel2.setBorder(border7);
        jPanel3.setMinimumSize(new Dimension(600, 230));
        jPanel3.setPreferredSize(new Dimension(600, 230));
        verticalFlowLayout1.setAlignment(VerticalFlowLayout.BOTTOM);
        verticalFlowLayout1.setVgap(0);
        verticalFlowLayout1.setHorizontalFill(true);
        verticalFlowLayout1.setVerticalFill(false);
        verticalFlowLayout2.setAlignment(VerticalFlowLayout.TOP);
        verticalFlowLayout2.setHgap(5);
        verticalFlowLayout2.setVgap(5);
        verticalFlowLayout2.setHorizontalFill(true);
        verticalFlowLayout2.setVerticalFill(true);
        jPanel5.setLayout(borderLayout1);
        jPanel4.setEnabled(true);
        jPanel4.setFont(new java.awt.Font("MS Sans Serif", 0, 11));
        jPanel4.setBorder(border6);
        jPanel4.setDebugGraphicsOptions(0);
        jPanel4.setDoubleBuffered(true);
        jPanel4.setLayout(gridBagLayout1);
        serviceL.setText("Server Name:");
        jPanel5.setBorder(border9);
        jPanel4.add(serviceL, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 2, 0, 0), 0, 0));
        jPanel4.add(serviceName, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 12, 0, 1), 174, 4));
        jPanel3.add(jPanel4, null);
        jPanel3.add(jPanel5, null);
        jPanel5.add(loginPanel, BorderLayout.CENTER);
        jPanel1.add(jPanel2, new GridBagConstraints(2, 1, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        jPanel2.add(connectButton, null);
        jPanel2.add(cancelButton, null);
        jPanel1.add(jPanel3, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 2, 0), 0, 0));
        this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    }

    /**
     * Initialize general Dialog behavors.
     */
    private void initDialog() {
        //center the dialog by default
        super.setLocationRelativeTo(null);
        //Handle window closing correctly.
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        //Ensure the session name field gets the first focus.
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
                serviceName.requestFocusInWindow();
                //serviceName.setText("session" + sessionNo);
                //++sessionNo;
                //serviceName.selectAll();
            }
        });
    }

    /**
     * Get the return value of this dialog.
     * This method should be called after the show method was called.
     */
    public int getReturnValue() {
        return returnValue;
    }


    private boolean verifySession() {
        if (((String) (serviceName.getSelectedItem())).trim().equals("")) {
            popMessage("Please enter session name.");
            return false;
        }
        return true;
    }


    private void popMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }


    public String getserviceName() {
        return (String) (serviceName.getSelectedItem());
    }

    void cancelButton_actionPerformed(ActionEvent e) {
        returnValue = CANCEL_OPTION;
        --sessionNo;
        setVisible(false);
    }

    boolean verify() {
        return true;
    }

    void connectButton_actionPerformed(ActionEvent e) {
        if (verify()) {
            returnValue = CONNECT_OPTION;
            setVisible(false);
        }
        return;
    }

    class ServiceDialog_cancelButton_actionAdapter implements java.awt.event.ActionListener {
        ServiceDialog adaptee;

        ServiceDialog_cancelButton_actionAdapter(ServiceDialog adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(ActionEvent e) {
            adaptee.cancelButton_actionPerformed(e);
        }
    }

    class ServiceDialog_connectButton_actionAdapter implements java.awt.event.ActionListener {
        ServiceDialog adaptee;

        ServiceDialog_connectButton_actionAdapter(ServiceDialog adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(ActionEvent e) {
            adaptee.connectButton_actionPerformed(e);
        }
    }

    /**
     * Used for key short cut on the session creation.
     *
     * @param e KeyEvent
     */
    void this_keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_ENTER && e.getSource() != cancelButton) {
            connectButton_actionPerformed(null);
        } else if (keyCode == KeyEvent.VK_ENTER && e.getSource() == cancelButton) {
            cancelButton_actionPerformed(null);
        }
    }
}

class ServiceDialog_this_keyAdapter extends java.awt.event.KeyAdapter {
    ServiceDialog adaptee;

    ServiceDialog_this_keyAdapter(ServiceDialog adaptee) {
        this.adaptee = adaptee;
    }

    public void keyReleased(KeyEvent e) {
        adaptee.this_keyReleased(e);
    }
}

class ServiceDialog_connectButton_actionAdapter implements java.awt.event.ActionListener {
    ServiceDialog adaptee;

    ServiceDialog_connectButton_actionAdapter(ServiceDialog adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.connectButton_actionPerformed(e);
    }
}
