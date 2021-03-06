package teammates.ui.automated;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import teammates.common.exception.PageNotFoundException;
import teammates.common.exception.TeammatesException;
import teammates.common.util.Const.ActionURIs;
import teammates.common.util.Const.TaskQueue;

/**
 * Generates the matching {@link AutomatedAction} for a given URI.
 */
public class AutomatedActionFactory {
    
    private static Map<String, Class<? extends AutomatedAction>> actionMappings =
            new HashMap<String, Class<? extends AutomatedAction>>();
    
    static {
        // Cron jobs
        map(ActionURIs.AUTOMATED_LOG_COMPILATION, CompileLogsAction.class);
        map(ActionURIs.AUTOMATED_FEEDBACK_OPENING_REMINDERS, FeedbackSessionOpeningRemindersAction.class);
        map(ActionURIs.AUTOMATED_FEEDBACK_CLOSED_REMINDERS, FeedbackSessionClosedRemindersAction.class);
        map(ActionURIs.AUTOMATED_FEEDBACK_CLOSING_REMINDERS, FeedbackSessionClosingRemindersAction.class);
        map(ActionURIs.AUTOMATED_FEEDBACK_PUBLISHED_REMINDERS, FeedbackSessionPublishedRemindersAction.class);
        
        // Task queue workers
        map(TaskQueue.ADMIN_PREPARE_EMAIL_WORKER_URL, AdminPrepareEmailWorkerAction.class);
        map(TaskQueue.ADMIN_SEND_EMAIL_WORKER_URL, AdminSendEmailWorkerAction.class);
        map(TaskQueue.COURSE_JOIN_REMIND_EMAIL_WORKER_URL, CourseJoinRemindEmailWorkerAction.class);
        map("/auto/emailWorker", null);
        map(TaskQueue.FEEDBACK_SESSION_REMIND_EMAIL_WORKER_URL, FeedbackSessionRemindEmailWorkerAction.class);
        map(TaskQueue.FEEDBACK_SESSION_REMIND_PARTICULAR_USERS_EMAIL_WORKER_URL,
                FeedbackSessionRemindParticularUsersEmailWorkerAction.class);
        map("/auto/feedbackSubmissionAdjustmentWorker", null);
        map(TaskQueue.SEND_EMAIL_WORKER_URL, SendEmailWorkerAction.class);
    }
    
    private static void map(String actionUri, Class<? extends AutomatedAction> actionClass) {
        actionMappings.put(actionUri, actionClass);
    }
    
    /**
     * @return the matching {@link AutomatedAction} object for the URI in the {@code req}.
     */
    public AutomatedAction getAction(HttpServletRequest req, HttpServletResponse resp) {
        String uri = req.getRequestURI();
        if (uri.contains(";")) {
            uri = uri.split(";")[0];
        }
        
        AutomatedAction action = getAction(uri);
        action.initialiseAttributes(req, resp);
        return action;
    }
    
    private AutomatedAction getAction(String uri) {
        Class<? extends AutomatedAction> action = actionMappings.get(uri);
        
        if (action == null) {
            throw new PageNotFoundException("Page not found for " + uri);
        }
        
        try {
            return action.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not create the action for " + uri + ": "
                                       + TeammatesException.toStringWithStackTrace(e));
        }
    }
    
}
