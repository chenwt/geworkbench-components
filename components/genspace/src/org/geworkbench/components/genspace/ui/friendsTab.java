package org.geworkbench.components.genspace.ui;

import javax.ejb.EJBException;
import javax.swing.*;

import org.geworkbench.components.genspace.GenSpace;
import org.geworkbench.components.genspace.GenSpaceServerFactory;
import org.geworkbench.components.genspace.RuntimeEnvironmentSettings;
import org.geworkbench.components.genspace.entity.Network;
import org.geworkbench.components.genspace.entity.User;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.LogManager;

/**
 * Created by IntelliJ IDEA. User: jon Date: Aug 28, 2010 Time: 12:20:54 PM To
 * change this template use File | Settings | File Templates.
 */
public class friendsTab extends SocialTab {
//	private JList pendingRequestsList;
//	private JButton acceptButton;
//	private JButton rejectButton;
	private JList myFriendsList;
	private Network networkFilter = null;

	public friendsTab() {

	}

	public friendsTab(Network filteredNetwork, SocialNetworksHome parentFrame) {
		networkFilter = filteredNetwork;
		this.parentFrame = parentFrame;
	}

	public void initComponents() {
		myFriendsList.setOpaque(false);
		myFriendsList.setCellRenderer(new ListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JPanel pan = new JPanel(); 
				
				User u = (User) value;
				pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));

				JLabel label = new JLabel(u.getFullNameWUsername());
				Font f = new Font(label.getFont().getName(), Font.BOLD, 18);
				label.setFont(f);
				label.setForeground(new Color(-16777012));

				pan.add(label);
				String byline = "";
				if (u.getWorkTitle() != null && !u.getWorkTitle().equals("")) {
					byline += u.getWorkTitle() + " ";
					if (u.getLabAffiliation() != null && !u.getLabAffiliation().equals(""))
						byline += "at ";
				}
				if (u.getLabAffiliation() != null && !u.getLabAffiliation().equals(""))
					byline += u.getLabAffiliation() + " ";

				if (u.getCity() != null && !u.getCity().equals("")) {
					byline += u.getCity();
					if (u.getState() != null && !u.getState().equals(""))
						byline += ", ";
					else
						byline += " ";

				}
				if (u.getState() != null && !u.getState().equals(""))
					byline += u.getState();
				JLabel label2 = new JLabel(byline);
				if (GenSpaceServerFactory.isVisible(u))
					pan.add(label2);
				pan.add(new JSeparator(SwingConstants.HORIZONTAL));
				if (isSelected)
					pan.setBackground(new Color(251, 251, 228));
				return pan;
			}
		});
		myFriendsList.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					viewProfileTab viewProfileTab = new viewProfileTab(
							(User) myFriendsList.getSelectedValue());
					parentFrame.setContent(viewProfileTab);
				}
			}
		});
	}

	{
		// GUI initializer generated by IntelliJ IDEA GUI Designer
		// >>> IMPORTANT!! <<<
		// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
		initComponents();
	}

	@Override
	public synchronized void updateFormFields() {
		if (GenSpaceServerFactory.isLoggedIn()) {
			SwingWorker<List<User>, Void> worker = new SwingWorker<List<User>, Void>() {
				int evt;
				@Override
				protected List<User> doInBackground() {

					evt = GenSpace.getStatusBar().start("Loading profiles");
					try{
						if (networkFilter == null)
							return (List<User>) RuntimeEnvironmentSettings.readObject(GenSpaceServerFactory.getFriendOps().getFriendsBytes());
						else
							return GenSpaceServerFactory.getNetworkOps().getProfilesByNetwork(networkFilter.getId());
					}
					catch(EJBException e)
					{
						GenSpace.getStatusBar().stop(evt);
						return null;
//						throw e;
					}
				}

				@Override
				protected void done() {
					GenSpace.logger.info("Done retrieving profiles, calling get");
					GenSpace.getStatusBar().stop(evt);
					List<User> lst = null;
					try {
						lst = get();
					} catch (InterruptedException e) {
						GenSpace.logger.warn("Error",e);
					} catch (ExecutionException e) {
						GenSpaceServerFactory.clearCache();
						updateFormFields();
						return;
					}
					GenSpace.logger.info("Done retrieving profiles, also called get!");
					if(lst == null)
						return;
					lst.remove(GenSpaceServerFactory.getUser());
					Collections.sort(lst,new Comparator<User>() {

						@Override
						public int compare(User o1, User o2) {
							return o1.compareTo(o2);
						}
					});
					DefaultListModel model = new DefaultListModel();
					if(lst != null)
						for (User t : lst) {
							model.addElement(t);
						}
					myFriendsList.setModel(model);
					GenSpace.logger.info("All done with refresh");
				}

			};
			worker.execute();
		}
	}
	/**
	 * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT
	 * edit this method OR call it in your code!
	 * 
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		panel1 = new JPanel();
		panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3,
				1, new Insets(0, 0, 0, 0), -1, -1));
		final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
		panel1.add(
				spacer1,
				new com.intellij.uiDesigner.core.GridConstraints(1, 0, 2, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
		myFriendsList = new JList();
		myFriendsList.setBackground(panel1.getBackground());
		panel1.add(
				myFriendsList,
				new com.intellij.uiDesigner.core.GridConstraints(1, 0, 2, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
		
	}

	@Override
	public String getName() {
		if (networkFilter != null)
			return "Users in network " + networkFilter.getName();
		else
			return "My Friends";
	}
}