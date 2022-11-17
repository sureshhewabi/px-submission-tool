package uk.ac.ebi.pride.gui.form;

import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.pride.App;
import uk.ac.ebi.pride.archive.submission.model.user.ContactDetail;
import uk.ac.ebi.pride.data.model.Contact;
import uk.ac.ebi.pride.data.model.Submission;
import uk.ac.ebi.pride.gui.data.SubmissionRecord;
import uk.ac.ebi.pride.gui.form.comp.ContextAwareNavigationPanelDescriptor;
import uk.ac.ebi.pride.gui.navigation.Navigator;
import uk.ac.ebi.pride.gui.task.GetPrideProjectFilesTask;
import uk.ac.ebi.pride.gui.task.GetPrideUserDetailTask;
import uk.ac.ebi.pride.gui.util.ValidationState;
import uk.ac.ebi.pride.toolsuite.gui.GUIUtilities;
import uk.ac.ebi.pride.toolsuite.gui.blocker.DefaultGUIBlocker;
import uk.ac.ebi.pride.toolsuite.gui.blocker.GUIBlocker;
import uk.ac.ebi.pride.toolsuite.gui.task.Task;
import uk.ac.ebi.pride.toolsuite.gui.task.TaskEvent;
import uk.ac.ebi.pride.toolsuite.gui.task.TaskListener;

import javax.help.HelpBroker;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Rui Wang
 * @version $Id$
 *          <p/>
 */
public class PrideLoginDescriptor extends ContextAwareNavigationPanelDescriptor implements TaskListener<ContactDetail, String> {

    private static final String COUNTRY = "Country";
    private static final String ORCID = "Orcid";
    private static final String TERMS = "Terms";

    public PrideLoginDescriptor(String id, String title, String desc) {
        super(id, title, desc, new PrideLoginForm());
    }

    /**
     * Method to be performed to show help document
     * Override this method to add help functionality
     */
    @Override
    public void getHelp() {
        HelpBroker hb = appContext.getMainHelpBroker();
        hb.showID("help.login", "javax.help.SecondaryWindow", "main");
    }

    @Override
    public void beforeDisplayingPanel() {
        Submission submission = appContext.getSubmissionRecord().getSubmission();
        Contact contact = submission.getProjectMetaData().getSubmitterContact();

        if (contact != null) {
            // set previous username and password
            setExistingSubmitter(contact);
        }

        firePropertyChange(BEFORE_DISPLAY_PANEL_PROPERTY, false, true);
    }

    @Override
    public void displayingPanel() {
        Navigator navigator = ((App) App.getInstance()).getNavigator();
        JButton nextButton = navigator.getNextButton();

        nextButton.setText(appContext.getProperty("pride.login.button.title"));
    }

    private void setExistingSubmitter(Contact contact) {
        PrideLoginForm form = (PrideLoginForm) getNavigationPanel();
        form.setUserName(contact.getUserName());
        form.setPassword(contact.getPassword());
        form.setSubmitterName(contact.getName());
        form.setAffiliation(contact.getAffiliation());
        form.setEmail(contact.getEmail());
        //todo
    }

    @Override
    public void beforeHidingForNextPanel() {
        // validate the content in the table
        PrideLoginForm form = (PrideLoginForm) getNavigationPanel();
        ValidationState state = form.doValidation();
        if (!ValidationState.ERROR.equals(state)) {
            getPrideUserDetail(form);
        } else {
            // notify validation error
            firePropertyChange(BEFORE_HIDING_FOR_NEXT_PANEL_PROPERTY, true, false);
        }
    }

    private void getPrideUserDetail(PrideLoginForm form) {
        String username = form.getUserName().trim();
        String password = new String(form.getPassword()).trim();
        // record user name and password
        SubmissionRecord submissionRecord = appContext.getSubmissionRecord();
        submissionRecord.setUserName(username);
        submissionRecord.setPassword(password);
        // launch a new task for login
//        Task task = new GetPrideProjectFilesTask(username, password.toCharArray());
        Task task = new GetPrideUserDetailTask(username, password.toCharArray());
        task.addTaskListener(this);
        task.setGUIBlocker(new DefaultGUIBlocker(task, GUIBlocker.Scope.NONE, null));
        App.getInstance().getDesktopContext().addTask(task);
    }

    @Override
    public void beforeHidingForPreviousPanel() {
        //hide any visible warning balloon tip
        PrideLoginForm form = (PrideLoginForm) getNavigationPanel();
        form.hideWarnings();
        saveFormContent();

        firePropertyChange(BEFORE_HIDING_FOR_PREVIOUS_PANEL_PROPERTY, false, true);
    }

  @Override
  public void succeed(TaskEvent<ContactDetail> event) {
    Set<String> updateRequiredFields = new HashSet<>();
    ContactDetail details = event.getValue();
    if (details != null) {
      if (StringUtils.isEmpty(details.getCountry())) {
        updateRequiredFields.add(COUNTRY);
      }
      if (StringUtils.isEmpty(details.getOrcid())) {
        updateRequiredFields.add(ORCID);
      }
      if (!details.getAcceptedTermsOfUse()) {
        updateRequiredFields.add(TERMS);
      }
      if (0 < updateRequiredFields.size()) {
        showAskUserDetailsUpdatePane(updateRequiredFields);
      }
      if (!updateRequiredFields.contains(TERMS)) { // continue
        updateFormContent(details); // set name and affiliation}
        saveFormContent();
        PrideLoginForm form = (PrideLoginForm) getNavigationPanel(); // hide warnings
        form.hideWarnings();
        firePropertyChange(BEFORE_HIDING_FOR_NEXT_PANEL_PROPERTY, false, true); // notify success
      } // else can't continue
    }
  }

  private void showAskUserDetailsUpdatePane(Set<String> updateRequiredFileds) {
    JLabel label = new JLabel();
    Font font = label.getFont();
    StringBuilder style = new StringBuilder("font-family:" + font.getFamily() + ";");
    style.append("font-weight:").append(font.isBold() ? "bold" : "normal").append(";");
    style.append("font-size:").append(font.getSize()).append("pt;");
    StringBuilder html = new StringBuilder();
    html.append("<html><body style=\"").append(style).append("\">");
    boolean askCountry = updateRequiredFileds.contains(COUNTRY);
    boolean askOrcid = updateRequiredFileds.contains(ORCID);
    boolean askTerms = updateRequiredFileds.contains(TERMS);
    if (askCountry || askOrcid) {
      html.append(appContext.getProperty("pride.login.ask.update.header"));
      if (askCountry) {
        html.append(appContext.getProperty("pride.login.ask.update.body.country"));
      }
      if (askOrcid) {
        html.append(appContext.getProperty("pride.login.ask.update.body.orcid"));
      }
      html.append(appContext.getProperty("pride.login.ask.update.footer.start"));
    }
    if (askTerms) {
      html.append(appContext.getProperty("pride.login.ask.update.body.terms"));
    }
    html.append(appContext.getProperty("pride.login.ask.update.footer"));
    html.append("</body></html>");

    // html content
    JEditorPane ep = new JEditorPane("text/html", html.toString());
    ep.addHyperlinkListener(
        new HyperlinkListener() {
          @Override
          public void hyperlinkUpdate(HyperlinkEvent e) {
            try {
              if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                Desktop.getDesktop().browse(e.getURL().toURI());
            } catch (Exception ex) {
              // couldn't display error message
            }
          }
        });
    ep.setEditable(false);
    ep.setBackground(label.getBackground());
    JOptionPane.showConfirmDialog(
        app.getMainFrame(),
        ep,
        appContext.getProperty("pride.login.ask.update.title"),
        JOptionPane.CLOSED_OPTION,
        JOptionPane.WARNING_MESSAGE);
  }

    /**
     * Save the content from the form to AppContext
     */
    private void saveFormContent() {
        PrideLoginForm form = (PrideLoginForm) getNavigationPanel();
        Submission submission = appContext.getSubmissionRecord().getSubmission();

        Contact contact = submission.getProjectMetaData().getSubmitterContact();
        if (contact == null) {
            contact = new Contact();
            submission.getProjectMetaData().setSubmitterContact(contact);
        }

        contact.setUserName(form.getUserName());
        contact.setPassword(form.getPassword());
        contact.setName(form.getSubmitterName());
        contact.setAffiliation(form.getAffiliation());
        contact.setEmail(form.getEmail());
    }

    private void updateFormContent(ContactDetail details) {
        PrideLoginForm form = (PrideLoginForm) getNavigationPanel();
        String submitterName = details.getFirstName() + " " + details.getLastName();
        String affiliation = details.getAffiliation();
        String email = details.getEmail();
        String country = details.getCountry();
        String orcid = details.getOrcid();
        form.setSubmitterName(submitterName);
        form.setAffiliation(affiliation);
        form.setEmail(email);
        form.setCountry(country);
        form.setOrcid(orcid);
    }

    @Override
    public void started(TaskEvent<Void> event) {
        //set the in-progress icon
        Navigator navigator = app.getNavigator();
        JButton nextButton = navigator.getNextButton();

        Icon newIcon = GUIUtilities.loadIcon(appContext.getProperty("pride.login.button.loading.small.icon"));
        nextButton.setIcon(newIcon);
    }

    @Override
    public void process(TaskEvent<List<String>> event) {
        List<String> errors = event.getValue();
        String errorMessage = errors.contains("UserCredentials mismatch") ?
                appContext.getProperty("pride.login.credentials.error.message") :
                appContext.getProperty("pride.login.proxy.error.message");
        Runnable eventDispatcher = new Runnable() {
            public void run() {
                // show warning dialog
                JLabel label = new JLabel();
                Font font = label.getFont();
                StringBuilder style = new StringBuilder("font-family:" + font.getFamily() + ";");
                style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
                style.append("font-size:" + font.getSize() + "pt;");
                // html content
                JEditorPane ep = new JEditorPane("text/html", "<html><body style=\"" + style + "\">" //
                        + errorMessage + "</body></html>");
                ep.addHyperlinkListener(new HyperlinkListener() {
                    @Override
                    public void hyperlinkUpdate(HyperlinkEvent e) {
                        try {
                            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                                Desktop.getDesktop().browse(e.getURL().toURI());
                        } catch (Exception ex) {
                            //couldn't display error message
                        }
                    }
                });
                ep.setEditable(false);
                ep.setBackground(label.getBackground());
                JOptionPane.showConfirmDialog(app.getMainFrame(),
                    ep,
                    appContext.getProperty("pride.login.error.title"),
                    JOptionPane.CLOSED_OPTION, JOptionPane.WARNING_MESSAGE);
            }
        };


        EventQueue.invokeLater(eventDispatcher);
    }

    @Override
    public void finished(TaskEvent<Void> event) {
        // replace the in-process icon with next button
        Navigator navigator = app.getNavigator();
        JButton nextButton = navigator.getNextButton();

        Icon newIcon = GUIUtilities.loadIcon(appContext.getProperty("next.button.small.icon"));
        nextButton.setIcon(newIcon);
    }

    @Override
    public void failed(TaskEvent<Throwable> event) {
    }

    @Override
    public void cancelled(TaskEvent<Void> event) {
    }

    @Override
    public void interrupted(TaskEvent<InterruptedException> iex) {
    }

    @Override
    public void progress(TaskEvent<Integer> progress) {
    }
}
