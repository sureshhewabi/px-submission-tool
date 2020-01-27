package uk.ac.ebi.pride;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.toolsuite.gui.desktop.Desktop;
import uk.ac.ebi.pride.toolsuite.gui.desktop.DesktopContext;
import uk.ac.ebi.pride.gui.form.*;
import uk.ac.ebi.pride.gui.navigation.NavigationException;
import uk.ac.ebi.pride.gui.navigation.NavigationPanelDescriptor;
import uk.ac.ebi.pride.gui.navigation.Navigator;
import uk.ac.ebi.pride.gui.util.Constant;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.Observable;

/**
 * This is the entry point of the entire application
 * <p/>
 * This class is responsible to start the application and show GUI components
 */
public class App extends Desktop {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static final String OS = System.getProperty("os.name").toLowerCase();

    /**
     * Main frame of the application
     */
    private JFrame mainFrame;

    /**
     * Navigator panel
     */
    private Navigator navigator;

    private boolean okToClose;

    // Window Listener for performing possible pending operations before closing the application
    private class AppCloseWindowListener extends Observable implements WindowListener {
        @Override
        public void windowOpened(WindowEvent e) {
            //Empty
        }

        @Override
        public void windowClosing(WindowEvent e) {
            logger.debug("Notifying possible observers that there is a 'Window Closing' action taking place");
            unsetDoNotCloseAppFlag();
            setChanged();
            notifyObservers();
            if (isOkToClose()) {
                mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            } else {
                mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            }
            //mainFrame.setVisible(false);
            //mainFrame.dispose();
        }

        @Override
        public void windowClosed(WindowEvent e) {
        }

        @Override
        public void windowIconified(WindowEvent e) {
            //Empty
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
            //Empty
        }

        @Override
        public void windowActivated(WindowEvent e) {
            //Empty
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
            //Empty
        }
    }

    // Window Listener
    private AppCloseWindowListener appCloseWindowListener = new AppCloseWindowListener();

    public static void main(String[] args) {
        if (!ApplicationInstanceManager.registerInstance()) {
            // instance already running.
            System.out.println("Another instance of this application is already running.  Exiting.");
            System.exit(0);
        }
        ApplicationInstanceManager.setApplicationInstanceListener(new ApplicationInstanceManager.ApplicationInstanceListener() {
            public void newInstanceCreated() {
                System.out.println("New instance detected...");
            }
        });
        Desktop.launch(App.class, AppContext.class, args);
    }

    @Override
    public void init(String[] args) {
        // load properties
        loadProps();

        // init key controls
        initKeyControls();

        // init user space
        initUserSpace();

        // build main frame
        buildMainFrame();

        // build navigation
        buildNavigation();
    }

    /**
     * Load property settings before showing the GUI
     */
    private void loadProps() {
        DesktopContext context = getDesktopContext();
        try {
            context.loadSystemProps(this.getClass().getClassLoader().getResourceAsStream("prop/gui.prop"));
            context.loadSystemProps(this.getClass().getClassLoader().getResourceAsStream("prop/setting.prop"));
        } catch (IOException e) {
            logger.error("Error while loading properties", e);
        }
    }

    /**
     * Initialize key controls for mac platform
     */
    private void initKeyControls() {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Mac OS")) {
            InputMap textFieldInputMap = (InputMap) UIManager.get("TextField.focusInputMap");
            textFieldInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), DefaultEditorKit.copyAction);
            textFieldInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), DefaultEditorKit.pasteAction);
            textFieldInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), DefaultEditorKit.cutAction);

            InputMap textAreaInputMap = (InputMap) UIManager.get("TextArea.focusInputMap");
            textAreaInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), DefaultEditorKit.copyAction);
            textAreaInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), DefaultEditorKit.pasteAction);
            textAreaInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), DefaultEditorKit.cutAction);

            InputMap passwordFieldInputMap = (InputMap) UIManager.get("PasswordField.focusInputMap");
            passwordFieldInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), DefaultEditorKit.copyAction);
            passwordFieldInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), DefaultEditorKit.pasteAction);
            passwordFieldInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), DefaultEditorKit.cutAction);
        }
    }

    /**
     * Initialize a space for saving user related details
     * such as: submission progress report
     */
    private void initUserSpace() {
        File userSpace = new File(System.getProperty("user.home") + File.separator + Constant.PX_TOOL_USER_DIRECTORY);
        if (!userSpace.exists()) {
            userSpace.mkdir();
        }
    }

    /**
     * Check whether it is Windows platform
     *
     * @return boolean  true means it is running on windows
     */
    private boolean isWindowsPlatform() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isMac() {

        return (OS.indexOf("mac") >= 0);

    }

    public static boolean isUnix() {

        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );

    }

    public static boolean isSolaris() {

        return (OS.indexOf("sunos") >= 0);

    }

    /**
     * Build the main display frame and set the look and feel
     */
    private void buildMainFrame() {
        mainFrame = new JFrame();
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(appCloseWindowListener);

        // set look and feel
        String lookAndFeel = "Nimbus"; // Default
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (isWindowsPlatform() && (info.getName().contains("Windows"))) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
                if (isMac() && (info.getName().contains("Mac OS"))) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
            // No look and feel found, set default
            UIManager.setLookAndFeel(lookAndFeel);
            // Workaround for Nimbus bug
            LookAndFeel laf = UIManager.getLookAndFeel();
            UIDefaults defaults = laf.getDefaults();
            defaults.put("ScrollBar.minimumThumbSize", new Dimension(30, 30));
        } catch (Exception e){
            logger.error("Failed to load a Look'n'Feel for the application " + e.toString());
        }
        /*if (!isWindowsPlatform()) {
            lookAndFeel = "Mac OS X";
        } else {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if (lookAndFeel.equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        LookAndFeel laf = UIManager.getLookAndFeel();
                        UIDefaults defaults = laf.getDefaults();
                        defaults.put("ScrollBar.minimumThumbSize", new Dimension(30, 30));
                        break;
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to load nimbus look and feel for the application", e);
            }
        }*/
    }

    /**
     * Build the main navigation panel
     */
    private void buildNavigation() {
        navigator = new Navigator();

        try {
            // login form
            PrideLoginDescriptor prideLoginPanel = new PrideLoginDescriptor(getDesktopContext().getProperty("pride.login.nav.desc.id"),
                    getDesktopContext().getProperty("pride.login.nav.desc.title") ,
                    getDesktopContext().getProperty("pride.login.nav.desc.detail") +
                            " (version " + getDesktopContext().getProperty("px.submission.tool.version") + ")");

            navigator.registerNavigationPanel(prideLoginPanel);

            // register submission type form
            SubmissionTypeDescriptor submissionTypeDescriptor = new SubmissionTypeDescriptor(getDesktopContext().getProperty("submission.type.nav.desc.id"),
                    "Step 1: " + getDesktopContext().getProperty("submission.type.nav.desc.title")+ " (1/10)",
                    getDesktopContext().getProperty("submission.type.nav.desc.detail") );
            navigator.registerNavigationPanel(submissionTypeDescriptor);

            // register prerequisite form
            PrerequisiteDescriptor prerequisiteDescriptor = new PrerequisiteDescriptor(getDesktopContext().getProperty("prerequisite.nav.desc.id"),
                    getDesktopContext().getProperty("prerequisite.nav.desc.title"),
                    getDesktopContext().getProperty("prerequisite.nav.desc.detail"));
            navigator.registerNavigationPanel(prerequisiteDescriptor);

            // register project metadata form
            ProjectMetaDataDescriptor metaDataPanel = new ProjectMetaDataDescriptor(getDesktopContext().getProperty("project.metadata.nav.desc.id"),
                    "Step 2: " + getDesktopContext().getProperty("project.metadata.nav.desc.title") + " (2/10)",
                    getDesktopContext().getProperty("project.metadata.nav.desc.detail"));
            navigator.registerNavigationPanel(metaDataPanel);

            // register file selection form
            FileSelectionDescriptor fileSelectionPanel = new FileSelectionDescriptor(getDesktopContext().getProperty("file.selection.nav.desc.id"),
                    "Step 3: " + getDesktopContext().getProperty("file.selection.nav.desc.title") + " (3/10)",
                    getDesktopContext().getProperty("file.selection.nav.desc.detail"));
            navigator.registerNavigationPanel(fileSelectionPanel);

            // register file mapping form
            FileMappingDescriptor fileMappingPanel = new FileMappingDescriptor(getDesktopContext().getProperty("file.mapping.nav.desc.id"),
                    "Step 4: " + getDesktopContext().getProperty("file.mapping.nav.desc.title") + " (4/10)",
                    getDesktopContext().getProperty("file.mapping.nav.desc.detail"));
            navigator.registerNavigationPanel(fileMappingPanel);

            // register sample metadata form
            SampleMetaDataDescriptor sampleMetaDataPanel = new SampleMetaDataDescriptor(getDesktopContext().getProperty("sample.metadata.nav.desc.id"),
                    "Step 5: " + getDesktopContext().getProperty("sample.metadata.nav.desc.title") + " (5/10)",
                    getDesktopContext().getProperty("sample.metadata.nav.desc.detail"));
            navigator.registerNavigationPanel(sampleMetaDataPanel);

            // register additional metadata form
            AdditionalMetaDataDescriptor additionalMetaDataPanel = new AdditionalMetaDataDescriptor(getDesktopContext().getProperty("additional.metadata.nav.desc.id"),
                    "Step 6: " + getDesktopContext().getProperty("additional.metadata.nav.desc.title") + " (6/10)",
                    getDesktopContext().getProperty("additional.metadata.nav.desc.detail"));
            navigator.registerNavigationPanel(additionalMetaDataPanel);

            // lab head form
            LabHeadDescriptor labHeadPanel = new LabHeadDescriptor(getDesktopContext().getProperty("pride.labhead.nav.desc.id"),
                    "Step 7: " + getDesktopContext().getProperty("pride.labhead.nav.desc.title") + " (7/10)",
                    getDesktopContext().getProperty("pride.labhead.nav.desc.detail"));
            navigator.registerNavigationPanel(labHeadPanel);

            // register additional project metadata form
            AdditionalDatasetDetailsDescriptor projectTagPanel = new AdditionalDatasetDetailsDescriptor(getDesktopContext().getProperty("additional.project.metadata.nav.desc.id"),
                    "Step 8: " + getDesktopContext().getProperty("additional.project.metadata.nav.desc.title") + " (8/10)",
                    getDesktopContext().getProperty("additional.project.metadata.nav.desc.detail"));
            navigator.registerNavigationPanel(projectTagPanel);

            // register submission summary
            SummaryDescriptor summaryPanel = new SummaryDescriptor(getDesktopContext().getProperty("summary.nav.desc.id"),
                    "Step 9: " + getDesktopContext().getProperty("summary.nav.desc.title") + " (9/10)",
                    getDesktopContext().getProperty("summary.nav.desc.detail"));
            navigator.registerNavigationPanel(summaryPanel);

            // register encryption panel
            EncryptionDescriptor encryptionPanel = new EncryptionDescriptor(getDesktopContext().getProperty("encryption.nav.desc.id"),
                    "Step 10: " + getDesktopContext().getProperty("encryption.nav.desc.title") + " (9/10)",
                    getDesktopContext().getProperty("encryption.nav.desc.detail"));
            navigator.registerNavigationPanel(encryptionPanel);

            // register submission form
            SubmissionDescriptor submissionPanel = new SubmissionDescriptor(getDesktopContext().getProperty("submission.nav.desc.id"),
                    "Step 11: " + getDesktopContext().getProperty("submission.nav.desc.title") + " (10/10)",
                    getDesktopContext().getProperty("submission.nav.desc.detail"));
            navigator.registerNavigationPanel(submissionPanel);

            // register finish form
            navigator.registerNavigationPanel(NavigationPanelDescriptor.FINISH);

            // set the first form
            navigator.setCurrentNavigationPanel(prideLoginPanel.getNavigationPanelId());

        } catch (NavigationException e) {
            logger.error("Failed to register navigation panels", e);
            // todo: system exist
        }

        mainFrame.setContentPane(navigator);
    }

    /**
     * Get the main display frame
     *
     * @return JFrame  main display frame
     */
    public JFrame getMainFrame() {
        return mainFrame;
    }

    /**
     * Get the navigator
     *
     * @return
     */
    public Navigator getNavigator() {
        return navigator;
    }

    @Override
    public void show() {
        DesktopContext context = getDesktopContext();
        int defaultWidth = Integer.parseInt(context.getProperty("px.submission.tool.width"));
        int defaultHeight = Integer.parseInt(context.getProperty("px.submission.tool.height"));
        mainFrame.setSize(defaultWidth, defaultHeight);

        // set display location
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setLocation((d.width - mainFrame.getWidth()) / 2, (d.height - mainFrame.getHeight()) / 2);
        mainFrame.setVisible(true);
    }

    @Override
    public void ready() {
    }

    @Override
    public void postShow() {
    }

    @Override
    public void finish() {
        mainFrame.dispose();
    }

    // Get Window Listener
    public Observable getCloseWindowListener() {
        return appCloseWindowListener;
    }

    public void setDoNotCloseAppFlag() {
        synchronized (this) {
            okToClose = false;
        }
    }

    public void unsetDoNotCloseAppFlag() {
        synchronized (this) {
            okToClose = true;
        }
    }

    public boolean isOkToClose() {
        return okToClose;
    }

    /**
     * Restart the submission process
     */
    public void restart() {
        buildNavigation();
        mainFrame.validate();
        mainFrame.repaint();
    }
}
