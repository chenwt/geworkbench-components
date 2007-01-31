/*
  The Broad Institute
  SOFTWARE COPYRIGHT NOTICE AGREEMENT
  This software and its documentation are copyright (2003-2007) by the
  Broad Institute/Massachusetts Institute of Technology. All rights are
  reserved.

  This software is supplied without any warranty or guaranteed support
  whatsoever. Neither the Broad Institute nor MIT can be responsible for its
  use, misuse, or functionality.
*/
package org.geworkbench.components.analysis.classification.knn;

import org.geworkbench.algorithms.AbstractTrainingPanel;
import org.geworkbench.bison.algorithm.classification.CSClassifier;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.util.ClassifierException;
import org.geworkbench.builtin.projects.LoadData;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.ColumnSpec;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * @author Marc-Danie Nazaire
 */
public class KNNTrainingPanel extends AbstractTrainingPanel
{
    private static final int DEFAULT_NUM_FEATURES = 10;
    private static final int DEFAULT_NUM_NEIGHBORS = 3;

    private JRadioButton featureFileMethod;
    private String featureFile;
    private javax.swing.JTextField featureFileTextBox;
    private JButton loadFeatureFileButton;
    private JFileChooser featureFileChooser = new JFileChooser();
    private JRadioButton numFeatureMethod;
    private JFormattedTextField numFeatures;
    private JComboBox statistic;
    private JFormattedTextField minStdDev;
    private JCheckBox medianCheckbox;
    private JCheckBox minStdDevCheckbox;
    private JFormattedTextField numNeighbors;
    private JComboBox weightType;
    private JComboBox distanceMeasure;
    private KNNTraining knnTraining;

    public KNNTrainingPanel(KNNTraining knnTraining)
    {
        this.knnTraining = knnTraining;
        try
        {   jbInit();   }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void initUI()
    {
        numFeatureMethod = new JRadioButton();
        numFeatureMethod.setText("num features");
        numFeatureMethod.setSelected(true);
        numFeatureMethod.addItemListener( new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == 1)
                {
                    numFeatures.setEnabled(true);
                    statistic.setEnabled(true);
                    medianCheckbox.setEnabled(true);
                    minStdDevCheckbox.setEnabled(true);
                }
                else
                {
                    numFeatures.setEnabled(false);
                    statistic.setEnabled(false);
                    medianCheckbox.setEnabled(false);
                    minStdDevCheckbox.setEnabled(false);
                }
            }
        });

        numFeatures = new JFormattedTextField();
        numFeatures.setValue(DEFAULT_NUM_FEATURES);

        featureFileMethod = new JRadioButton();
        featureFileMethod.setText("feature filename");
        featureFileMethod.addItemListener( new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == 1)
                {
                    featureFileTextBox.setEnabled(true);
                    loadFeatureFileButton.setEnabled(true);
                }
                else
                {
                    featureFileTextBox.setEnabled(false);
                    loadFeatureFileButton.setEnabled(false);
                }
            }
        });

        featureFileTextBox = new JTextField();
        featureFileTextBox.setEnabled(false);
        loadFeatureFileButton = new JButton("Load");
        loadFeatureFileButton.setEnabled(false);
        loadFeatureFileButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
               featureFileLoadHandler();
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(numFeatureMethod);
        group.add(featureFileMethod);

        statistic = new JComboBox();
        statistic.addItem("SNR");
        statistic.addItem("T-Test");

        medianCheckbox = new JCheckBox();
        medianCheckbox.setText("median") ;

        minStdDevCheckbox = new JCheckBox();
        minStdDevCheckbox.setText("min std dev");
        minStdDevCheckbox.addItemListener( new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == 1)
                {   minStdDev.setEnabled(true);     }
                else
                {   minStdDev.setEnabled(false);    }
            }
        });

        minStdDev = new JFormattedTextField("");
        minStdDev.setEnabled(false);

        numNeighbors = new JFormattedTextField();
        numNeighbors.setValue(DEFAULT_NUM_NEIGHBORS);

        weightType = new JComboBox();
        weightType.addItem("none");
        weightType.addItem("one-over-k");
        weightType.addItem("distance");

        distanceMeasure = new JComboBox();
        distanceMeasure.addItem("Cosine");
        distanceMeasure.addItem("Euclidean");
    }

    public int getNumFeatures()
    {
        return ((Integer)numFeatures.getValue()).intValue();
    }

    public String getFeatureFile()
    {
        return this.featureFile;
    }

    public String getStatistic()
    {
        return (String)statistic.getSelectedItem();
    }

    public String getMinStdDev()
    {
        return  (String)minStdDev.getValue();
    }

    public boolean useMinStdDev()
    {
        return minStdDevCheckbox.isSelected();
    }

    public boolean useMedian()
    {
        return medianCheckbox.isSelected();
    }

    public boolean useFeatureFileMethod()
    {
        return(featureFileMethod.isSelected());
    }

    public String getWeightType()
    {
        return (String)weightType.getSelectedItem();
    }

    public String getDistanceMeasure()
    {
        return (String)distanceMeasure.getSelectedItem();
    }

    public int getNumNeighbors()
    {
        return ((Integer)numNeighbors.getValue()).intValue();
    }

    private JLabel getGPLogo()
    {
        java.net.URL imageURL = KNNTrainingPanel.class.getResource("images/gp-logo.jpg");
        ImageIcon image = new ImageIcon(imageURL);

        JLabel label = new JLabel();
        label.setIcon(image);

        return label;
    }

    protected void addParameters(DefaultFormBuilder builder)
    {
        builder.appendSeparator("K-Nearest Neighbor Parameters");
        builder.nextRow();

        builder.appendColumn(new ColumnSpec("9dlu"));
        builder.append(numFeatureMethod, numFeatures);

        // add the GenePattern logo
        builder.setColumn(7);
        builder.add(getGPLogo());
        builder.nextRow();

        builder.append("feature selection statistic", statistic);
        builder.nextRow();

        builder.append(minStdDevCheckbox, minStdDev, medianCheckbox);
        builder.nextRow();

        builder.append(featureFileMethod, featureFileTextBox, loadFeatureFileButton);
        builder.nextRow();

        builder.append("num neighbors", numNeighbors);
        builder.nextRow();

        builder.append("neighbor weight type", weightType);
        builder.append("distance measure", distanceMeasure);
    }

    protected CSClassifier trainForValidation(java.util.List<float[]> trainingCaseData, java.util.List<float[]> trainingControlData) throws ClassifierException
    {
        ParamValidationResults validationResults = validateParameters();
        if(!validationResults.isValid())
            throw new ClassifierException(validationResults.getMessage());

        setTrainingTask(knnTraining);

        return knnTraining.trainClassifier(trainingCaseData, trainingControlData);
    }

    public ParamValidationResults validateParameters()
    {
        if(!useFeatureFileMethod() && getNumFeatures() <= 0)
            return new ParamValidationResults(false, "num features must be greater than 0");
        else if(!useFeatureFileMethod() && getNumFeatures() > getActiveMarkers().size())
            return new ParamValidationResults(false, "num features cannot be greater than \nnumber of activated markers: "
                    + getActiveMarkers().size());
        else if(useMinStdDev() && getMinStdDev() == null)
            return new ParamValidationResults(false, "min std dev not provided");
        else if(useMinStdDev() && Double.parseDouble(getMinStdDev()) <= 0)
            return new ParamValidationResults(false, "min std dev must be greater than 0");
        else if(getNumNeighbors() <= 0)
            return new ParamValidationResults(false, "num neighbors must be greater than 0");
        else
            return new ParamValidationResults(true, "KNN Parameter validations passed");
    }

    private void featureFileLoadHandler()
    {
        String lwd = LoadData.getLastDataDirectory();
        featureFileChooser.setCurrentDirectory(new File(lwd));
        featureFileChooser.showOpenDialog(this);
        File file = featureFileChooser.getSelectedFile();
        if (file != null) {
            featureFile = featureFileChooser.getSelectedFile().getAbsolutePath();
            featureFileTextBox.setText(featureFile);
        }
    }

    Object writeReplace() throws ObjectStreamException {
        return new SerializedInstance(numFeatureMethod.isSelected(), getNumFeatures(),
                getStatistic(), useMedian(), useMinStdDev(), getMinStdDev(),
                useFeatureFileMethod(), featureFile, getNumNeighbors(), getWeightType(), getDistanceMeasure());
    }

    private static class SerializedInstance implements Serializable
    {
        private boolean numFeatureMethod;
        private int numFeatures;
        private String statistic;
        private boolean useMedian;
        private boolean useMinStdDev;
        private String minStdDev;
        private boolean featureFileMethod;
        private String featureFile;
        private Integer numNeighbors;
        private String weightType;
        private String distanceMeasure;

        public SerializedInstance(Boolean numFeatureMethod, Integer numFeatures, String statistic, Boolean useMedian,
                                  Boolean useMinStdDev, String minStdDev, Boolean featureFileMethod, String featureFile,
                                  Integer numNeighbors, String weightType, String distanceMeasure)
        {
            this.numFeatureMethod = numFeatureMethod;
            this.numFeatures = numFeatures;
            this.statistic = statistic;
            this.useMedian = useMedian;
            this.minStdDev = minStdDev;
            this.useMinStdDev = useMinStdDev;
            this.featureFileMethod = featureFileMethod;
            this.featureFile = featureFile;
            this.numNeighbors = numNeighbors;
            this.weightType = weightType;
            this.distanceMeasure = distanceMeasure;
        }

        Object readResolve() throws ObjectStreamException
        {
            KNNTrainingPanel panel = new KNNTrainingPanel(null);
            panel.numFeatureMethod.setSelected(numFeatureMethod);
            panel.numFeatures.setValue(numFeatures);
            panel.statistic.setSelectedItem(statistic);
            panel.medianCheckbox.setSelected(useMedian);
            panel.minStdDevCheckbox.setSelected(useMinStdDev);
            panel.minStdDev.setValue(minStdDev);
            panel.featureFileMethod.setSelected(featureFileMethod);
            panel.featureFileTextBox.setText(featureFile);
            panel.numNeighbors.setValue(numNeighbors);
            panel.weightType.setSelectedItem(weightType);
            panel.distanceMeasure.setSelectedItem(distanceMeasure);

            return panel;
        }
    }
}
