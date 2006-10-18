/*
 * Copyright 2006 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * MZmine; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.methods.filtering.crop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.mzmine.data.AlignmentResult;
import net.sf.mzmine.io.OpenedRawDataFile;
import net.sf.mzmine.io.RawDataFile;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.methods.Method;
import net.sf.mzmine.methods.MethodParameters;
import net.sf.mzmine.project.MZmineProject;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.taskcontrol.TaskController;
import net.sf.mzmine.taskcontrol.TaskListener;
import net.sf.mzmine.userinterface.Desktop;
import net.sf.mzmine.userinterface.Desktop.MZmineMenu;
import net.sf.mzmine.userinterface.dialogs.ParameterSetupDialog;

public class CropFilter implements Method, TaskListener,
        ListSelectionListener, ActionListener {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private TaskController taskController;
    private Desktop desktop;
    private JMenuItem myMenuItem;

    /**
     * @see net.sf.mzmine.main.MZmineModule#initModule(net.sf.mzmine.main.MZmineCore)
     */
    public void initModule(MZmineCore core) {

        this.taskController = core.getTaskController();
        this.desktop = core.getDesktop();

        myMenuItem = desktop.addMenuItem(MZmineMenu.FILTERING, "Crop filter",
                this, null, KeyEvent.VK_C, false, false);

        desktop.addSelectionListener(this);

    }

    /**
     * @see net.sf.mzmine.methods.Method#askParameters()
     */
    public MethodParameters askParameters() {

        MZmineProject currentProject = MZmineProject.getCurrentProject();
        CropFilterParameters currentParameters = (CropFilterParameters) currentProject.getParameters(this);
        if (currentParameters == null)
            currentParameters = new CropFilterParameters();

        // Show parameter setup dialog
        double[] paramValues = new double[4];
        paramValues[0] = currentParameters.minMZ;
        paramValues[1] = currentParameters.maxMZ;
        paramValues[2] = currentParameters.minRT;
        paramValues[3] = currentParameters.maxRT;

        String[] paramNames = new String[4];
        paramNames[0] = "Minimum M/Z (Da)";
        paramNames[1] = "Maximum M/Z (Da)";
        paramNames[2] = "Minimum RT (seconds)";
        paramNames[3] = "Maximum RT (seconds)";

        NumberFormat[] numberFormats = new NumberFormat[4];
        numberFormats[0] = NumberFormat.getNumberInstance();
        numberFormats[0].setMinimumFractionDigits(3);
        numberFormats[1] = NumberFormat.getNumberInstance();
        numberFormats[1].setMinimumFractionDigits(3);
        numberFormats[2] = NumberFormat.getNumberInstance();
        numberFormats[2].setMinimumFractionDigits(1);
        numberFormats[3] = NumberFormat.getNumberInstance();
        numberFormats[3].setMinimumFractionDigits(1);

        logger.finest("Showing cropping filter parameter setup dialog");

        ParameterSetupDialog psd = new ParameterSetupDialog(
                desktop.getMainFrame(), "Please check the parameter values",
                paramNames, paramValues, numberFormats);
        psd.setVisible(true);

        // Check if user clicked Cancel-button
        if (psd.getExitCode() == -1)
            return null;

        CropFilterParameters newParameters = new CropFilterParameters();

        // Read parameter values
        double d;

        // minMZ
        d = psd.getFieldValue(0);
        if (d < 0) {
            desktop.displayErrorMessage("Incorrect minimum M/Z value!");
            return null;
        }
        newParameters.minMZ = d;

        // maxMZ
        d = psd.getFieldValue(1);
        if (d <= 0) {
            desktop.displayErrorMessage("Incorrect maximum M/Z value!");
            return null;
        }
        newParameters.maxMZ = d;

        // minRT
        d = psd.getFieldValue(2);
        if (d < 0) {
            desktop.displayErrorMessage("Incorrect minimum RT value!");
            return null;
        }
        newParameters.minRT = d;

        // maxRT
        d = psd.getFieldValue(3);
        if (d <= 0) {
            desktop.displayErrorMessage("Incorrect maximum RT value!");
            return null;
        }
        newParameters.maxRT = d;

        // save the current parameter settings for future runs
        currentProject.setParameters(this, newParameters);

        return newParameters;

    }

    /**
     * @see net.sf.mzmine.methods.Method#runMethod(net.sf.mzmine.methods.MethodParameters,
     *      net.sf.mzmine.io.RawDataFile[],
     *      net.sf.mzmine.methods.alignment.AlignmentResult[])
     */
    public void runMethod(MethodParameters parameters,
            OpenedRawDataFile[] dataFiles, AlignmentResult[] alignmentResults) {

        logger.info("Running cropping filter");

        for (OpenedRawDataFile dataFile : dataFiles) {
            Task filterTask = new CropFilterTask(dataFile,
                    (CropFilterParameters) parameters);
            taskController.addTask(filterTask, this);
        }

    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {

        MethodParameters parameters = askParameters();
        if (parameters == null)
            return;

        OpenedRawDataFile[] dataFiles = desktop.getSelectedDataFiles();

        runMethod(parameters, dataFiles, null);

    }

    /**
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e) {
        myMenuItem.setEnabled(desktop.isDataFileSelected());
    }

    public void taskStarted(Task task) {
        // do nothing
    }

    public void taskFinished(Task task) {

        if (task.getStatus() == Task.TaskStatus.FINISHED) {

            Object[] result = (Object[]) task.getResult();
            OpenedRawDataFile openedFile = (OpenedRawDataFile) result[0];
            RawDataFile newFile = (RawDataFile) result[1];
            MethodParameters cfParam = (MethodParameters) result[2];

            openedFile.updateFile(newFile, this, cfParam);

        } else if (task.getStatus() == Task.TaskStatus.ERROR) {
            /* Task encountered an error */
            String msg = "Error while filtering a file: "
                    + task.getErrorMessage();
            logger.severe(msg);
            desktop.displayErrorMessage(msg);
        }

    }

    /**
     * @see net.sf.mzmine.methods.Method#toString()
     */
    public String toString() {
        return "Cropping filter";
    }

}