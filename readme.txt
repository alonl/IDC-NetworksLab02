ReadMe:

In this lab we have bulit SMTP client and a task manager named ZCOR'TO that provides the following services:
o E-mail reminder
o Add and assign task + reminder of due date
o Poll a group

in addition we have implmented cool extra features that we will describe in bonus.txt

Our classes and pages:
1. SMTPClient
- SMTPClient.java : send messages throgh smtp
- SMTPThreadPool.java :threadpool that opens SMTP client threads (for concurrency) 

2. index.html
allow users to log-in with their mail address

3. main.html
main page for the application

4. reminders classes





6. polls classes

- 

Main classes: 
- Runner.java : reads the config.ini and starts the server 
- MainRouter.java : the main router of the app routes the requests to the designated manager
- WebServer.java : starts the HTTP server  
- SMTPApp.java : starts the underlying app

Managers classes: 
- RemindersManager.java : to handle user requests for remainders 
- TasksManager.java : to handle user requests for tasks
- PollsManager.java : to handle user requests for polls
- ResourcesManager.java : to handle user requests to static resources 

Generic classes:
- BaseItem.java : the base item in our app
- ScheduledItem.java : extends baseItem- adds schedualing attributes 
- AbstractManager.java : application asspect- an abstract namager to handle user requests 
- AbstractRepository.java : persists the data
- AbstractScheduledRepository.java : extends AbstractRepository, persists the data of scheduled items
- AbstractScheduledService.java : extends absract service for handling with scheduled items
- AbstractService.java : handles the buisines asspect of the app


Helper classes: 
- HTTPConstants.java
- HTTPMethod.java : enoumeration 


Exceptions classes:
- WebServerBadRequestException.java
- WebServerNotFoundException.java
- WebServerRuntimeException.java

Web server classes: (from lab1)
- HTTPProcessor.java
- HTTPRequest.java
- HTTPResponse.java
- HTTPThreadPool.java
- HTTPUtils.java

Models:
- User.java
- Reminder.java
- Task.java
- Poll.java
- AppResponse.java
- MailMessage.java

Services:
- TasksService.java
- ReminderService.java
- PollsService.java

Others:
- Logger.java
- CustomDateSerializer.java
- CustomJsonDateDeserializer.java






