package uk.ac.ebi.pride.gui.form;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.App;
import uk.ac.ebi.pride.gui.form.comp.HeaderPanel;
import uk.ac.ebi.pride.gui.navigation.Navigator;
import uk.ac.ebi.pride.gui.util.BorderUtil;
import uk.ac.ebi.pride.toolsuite.gui.GUIUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EncryptionForm extends Form implements ActionListener {
    private static final Logger logger = LoggerFactory.getLogger(SubmissionForm.class);

    /**
     * Action commands & properties
     */
    public static final String START_ENCRYPTION_PROP = "startAction";
    public static final String STOP_ENCRYPTION_PROP = "stopAction";

    private JLabel encryptingFileLabel;
    private JLabel progressLabel;
    private JProgressBar progressBar;
    private JButton controlButton;
    private JPanel progPanel;


    public EncryptionForm() {
        initComponents();
    }

    private void initComponents() {
        // setup the main pane
        this.setLayout(new BorderLayout());

        // add gap
        this.add(Box.createRigidArea(new Dimension(20, 150)), BorderLayout.NORTH);

        // create progress panel
        this.progPanel = createProgressPanel();
        this.add(this.progPanel, BorderLayout.CENTER);
    }

    /**
     * Create the progress bar panel
     *
     * @return JPanel  panel to show the progress of the upload
     */
    private JPanel createProgressPanel() {
        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // uploading icon
        Icon icon = GUIUtilities.loadIcon(App.getInstance().getDesktopContext().getProperty("encryption.large.icon"));
        JLabel iconLabel = new JLabel(icon);
        progressPanel.add(iconLabel);

        // progress detail panel
        JPanel progressDetailPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;

        // label to indicate which file is uploading
        c.gridx = 0;
        c.gridy = 0;

        encryptingFileLabel = new JLabel();
        encryptingFileLabel.setFont(encryptingFileLabel.getFont().deriveFont(Font.BOLD).deriveFont(13f));
        progressDetailPanel.add(encryptingFileLabel, c);

        // progress bar
        c.gridx = 0;
        c.gridy = 1;

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(620, 25));
        progressBar.setValue(0);
        progressDetailPanel.add(progressBar, c);

        // control button
        c.gridx = 1;
        c.gridy = 1;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        Icon cancelIcon = GUIUtilities.loadIcon(App.getInstance().getDesktopContext().getProperty("pause.small.icon"));
        controlButton = GUIUtilities.createLabelLikeButton(cancelIcon, null);
        controlButton.setActionCommand(STOP_ENCRYPTION_PROP);
        controlButton.addActionListener(this);
        buttonPanel.add(controlButton);
        progressDetailPanel.add(buttonPanel, c);

        // label to indicate the upload progress
        c.gridx = 0;
        c.gridy = 2;
        progressLabel = new JLabel();
        progressDetailPanel.add(progressLabel, c);

        progressPanel.add(progressDetailPanel);

        return progressPanel;
    }

    /**
     * Set progress message
     *
     * @param message progress message
     */
    public void setProgressMessage(String message) {
        progressLabel.setText(message);
    }


    public void setProgress(int encryptedFiles, int totalNumOfFiles) {
        int percentage = Math.round((encryptedFiles * 1.0f / totalNumOfFiles) * 100);
        // set progress bar
        progressBar.setValue(percentage);

        // set progress label
        String progressLabelText = percentage + "% completed of " + totalNumOfFiles + " files";
        progressLabel.setText(progressLabelText);

        if(percentage == 100){
            Navigator navigator = ((App) App.getInstance()).getNavigator();
            JButton nextButton = navigator.getNextButton();
            nextButton.setEnabled(true);
        }
    }

    public void enableCancelButton(boolean enabled) {
        Icon cancelIcon = GUIUtilities.loadIcon(App.getInstance().getDesktopContext().getProperty("pause.small.icon"));
        controlButton.setIcon(cancelIcon);
        controlButton.setEnabled(enabled);
        controlButton.setActionCommand(STOP_ENCRYPTION_PROP);
    }

    public void enableStartButton(boolean enabled) {
        Icon startIcon = GUIUtilities.loadIcon(App.getInstance().getDesktopContext().getProperty("start.small.icon"));
        controlButton.setIcon(startIcon);
        controlButton.setEnabled(enabled);
        controlButton.setActionCommand(START_ENCRYPTION_PROP);
    }

    public void enabledSuccessButton(boolean enabled) {
        Icon successIcon = GUIUtilities.loadIcon(App.getInstance().getDesktopContext().getProperty("success.small.icon"));
        controlButton.setIcon(successIcon);
        controlButton.setEnabled(enabled);
        controlButton.setActionCommand(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String evtCmd = e.getActionCommand();
        if (evtCmd.equals(START_ENCRYPTION_PROP)) {
            logger.debug("Restart button pressed");
            firePropertyChange(START_ENCRYPTION_PROP, false, true);
        } else if (evtCmd.equals(STOP_ENCRYPTION_PROP)) {
            logger.debug("Cancel button pressed");
            firePropertyChange(STOP_ENCRYPTION_PROP, false, true);
        }
    }

    /**
     * Show submission complete message
     *
     * @param submissionRef submission reference id
     */
    public void showCompletionMessage(String submissionRef) {
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(BorderUtil.createLoweredBorder());
        messagePanel.setPreferredSize(new Dimension(500, 200));

        JPanel contentPanel = new HeaderPanel();
        contentPanel.setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel();
        Icon icon = GUIUtilities.loadIcon(App.getInstance().getDesktopContext().getProperty("encryption.complete.large.icon"));
        titleLabel.setIcon(icon);
        titleLabel.setForeground(Color.white);
        titleLabel.setFont(titleLabel.getFont().deriveFont(18f).deriveFont(Font.BOLD));
        titleLabel.setText(App.getInstance().getDesktopContext().getProperty("encryption.complete.title"));

        JLabel refLabel = new JLabel(submissionRef);
        refLabel.setForeground(Color.green);
        refLabel.setFont(refLabel.getFont().deriveFont(18f).deriveFont(Font.BOLD));

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(5, 5)));
        titlePanel.add(refLabel);

        JLabel messageLabel = new JLabel();
        messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 0));
        messageLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        messageLabel.setVerticalTextPosition(SwingConstants.TOP);
        messageLabel.setForeground(Color.white);
        messageLabel.setFont(messageLabel.getFont().deriveFont(16f));
        messageLabel.setText(App.getInstance().getDesktopContext().getProperty("encryption.complete.message"));

        contentPanel.add(titlePanel, BorderLayout.NORTH);
        contentPanel.add(messageLabel, BorderLayout.CENTER);


        messagePanel.add(contentPanel, BorderLayout.CENTER);
        this.add(messagePanel, BorderLayout.SOUTH);
        this.revalidate();
        this.repaint();
    }
}
