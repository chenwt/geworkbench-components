package org.geworkbench.components.viewers;

import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.builtin.projects.ImageData;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 *
 * @author First Genetic Trust Inc.
 * @version 1.0
 */
@AcceptTypes({ImageData.class}) public class ImageViewer extends JPanel implements VisualPlugin {
    /**
     * Canvas on which Image is painted
     */
    ImageDisplay display = new ImageDisplay();
    /**
     * Visual Widgets
     */
    BorderLayout borderLayout1 = new BorderLayout();
    JScrollPane jScrollPane1 = new JScrollPane();

    public ImageViewer() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Interface <code>VisualPlugin</code> method
     *
     * @return visual representation of this component
     */
    public Component getComponent() {
        return this;
    }

    @Subscribe public void receive(ImageSnapshotEvent event, Object source) {
        if (event.getAction() == org.geworkbench.events.ImageSnapshotEvent.Action.SHOW) {
            ImageIcon image = event.getImage();
            display.setImage(image);
            if (image != null) {
                display.setSize(new Dimension(image.getIconWidth(), image.getIconHeight()));
                display.setPreferredSize(new Dimension(image.getIconWidth(), image.getIconHeight()));
            }

            if (this.getParent() instanceof JTabbedPane) {
                ((JTabbedPane) this.getParent()).setSelectedComponent(this);
            }

            repaint();
        } else {
            // no-op
        }
    }

    private void jbInit() throws Exception {
        this.setLayout(borderLayout1);
        jScrollPane1.getViewport().add(display, null);
        this.add(jScrollPane1, BorderLayout.CENTER);
    }

}

