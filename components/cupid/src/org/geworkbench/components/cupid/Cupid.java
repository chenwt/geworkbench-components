/**
 * 
 */
package org.geworkbench.components.cupid;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.util.ProgressDialog;
import org.geworkbench.util.ProgressItem;
import org.geworkbench.util.ProgressTask;

/**
 * @author zji
 * 
 */
public class Cupid extends JPanel implements VisualPlugin {

	private static final String MI_RNA_ID = "miRNA ID";
	private static final String REF_SEQ_ID = "RefSeq ID";
	private static final long serialVersionUID = 1717902242692528577L;
	private Log log = LogFactory.getLog(Cupid.class);

	private static final String defaultServerUrl = "http://afdev.c2b2.columbia.edu:8080";

	public Cupid() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		JPanel panel1 = new JPanel();
		panel1.add(new JLabel("Server URL"));
		final JTextField urlField = new JTextField(defaultServerUrl, 20);
		panel1.add(urlField);

		JPanel panel2 = new JPanel();
		panel2.add(new JLabel("Query Type"));
		final JComboBox queryTypeComboBox = new JComboBox(new String[] {
				REF_SEQ_ID, MI_RNA_ID });
		panel2.add(queryTypeComboBox);
		panel2.add(new JLabel("Query Value"));
		final JTextField queryValueField = new JTextField(10);
		panel2.add(queryValueField);
		final JButton submitButton = new JButton("Submit");
		submitButton.addActionListener(new ActionListener() {

			final ProgressDialog pd = ProgressDialog.create(ProgressDialog.MODAL_TYPE);
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ProgressTask<Void, Void> task = new ProgressTask<Void, Void>(ProgressItem.INDETERMINATE_TYPE, "Querying CUPID database") {

					
					@Override
					protected Void doInBackground() throws Exception {
						queryRemoteData();
						return null;
					}
					
			    	@Override
					protected void done(){
			    		pd.removeTask(this);
			    	}
				};
				pd.executeTask(task);
			}
			
			private void queryRemoteData() {
				String queryType = encodeSpace( (String) queryTypeComboBox.getSelectedItem() );
				String queryValue = encodeSpace( queryValueField.getText() );
				String host = urlField.getText();

				URL url;
				try {
					url = new URL(host+"/CupidServlet/CupidServlet?type="+queryType+"&value="+queryValue);
					HttpURLConnection connection = (HttpURLConnection) url
							.openConnection();

					int respCode = connection.getResponseCode();
					if (respCode != HttpURLConnection.HTTP_OK) {
						log.error("CupidServlet is not working: " + respCode);
						return;
					}

					BufferedReader in = new BufferedReader(
							new InputStreamReader(connection.getInputStream()));
					
					String line = in.readLine();
					Vector<String[]> data = new Vector<String[]>();
					while(line!=null) {
						String[] fields = line.split("\\|");
						data.add(fields);
						line = in.readLine();
					}

					in.close();
					
					tableModel.setValues(data);
					tableModel.fireTableDataChanged();

				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
					return;
				}

			}

		});
		panel2.add(submitButton);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
		topPanel.add(panel1);
		topPanel.add(panel2);
		topPanel.setMaximumSize(new Dimension(500, 30));

		result = new JTable(tableModel);
		result.setAutoCreateRowSorter(true);
		JScrollPane scrollPane = new JScrollPane(result);

		add(topPanel);
		add(scrollPane);
	}
	
	private JTable result; 
	private CupidTableModel tableModel = new CupidTableModel();

	private static String encodeSpace(String s) {
		return s.replaceAll(" " , "%20");
	}
	
	@Override
	public Component getComponent() {
		return this;
	}

}
