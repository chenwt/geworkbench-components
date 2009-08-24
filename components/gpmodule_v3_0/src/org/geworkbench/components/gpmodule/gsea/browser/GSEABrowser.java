package org.geworkbench.components.gpmodule.gsea.browser;

import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.bison.datastructure.biocollections.gsea.CSGSEAResultDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.events.ProjectEvent;
import org.genepattern.io.ArchiveUtil;
import org.jdesktop.jdic.browser.*;

import javax.swing.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import java.util.Properties;
import java.util.StringTokenizer;
import java.lang.reflect.Method;

/**
 * User: nazaire
 */

@AcceptTypes({CSGSEAResultDataSet.class})
public class GSEABrowser implements VisualPlugin
{
    private String gseaURL;

    private CSGSEAResultDataSet gseaResultDataSet;
    private DSDataSet dataSet;

    private JPanel mainPanel = new JPanel(new BorderLayout());
	private JPanel jp = new JPanel(new BorderLayout());
    private static CloseableTabbedPane jtp = new CloseableTabbedPane();

    private TabBrowser tb;
    private WebBrowser wb = null;
    private Object webBrowser = null;
	private Method setURL = null;
    private String tabtitle = null;

    private String process_id = null;
    private MyStatusBar statusBar = new MyStatusBar();

    private boolean initial = true;
	private boolean link = true;

    private static String osname = System.getProperty("os.name").toLowerCase();
    private final static boolean is_mac = (osname.indexOf("mac") > -1);
    private final static boolean is_windows = (osname.indexOf("windows") > -1);

    private final static boolean useIE = is_windows;

	private static Properties prop = new Properties();
    private static String mozilla_path = null;
    static
    {
		BrowserEngineManager bem = BrowserEngineManager.instance();
		if (is_mac)
			bem.setActiveEngine(BrowserEngineManager.WEBKIT);
		else if (is_windows)
			bem.setActiveEngine(BrowserEngineManager.IE);
		else {
			bem.setActiveEngine(BrowserEngineManager.MOZILLA);
			IBrowserEngine be = bem.getActiveEngine();
			try{
				FileInputStream fis = new FileInputStream("conf/jdic.properties");
				prop.load(fis);
				mozilla_path = prop.getProperty("mozilla.path");
			}catch(Exception e){
				e.printStackTrace();
			}
			be.setEnginePath(mozilla_path);
		}
	}

    protected GSEABrowser(){}

    protected JTabbedPane getTabbedPane()
    {
        return jtp;
    }

    public Component getComponent()
    {
        return mainPanel;
    }

    private void display()
    {
        if (is_windows)
	    {
	        if(tb==null || !tb.isInitialized())
            {
		        try
                {
		            tb = new TabBrowser(new URL(gseaURL
							+ process_id), useIE);
				    tb.addWebBrowserListener(new WebTabListener());
				    tb.setMainBrowser(this);
				    jp.removeAll();
				    jp.add(tb, BorderLayout.CENTER);
				    jtp.addTab(tabtitle, jp);
				    mainPanel.add(jtp, BorderLayout.CENTER);
				    mainPanel.invalidate();
				    mainPanel.repaint();
	            }
                catch (MalformedURLException e)
                {
		            e.printStackTrace();
		            return;
			    }
	        }
        }

        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

		statusBar.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		statusBar.lblDesc.setText("JDIC Browser");
        
        try
        {
            if(initial)
            {
                initial = false;
                if (is_mac)
                {
                    Class wkwbc = Class.forName("org.jdesktop.jdic.browser.WebKitWebBrowser");
			        webBrowser = wkwbc.newInstance();
                    Method setContent = wkwbc.getMethod("setContent", Class.forName("java.lang.String"));
			        setURL = wkwbc.getMethod("setURL", Class.forName("java.net.URL"));
			        setContent.invoke(webBrowser, "MacRoman");
                    setURL.invoke(webBrowser, new URL(gseaURL));
                    jp.add((java.awt.Component)webBrowser, BorderLayout.CENTER);

                    JButton jStopButton = new JButton("Stop",
						new ImageIcon("src/images/Stop.png"));
				    JButton jRefreshButton = new JButton("Refresh",
						new ImageIcon("src/images/Reload.png"));
				    JButton jForwardButton = new JButton("Forward",
						new ImageIcon("src/images/Forward.png"));
				    JButton jBackButton = new JButton("Back",
						new ImageIcon("src/images/Back.png"));
				    jBackButton.setHorizontalTextPosition(SwingConstants.TRAILING);
				    jBackButton.setEnabled(true);
				    jBackButton.setMaximumSize(new Dimension(75, 27));
				    jBackButton.setPreferredSize(new Dimension(75, 27));
				    jBackButton.addActionListener(new ActionListener(){
					    public void actionPerformed(ActionEvent ae) {
						((IWebBrowser)webBrowser).back(); }});
				    jForwardButton.setEnabled(true);
				    jForwardButton.addActionListener(new ActionListener(){
					    public void actionPerformed(ActionEvent ae) {
						((IWebBrowser)webBrowser).forward(); }});
				    jRefreshButton.setEnabled(true);
				    jRefreshButton.setMaximumSize(new Dimension(75, 27));
				    jRefreshButton.setMinimumSize(new Dimension(75, 27));
				    jRefreshButton.setPreferredSize(new Dimension(75, 27));
				    jRefreshButton.addActionListener(new ActionListener(){
					    public void actionPerformed(ActionEvent ae) {
						((IWebBrowser)webBrowser).refresh(); }});
				    jStopButton.setVerifyInputWhenFocusTarget(true);
				    jStopButton.setText("Stop");
				    jStopButton.setEnabled(true);
				    jStopButton.setMaximumSize(new Dimension(75, 27));
				    jStopButton.setMinimumSize(new Dimension(75, 27));
				    jStopButton.setPreferredSize(new Dimension(75, 27));
				    jStopButton.addActionListener(new ActionListener(){
					    public void actionPerformed(ActionEvent ae){
						((IWebBrowser)webBrowser).stop(); }});
				    JToolBar jBrowserToolBar = new JToolBar();
				    jBrowserToolBar.setFloatable(false);
				    jBrowserToolBar.add(jBackButton, null);
				    jBrowserToolBar.add(jForwardButton, null);
				    jBrowserToolBar.addSeparator();
				    jBrowserToolBar.add(jRefreshButton, null);
				    jBrowserToolBar.add(jStopButton, null);
				    jBrowserToolBar.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEtchedBorder(),
					BorderFactory.createEmptyBorder(2, 2, 2, 0)));

                    //jp.add(jBrowserToolBar, BorderLayout.NORTH);
				    jtp.addTab(tabtitle, jp);
                }
		        else if (is_windows)
			    {
					// Print out debug messages in the command line.
					// tb.setDebug(true);
					tb = new org.geworkbench.components.gpmodule.gsea.browser.TabBrowser(new URL(gseaURL), useIE);
					tb.addWebBrowserListener(new WebTabListener());
					tb.setMainBrowser(this);

					jp.removeAll();
					jp.add(tb, BorderLayout.CENTER);
					jtp.addTab(tabtitle, jp);
			    }
			    else
			    {
					// set auto_dispose=false to avoid dead mozilla browser in linux
					wb = new WebBrowser(new URL(gseaURL), useIE);
					jp.removeAll();
					jp.add(wb, BorderLayout.CENTER);
					jtp.addTab(process_id, jp);
			    }
                mainPanel.add(jtp, BorderLayout.CENTER);
			    mainPanel.add(statusBar, BorderLayout.SOUTH); 
            }
            else {
				if (is_mac)
				    setURL.invoke(webBrowser, new URL(gseaURL));
				else if (is_windows)
					tb.setURL(new URL(gseaURL)); // add this no matter what
				else
				{
					wb = new WebBrowser(new URL(gseaURL));
				    jp.removeAll();
				    jp.add(wb, BorderLayout.CENTER);
				    jtp.addTab(process_id, jp);
				    mainPanel.add(jtp, BorderLayout.CENTER);
				    mainPanel.invalidate();
				    mainPanel.repaint();
				}
			}
        }
        catch(Exception e)
        {
		    e.printStackTrace();
        }

        mainPanel.revalidate();
		mainPanel.repaint();
    }

    private void extractResults()
    {        
        ArchiveUtil.unzipRecursive(new File(gseaResultDataSet.getReportFile()), new File(System.getProperty("temporary.files.directory") + "/gsearesults"));

        System.out.println("report file:" + gseaResultDataSet.getReportFile());
        try
        {
            gseaURL = (new File(System.getProperty("temporary.files.directory") + "/gsearesults/"+ "index.html")).toURL().toString();
            System.out.println("GSEA url:" + gseaURL);
        }
        catch(MalformedURLException me)
        {
            me.printStackTrace();
        }
    }

    @Subscribe    
    public void receive(ProjectEvent e, Object source)
    {
        if(e.getDataSet() != null && e.getDataSet() instanceof CSGSEAResultDataSet)
        {
            System.out.println("GSEABrowser received project node added event.");

            gseaResultDataSet = ((CSGSEAResultDataSet)e.getDataSet());
            dataSet = gseaResultDataSet.getParentDataSet();

            extractResults();
            display();
        }

    }
    // web browser listener
	protected class WebTabListener implements WebBrowserListener
    {
		public void downloadStarted(WebBrowserEvent event) {
			updateStatusInfo("Loading started.");
		}

		public void initializationCompleted(WebBrowserEvent event) {
			updateStatusInfo("Initialization completed.");
		}

		public void downloadCompleted(WebBrowserEvent event) {
			updateStatusInfo("Loading completed.");
		}

		public void downloadError(WebBrowserEvent event) {
			updateStatusInfo("Loading error.");
		}

		public void documentCompleted(WebBrowserEvent event) {
			if (link == true)
				return;

			// set url or content in this function to get the page displayed

			try {
				if (tb.getURL() != null) {
					URL currentUrl = tb.getURL();
					String curl = currentUrl.toString();
					System.out.println("\nfrom " + curl + " to " + gseaURL);
					if (!gseaURL.equals(curl)) {
						System.out.println("isinitialized: "
								+ tb.isInitialized() + "; newurl: " + gseaURL);

						tb.setURL(new URL(gseaURL));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			updateStatusInfo("Document loading completed.");
			link = true;
		}

		public void downloadProgress(WebBrowserEvent webEvent) {
			String values = webEvent.getData();
			if (values != null) {
				StringTokenizer tokenizer = new StringTokenizer(values, " ");

				if (tokenizer.hasMoreTokens()) {
					String current = tokenizer.nextToken();
					if (tokenizer.hasMoreTokens()) {
						String max = tokenizer.nextToken();

						int progress = Integer.parseInt(current);
						int progressMax = Integer.parseInt(max);

						onDownloadProgress(webEvent, progress, progressMax);
					}
				}
			}
		}

		// whatever this was meant to do, it was not implemented
		private void onDownloadProgress(WebBrowserEvent webEvent, int progress,
				int progressMax) {
		}

		public void windowClose(WebBrowserEvent event) {
			updateStatusInfo("Closed by script.");
			System.out.println("closed by script." + event.getData());
			if (JOptionPane.YES_OPTION == JOptionPane
					.showConfirmDialog(
							tb,
							"The webpage you are viewing is trying to close the window.\n Do you want to close this window?",
							"Warning", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE)) {
				System.exit(0);
			}
		}

		public void titleChange(WebBrowserEvent event) {
			tabtitle = event.getData();
			jtp.setTitleAt(jtp.getTabCount() - 1, tabtitle);
			updateStatusInfo("Title of the browser window changed.");
		}

		public void statusTextChange(WebBrowserEvent event) {
		}
	}

    void updateStatusInfo(String statusMessage) {
		statusBar.lblStatus.setText(statusMessage);
	}

    static class CustomExceptionHandler implements Thread.UncaughtExceptionHandler
    {
        public void uncaughtException(Thread t, Throwable e)
        {
            e.printStackTrace();
        }
    }
}
