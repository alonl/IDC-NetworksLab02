Classes Roles:

Main:
MainRunner.java - The main runner of the application. Runs the HTTP server and the underlying app.
MainRouter.java - The main router of the app. Gets the HTTP request (method, path, cookie...) and routes to the designated request handler.
MainWebServer.java - The HTTP server runner.

SMTP App:
SMTPApp.java - The app runner.
SMTPThreadPool.java - Multi-threaded, asynchronous non-blocking (thread pool) SMTP client.
SMTPClient.java - Simple SMTP for sending email messages.

Application:
ManagerAbstract.java - Abstract Manager for the application - user side logic - of our web app.
ManagerPoll.java - Handles Poll requests.
ManagerReminders.java - Handles Reminder requests.
ManagerTasks.java - Handles Task requests.
ManagerResources.java - Handles requests for static resources.

Models: - Basic object classes to represents our models.
ModelAppResponse.java - Represents the HTTP response from the underlying app.
ModelUser.java - Represents a user (by email address).
ModelMailMessage.java - Represents a mail message.
ModelBaseItem.java - The super class of an item to be saved in the database.
ModelScheduledItem.java - Extends ModelBaseItem. Adds functionality for "scheduled items".
ModelPoll.java - Extends BaseItem. Represents a Poll.
ModelReminder.java - Extends ModelScheduledItem. Represents a Reminder.
ModelTask.java - Extends ModelScheduledItem. Represents a Task.

Repository:
RepositoryAbstract.java - Abstract repository for the persistence of our database.
RepositoryAbstractScheduled.java - Extends RepositoryAbstract. Adds scheduled items funcionality.

Business:
ServiceAbstract.java - Abstract service for the business-side of the application.
ServiceAbstractScheduled.java - Extends ServiceAbstract. Holds the nearest job to run, and updates it, if necessary, on every new item.
ServicePolls.java - Extends ServiceAbstract. Handles any Polls actions.
ServiceReminder.java - Extends ServiceAbstractScheduled. Handles any Reminders actions.
ServiceTasks.java - Extends ServiceAbstractScheduled. Handles any Tasks actions.

HTTP Web Server: - All classes from Lab 1
HTTPConstants.java
HTTPMethod.java
HTTPProcessor.java
HTTPRequest.java
HTTPResponse.java
HTTPThreadPool.java

Helpers:
HelperConstants.java - Some app constants
HelperLogger.java - A logger class. Adds date and time, class name and log level to each log message. Can be configured from config.ini.
HelperUtils.java - Some common utils that are used throughout the app.

Jackson JSON De/Serializers:
JsonAnswersDeserializer.java - De/serializes the answers of the polls (CRLF delimited)
JsonAnswersSerializer.java
JsonDateDeserializer.java - De/serializes dates to comply with the UI.
JsonDateSerializer.java
JsonRecipientsDeserializer.java - De/serializes the recipients of the polls (CRLF delimited, colon between recipient and its answer)
JsonRecipientsSerializer.java

Exceptions:
WebServerBadRequestException.java - Represents an error occurred following a bad request. (4xx status code)
WebServerNotFoundException.java - Represents an error occurred following a request for an unknown resource. (404 status code)
WebServerRuntimeException.java - Represents an error occurred in the server. (5xx status code)
