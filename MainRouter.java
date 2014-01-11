

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gets the HTTP request (method, path, cookie...) and routes to the designated
 * request handler.
 * <ol>
 * <li>Authenticate the user.</li>
 * <li>Check whether it's a REST API request or a static HTML page.</li>
 * <li>Redirects to the appropriate handler</li>
 * </ol>
 */
public class MainRouter {

    public static final String[] EXCLUDED_SECURITY_ARR = {"task_reply.html", "poll_reply.html"};
    private static final HashSet<String> EXCLUDED_SECURITY = new HashSet<>(Arrays.asList(EXCLUDED_SECURITY_ARR));
    public static final String[] PARAMETRIZED_PATHS_ARR = {"submit_reminder.html", "submit_task.html", "submit_poll.html", "task_reply.html", "poll_reply.html"};
    private static final HashSet<String> PARAMETRIZED_PATHS = new HashSet<>(Arrays.asList(PARAMETRIZED_PATHS_ARR));
    public static final String[] FILE_TYPES_ARR = {"BMP", "GIF", "PNG", "JPG", "HTML", "ICO", "CSS", "JS", "MAP"};
    private static final HashSet<String> FILE_TYPES = new HashSet<>(Arrays.asList(FILE_TYPES_ARR));
    public static final String[] REST_REQUESTS_ARR = {HelperConstants.TASKS_PATH, HelperConstants.REMINDERS_PATH, HelperConstants.POLLS_PATH};
    private static final HashSet<String> REST_REQUESTS = new HashSet<>(Arrays.asList(REST_REQUESTS_ARR));
    public static final HTTPMethod[] REST_METHODS_ARR = {HTTPMethod.GET, HTTPMethod.HEAD, HTTPMethod.POST, HTTPMethod.PUT, HTTPMethod.DELETE};
    private static final HashSet<HTTPMethod> REST_METHODS = new HashSet<>(Arrays.asList(REST_METHODS_ARR));
    public static final HTTPMethod[] STATIC_METHODS_ARR = {HTTPMethod.GET, HTTPMethod.POST, HTTPMethod.PUT, HTTPMethod.DELETE, HTTPMethod.HEAD};
    private static final HashSet<HTTPMethod> STATIC_METHODS = new HashSet<>(Arrays.asList(STATIC_METHODS_ARR));

    private ManagerResources resourcesManager;
    private ManagerPoll pollsManager;
    private ManagerReminders remindersManager;
    private ManagerTasks tasksManager;

    @SuppressWarnings("unchecked")
	public MainRouter(Map<String, ServiceAbstract<? extends ModelBaseItem>> services) {
        this.resourcesManager = new ManagerResources();
        this.pollsManager = new ManagerPoll((ServiceAbstract<ModelPoll>) services.get(ModelPoll.class.getName()));
        this.remindersManager = new ManagerReminders((ServiceAbstractScheduled<ModelReminder>) services.get(ModelReminder.class.getName()));
        this.tasksManager = new ManagerTasks((ServiceAbstractScheduled<ModelTask>) services.get(ModelTask.class.getName()));
    }

    public ModelAppResponse handleRequest(HTTPRequest request, BufferedReader is) {
        ModelUser user = authenticateUser(request);
        if (isExcludedSecurity(request)) {
            user = (user == null) ? ModelUser.annonymous() : user;
        }

        if (isLoginRequest(request)) {
            return routeLogin(request, user);
        } else if (user == null) {
            return redirectLogin(request);
        } else if (isParametrizedRequest(request)) {
            return routeParametrizedRequest(request, user);
        } else if (isStaticRequest(request)) {
            return routeStatic(request);
        } else if (isRESTRequest(request)) {
            return routeREST(request, user, is);
        }
        // else
        return ModelAppResponse.responseError(new WebServerBadRequestException("Unknown "));
    }

    private boolean isParametrizedRequest(HTTPRequest request) {
        return request.getRequestedPage() != null && !request.getUrlParameters().isEmpty() &&
                PARAMETRIZED_PATHS.contains(request.getRequestedPage().getName());
    }

    private ModelAppResponse routeParametrizedRequest(HTTPRequest request, ModelUser user) {
        String requestedPageName = request.getRequestedPage().getName();
        if (requestedPageName.contains("poll")) {
            return pollsManager.handleParamRequest(request.getRequestedPage(), request.getUrlParameters(), user);
        } else if (requestedPageName.contains("reminder")) {
            return remindersManager.handleParamRequest(request.getRequestedPage(), request.getUrlParameters(), user);
        } else if (requestedPageName.contains("task")) {
            return tasksManager.handleParamRequest(request.getRequestedPage(), request.getUrlParameters(), user);
        } else { // not supposed to get here
            return ModelAppResponse.responseError(new WebServerNotFoundException("Not found: " + requestedPageName));
        }
    }


    private boolean isExcludedSecurity(HTTPRequest request) {
        return request.isResource() ||
                (request.getRequestedPage() != null && EXCLUDED_SECURITY.contains(request.getRequestedPage().getName()));
    }

    private boolean isLoginRequest(HTTPRequest request) {
        return request.getRequestedPage() != null
                && request.getRequestedPage().exists()
                && request.getRequestedPage().getName().equals("index.html");
    }

    private boolean isStaticRequest(HTTPRequest request) {
        if (!STATIC_METHODS.contains(request.getMethod())) {
            return false;
        }
        if (request.getRequestedPath() != null) {
            int lastIndexOfDot = request.getRequestedPath().lastIndexOf(".");
            if (lastIndexOfDot > 0) {
                String fileExtension = request.getRequestedPath().substring(lastIndexOfDot + 1);
                if (FILE_TYPES.contains(fileExtension.toUpperCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isRESTRequest(HTTPRequest request) {
        if (!REST_METHODS.contains(request.getMethod())) {
            return false;
        }
        for (String restPath : REST_REQUESTS) {
            if (request.getRequestedPath().startsWith(restPath)) {
                return true;
            }
        }
        return false;
    }

    private ModelAppResponse routeREST(HTTPRequest request, ModelUser user, BufferedReader is) {
        HTTPMethod method = request.getMethod();
        String path = request.getRequestedPath();
        String body = null;
        if (method == HTTPMethod.POST || method == HTTPMethod.PUT) {
            try {
                body = HelperUtils.readRequestBody(is, request);
            } catch (IOException | WebServerBadRequestException e) {
                return ModelAppResponse.responseError(e);
            }
        }

        if (path.startsWith(remindersManager.getServicePath())) {
            return remindersManager.handleRequest(method, path.substring(remindersManager.getServicePath().length()), body, user);
        } else if (path.startsWith(tasksManager.getServicePath())) {
            return tasksManager.handleRequest(method, path.substring(tasksManager.getServicePath().length()), body, user);
        } else if (path.startsWith(pollsManager.getServicePath())) {
            return pollsManager.handleRequest(method, path.substring(pollsManager.getServicePath().length()), body, user);
        } else {
            return ModelAppResponse.responseError(new WebServerBadRequestException("Unknown request: " + method + " , " + path));
        }
    }

    private ModelAppResponse routeStatic(HTTPRequest request) {
        return resourcesManager.handleRequest(request);
    }

    private ModelAppResponse routeLogin(HTTPRequest request, ModelUser user) {
        if (request.getUrlParameters().get("logout") != null) {
            return redirectLogin(request);
        } else if (user != null) {
            return redirectMain(request, user);
        }
        String userMail = request.getUrlParameters().get("email");
        if (userMail != null) {
            if (userMail.matches("([a-zA-Z0-9_.+-]+(@|(\\%40))[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+)")) {
                ModelAppResponse response = ModelAppResponse.responseRedirect("/main.html");
                response.setHeader(HTTPConstants.HEADER_SET_COOKIE, "usermail-" + userMail);
                return response;
            }
        }
        // else
        return ModelAppResponse.responsePage(request.getRequestedPage());
    }

    private ModelAppResponse redirectLogin(HTTPRequest request) {
        ModelAppResponse appResponse = ModelAppResponse.responseRedirect("/index.html");
        appResponse.setHeader(HTTPConstants.HEADER_SET_COOKIE, "usermail-");
        return appResponse;
    }

    private ModelAppResponse redirectMain(HTTPRequest request, ModelUser user) {
        return ModelAppResponse.responseRedirect("/main.html");
    }

    /**
     * Authenticates the user.
     * <ul>
     * <li>If the user is authenticated, returns the User.</li>
     * <li>Otherwise, returns null.</li>
     * </ul>
     *
     * @param request
     * @return
     */
    private ModelUser authenticateUser(HTTPRequest request) {
        String requestCookie = request.getHeader(HTTPConstants.HEADER_COOKIE);
        if (requestCookie != null) {
            Pattern p = Pattern.compile("usermail-([a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+)");
            Matcher m = p.matcher(requestCookie);
            if (m.matches()) {
                return new ModelUser(m.group(1).toLowerCase());
            }
        }
        return null;
    }

}
