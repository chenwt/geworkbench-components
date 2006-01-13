package org.geworkbench.components.discovery;

import org.geworkbench.bison.datastructure.biocollections.Collection;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSCollection;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.DSMatchedPattern;
import org.geworkbench.bison.datastructure.complex.pattern.ParmsDataSet;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.DSSeqRegistration;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.components.discovery.view.PatternNode;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.SequenceDiscoveryTableEvent;
import org.geworkbench.events.SessionConnectEvent;
import org.geworkbench.util.PropertiesMonitor;
import org.geworkbench.util.remote.Connection;
import org.geworkbench.util.remote.ConnectionCreationException;
import org.geworkbench.util.remote.GlobusConnection;
import org.geworkbench.util.remote.SPLASHDefinition;
import org.geworkbench.util.session.*;
import org.geworkbench.util.session.dialog.SessionChooser;
import polgara.soapPD_wsdl.Parameters;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * <p>This AppComponent controls its associated widget SequenceDiscoveryViewWidget</p>
 * <p>Description: SequenceDiscoveryViewAppComponent is controller of its associated widget: SequenceDiscoveryViewWidget.
 * Could make this the primary event thrower for all events from SequenceDiscoveryViewWidget; at the
 * moment, SequenceDiscoveryViewWidget throws a row selection SequenceDiscoveryTableEvent.  </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Califano Lab </p>
 *
 * @author cal lab
 * @version 1.0
 */

@AcceptTypes({CSSequenceSet.class, ParmsDataSet.class}) public class SequenceDiscoveryViewAppComponent implements VisualPlugin, PropertyChangeListener {

    private SequenceDiscoveryViewWidget sDiscoveryViewWidget = null;

    //This is the currently selected database in the project.
    //It is updated every time a file selection occurs in the main project.
    private DSSequenceSet sequenceDB = null;

    //login data parameters are stored here
    private LoginPanelModel loginPanelModel = new LoginPanelModel();

    //intermediate values of the loginPanelModel are saved here
    LoginPanelModel tempLoginModel = new LoginPanelModel();

    public SequenceDiscoveryViewAppComponent() {
        try {
            sDiscoveryViewWidget = new SequenceDiscoveryViewWidget();
            sDiscoveryViewWidget.addPropertyChangeListener(this);
            sDiscoveryViewWidget.setSequenceDiscoveryViewAppComponent(this);
        } catch (Exception ex) {
            System.out.println("SequenceDiscoveryViewAppComponent::::::::constructor: " + ex.toString());
            ex.printStackTrace();
        }
    }

    /**
     * This method checks that a database file is set in the project.
     * If the file is set it creates a session.
     */
    private Session doNewSession() {
        Session session = null;
        //check that a database file is selected in the project.
        if (!isDiscoveryFileSet()) {
            showInfoMessage("Please select a sequence file.", "Select File");
            return session;
        }
        session = createSession();
        return session;
    }

    /**
     * This method creates a new session.
     * The session is created with the database file returned from getDivcoveryFile.
     *
     * @return the new session or null if no session was created.
     */
    private Session createSession() {
        //try to create this session
        Session aSession = null;
        //if the chooser is null, the user cancelled the dialog
        SessionChooser chooser = showChooser();
        if (chooser == null) {
            return aSession;
        }
        String host = chooser.getHostName();
        int port = chooser.getPortNum();
        String userName = chooser.getUserName();
        char[] password = chooser.getPassWord();
        String sName = chooser.getSession();

        try {
            aSession = connectToService(host, port, userName, password, sName);

        } catch (SessionCreationException exp) {
            showInfoMessage("Session was not created. " + exp.getMessage(), "Session Error");
            return aSession;
        }

        //save the user's choosing to the Properties file and to the model
        saveSessionProperties(host, port, userName);
        copyLoginPanelModel(tempLoginModel, loginPanelModel);

        return aSession;
    }

    private Session connectToService(String host, int port, String userName, char[] password, String sessionName) throws SessionCreationException {
        URL url = getURL(host, port);
        //establish a connection
        Connection connection = getConnection(url);
        //now try to login
        Logger logger = getLogger(connection, userName, password);

        return createSession(sessionName, connection, userName, logger.getUserId());
    }

    private Session createSession(String sessionName, Connection con, String uName, int uId) throws SessionCreationException {
        File seqFile = getDiscoveryFile();
        if (seqFile == null) {
            return null;
        }

        DSSequenceSet database = CSSequenceSet.getSequenceDB(seqFile);
        //the database will be saved with this name on the server.
        String databaseName = SPLASHDefinition.encodeFile(database.getFile(), uName);
        //create a session
        return new Session(sessionName, database, databaseName, con, uName, uId);
    }

    /**
     * This is a helper method to build a URL to a server
     *
     * @param host name of a host
     * @param port the port on the host
     * @return url
     */
    private URL getURL(String host, int port) throws SessionCreationException {
        try {
            return (Session.isNormalSession) ? Connection.getURL(host, port) : GlobusConnection.getURL(host, port);
        } catch (MalformedURLException ex) {
            throw new SessionCreationException("Could not form URL. (host: " + host + "port: " + port + ")");
        }
    }

    private Logger getLogger(Connection connection, String user, char[] password) throws SessionCreationException {
        Logger logger = null;
        try {
            logger = new Logger(connection, user, password);
        } catch (LoggerException exp) {
            throw new SessionCreationException("Login operation failed.");
        }
        return logger;
    }

    private Connection getConnection(URL url) throws SessionCreationException {
        try {
            return new Connection(url);
        } catch (ConnectionCreationException ex) {
            throw new SessionCreationException("Could not connect to the server.");
        }
    }

    public Component getComponent() {
        return sDiscoveryViewWidget;
    }

    private void showInfoMessage(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * We want to know when the user selects file
     */
    @Subscribe public void receive(ProjectEvent e, Object source) {
        if (e.getMessage().equalsIgnoreCase("Project Cleared")) {
            sequenceDB = null;
            return;
        }
            ProjectSelection selection = ((ProjectPanel) source).getSelection();
            DSDataSet df = selection.getDataSet();
            if (df != null) {
                //update db with the selected file in the project
                if (df instanceof DSSequenceSet) {
                    Parameters parms = null;
                    sequenceDB = (DSSequenceSet) df;
                    DSAncillaryDataSet ds = selection.getDataSubSet();
                    String subNodeID = null;
                    boolean withSubNode = false;
                    if( ds!= null && ds instanceof ParmsDataSet){
                        parms = ((ParmsDataSet)ds).getParameters();
                        subNodeID = ds.getID();
                        withSubNode = true;
                    }

                    sDiscoveryViewWidget.setSequenceDB((DSSequenceSet) df, withSubNode, subNodeID, parms);

                } else if (df instanceof ParmsDataSet) {
                    ParmsDataSet pds = (ParmsDataSet) df;
                    
                }
            }
    }

    @Subscribe public void sessionConnectAction(SessionConnectEvent evt, Object publisher) {
        //if evt is splash do...
        System.out.println("session Connect event.....");
        File seqFile = getDiscoveryFile();
        if (seqFile == null) {
            showInfoMessage("Please select a sequence file in the project.", "Splash Reconnect");
            return;
        }

        try {
            URL url = getURL(evt.getHost(), evt.getPort());
            //establish a connection
            Connection connection = getConnection(url);
            //now try to login
            Logger logger = getLogger(connection, evt.getUserName(), evt.getPassword().toCharArray());

            DSSequenceSet database = CSSequenceSet.getSequenceDB(seqFile);
            String localDatabase = SPLASHDefinition.encodeFile(database.getFile(), evt.getUserName());
            Session s = new Session(evt.getSessionName(), database, localDatabase, connection, logger.getUserName(), logger.getUserId(), evt.getSessionId());

            if (!checkDataFile(s, localDatabase, logger.getUserName())) {
                return;
            }

            sDiscoveryViewWidget.viewResult(s);
        } catch (SessionCreationException exp) {
            showInfoMessage("Session was not created. " + exp.getMessage(), "Session Error");
        }
    }

    /**
     * The function varifies that the selected data file in the same
     * as the session's data file.
     *
     * @param s Session
     * @return boolean
     */
    private boolean checkDataFile(Session s, String localDataFile, String user) {
        try {
            String remoteDatabase = s.getDataFileName();
            if (remoteDatabase.equals(localDataFile)) {
                return true;
            } else {
                File f = SPLASHDefinition.decode(remoteDatabase, user);
                String file = f.getAbsolutePath();
                showInfoMessage("The selected data file does not match\n" + file + " which is set for the session.\n" + "Please select the correct " + "file before trying to reconnect.", "Session Error");
                return false;
            }

        } catch (SessionOperationException exp) {
            showInfoMessage("Error while varifying data. " + exp.getMessage(), "Session Error");
        }
        return false;
    }

    /**
     * The method returns a session. Note: The method will popup
     * a dialog to create a session.
     *
     * @return the active session.
     */
    public synchronized Session getSession() {
        Session session = doNewSession();
        return session;
    }

    /**
     * The method pop the session chooser dialog.
     *
     * @return the a SessionChooser object if the user entered valid data,
     *         else null.
     */
    private SessionChooser showChooser() {
        copyLoginPanelModel(loginPanelModel, tempLoginModel);
        SessionChooser chooser = new SessionChooser(null, "New Session", tempLoginModel);
        int retVal = chooser.show();
        if (retVal == SessionChooser.CANCEL_OPTION) {
            return (null);
        }
        return chooser;
    }

    /**
     * copy the data from one LoginPanelModel to the other.
     *
     * @param from
     * @param to
     */
    private void copyLoginPanelModel(LoginPanelModel from, LoginPanelModel to) {
        to.setHostNames(from.getHostSet(), from.getHostName());
        to.setPort(from.getPort());
        to.setUserName(from.getUserName());
    }

    /**
     * This method passes the session properties to the Properties manager.
     *
     * @param host host name
     * @param port port number
     * @param user user name
     */
    private void saveSessionProperties(String host, int port, String user) {
        org.geworkbench.util.PropertiesMonitor pmMgr = PropertiesMonitor.getPropertiesMonitor();
        pmMgr.addHost(host);
        pmMgr.setPort(port);
        pmMgr.setUserName(user);
        pmMgr.setHostSelected(host);
        pmMgr.writeProperties();
    }

    /**
     * This method returns the file on which a discovery will be made on.
     *
     * @return file for discovery
     */
    public synchronized File getDiscoveryFile() {
        if (sequenceDB != null) {
            return sequenceDB.getFile();
        }
        return null;
    }

    /**
     * This method returns a true if a discovery file is selected.
     *
     * @return true if and only if a file is selected in the project pannel.
     */
    public boolean isDiscoveryFileSet() {
        return (sequenceDB != null);
    }

    /**
     * This method is used to fire events from the SequenceDiscoveryViewWidget
     *
     * @param evt property event
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        if (property.equalsIgnoreCase(SequenceDiscoveryViewWidget.PARAMETERS) || property.equalsIgnoreCase(SequenceDiscoveryViewWidget.PATTERN_DB)) {
            DSAncillaryDataSet data = (DSAncillaryDataSet) evt.getNewValue();
            projectNodeEvent(data);
        } else if (property.equalsIgnoreCase(SequenceDiscoveryViewWidget.TABLE_EVENT)) {
            notifyTableEvent(evt);
        } else if (property.equalsIgnoreCase(SequenceDiscoveryViewWidget.TREE_EVENT)) {
            notifyTreeEvent(evt);
        }
    }

    @Publish public org.geworkbench.events.ProjectNodeAddedEvent publishProjectNodeAddedEvent(org.geworkbench.events.ProjectNodeAddedEvent event) {
        return event;
    }

    private void projectNodeEvent(DSAncillaryDataSet set) {
        org.geworkbench.events.ProjectNodeAddedEvent event = new org.geworkbench.events.ProjectNodeAddedEvent("message", null, set);
        publishProjectNodeAddedEvent(event);
    }

    public SequenceDiscoveryViewWidget getSequenceDiscoveryViewWidget() {
        return sDiscoveryViewWidget;
    }

    public void setSequenceDiscoveryViewWidget(SequenceDiscoveryViewWidget s) {
        sDiscoveryViewWidget = s;
    }

    private void notifyTableEvent(PropertyChangeEvent evt) {
        if (!isDiscoveryFileSet()) {
            return;
        }

        JTable table = (JTable) evt.getNewValue();
        /*
             Pattern[] patterns = null;
             if (table != null) {
          PatternTableModel model = (PatternTableModel) (table).getModel();
          int[] rows = table.getSelectedRows();
          patterns = new Pattern[rows.length];
          for (int i = 0; i < rows.length; i++) {
            patterns[i] = model.getPattern(rows[i]);
          }

             } else {
          patterns = new Pattern[0];
             }
         */
        DSCollection<DSMatchedPattern<DSSequence, DSSeqRegistration>> patternMatches = new Collection<DSMatchedPattern<DSSequence, DSSeqRegistration>>();
        if (table != null) {
            org.geworkbench.util.patterns.PatternTableModel model = (org.geworkbench.util.patterns.PatternTableModel) (table).getModel();
            int[] rows = table.getSelectedRows();
            for (int i = 0; i < rows.length; i++) {
                patternMatches.add(model.getPattern(rows[i]));
            }
        }

        //    SequenceDiscoveryTableEvent e = new SequenceDiscoveryTableEvent(this, patterns);
        //    e.setSequenceDB(sequenceDB);
        SequenceDiscoveryTableEvent e = new SequenceDiscoveryTableEvent(patternMatches);
        publishSequenceDiscoveryTableEvent(e);
    } //end notify

    @Publish public SequenceDiscoveryTableEvent publishSequenceDiscoveryTableEvent(SequenceDiscoveryTableEvent event) {
        return event;
    }

    private void notifyTreeEvent(PropertyChangeEvent evt) {
        if (!isDiscoveryFileSet()) {
            return;
        }
        JTree tree = (JTree) evt.getNewValue();
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            ArrayList patternList = new ArrayList();
            for (int i = 0; i < paths.length; i++) {
                Object lastPathComponent = paths[i].getLastPathComponent();
                if (lastPathComponent instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) lastPathComponent;
                    Object userObject = node.getUserObject();

                    if (userObject instanceof PatternNode) {
                        PatternNode nvr = (PatternNode) userObject;
                        if (nvr.getPattern() != null) {
                            patternList.add(nvr.getPattern());
                        }
                    }
                }
            }
            if (patternList.size() > 0) {
                DSCollection<DSMatchedPattern<DSSequence, DSSeqRegistration>> patternMatches = new Collection<DSMatchedPattern<DSSequence, DSSeqRegistration>>();
                patternMatches.addAll(patternList);
                SequenceDiscoveryTableEvent e = new SequenceDiscoveryTableEvent(patternMatches);
                publishSequenceDiscoveryTableEvent(e);
            }
        } //end notify
    }
}
