


import java.io.File;
import java.io.IOException;
import java.sql.SQLException;


public class ServiceTasks extends ServiceAbstractScheduled<ModelTask> {

	public ServiceTasks(SMTPThreadPool smtpThreadPool, File dbFilePath, String serverName) throws IOException,
			SQLException {
		super(smtpThreadPool, dbFilePath, serverName);
	}

	@Override
	protected RepositoryAbstractScheduled<ModelTask> initRepo(File dbFilePath) throws IOException, SQLException {
		return new RepositoryAbstractScheduled<ModelTask>(dbFilePath) {
			@Override
			protected Class<ModelTask> getModelClass() {
				return ModelTask.class;
			}
		};
	}

	@Override
	public ModelTask insertNoScheduler(ModelTask task) throws SQLException, WebServerRuntimeException {
		ModelTask newTask = super.insertNoScheduler(task);
		sendMessage(newTask, new ModelMailMessage(task.getUsermail(), task.getToMail(), generateSubjectNew(task),
				generateMessage(task, true)), true);
		return newTask;
	}

	@Override
	public void performDueDateAction(ModelTask nearestItem) {
		logger.info("Due date is here! " + nearestItem.getDueDate());
		sendMessage(nearestItem, new ModelMailMessage(nearestItem.getUsermail(), nearestItem.getUsermail(),
				generateSubjectUser(nearestItem), generateMessage(nearestItem, true)), true);
		sendMessage(nearestItem, new ModelMailMessage(nearestItem.getUsermail(), nearestItem.getToMail(),
				generateSubjectTo(nearestItem), generateMessage(nearestItem, true)), false);
	}

	@Override
	public boolean updateDue(ModelTask nearestItem) throws SQLException {
		nearestItem.setStatus(ModelTask.TaskStatus.TIME_DUE);
		return super.updateDue(nearestItem);
	}

	@Override
	public boolean updateComplete(Integer id) throws SQLException {
		ModelTask task = findOne(id);
		if (task != null) {
			task.setStatus(ModelTask.TaskStatus.COMPLETED);
			task.setDue(true);
			sendMessage(task, new ModelMailMessage(task.getUsermail(), task.getUsermail(), generateSubjectDone(task),
					generateMessage(task, false)), true);
			return update(task);
		}
		return false;
	}

	private void sendMessage(ModelTask task, ModelMailMessage mailMessage, boolean sendSms) {
		logger.info("Sending a mail message for the task: '" + task.getTitle() + "', to: " + mailMessage.getTo());
		smtpThreadPool.sendMessage(mailMessage);
		if (sendSms && task.getPhone() != null) {
			smtpThreadPool.sendSMS(task.getPhone(), generateSMS(task.getTitle(), task.getContent()));
		}
	}

	private String generateSubjectNew(ModelTask task) {
		return "Task: Hi! A new task has been asigned to you. (ID: " + task.getId() + ")";
	}

	private String generateSubjectDone(ModelTask task) {
		return "Task: Ohh yeah! Your task was completed! " + task.getId();
	}

	private String generateSubjectUser(ModelTask task) {
		return "Task: Pay attention! Your friend hasn't completed his job! " + task.getId();
	}

	private String generateSubjectTo(ModelTask task) {
		return "Task: HURRY! The due date for your task is here! " + task.getId();
	}

	private String generateMessage(ModelTask task, boolean setReplyLink) {
		String message = "Title: '" + task.getTitle() + "'\r\n" + "Content: '" + task.getContent() + "'\r\n" + "To: '"
				+ task.getToMail() + "'\r\n" + "Due Date: '" + task.getDueDate() + "'\r\n" + "Created At: '"
				+ task.getCreatedAt() + "'\r\n" + "User: '" + task.getUsermail() + "'\r\n";
		if (setReplyLink) {
			String link = "http://" + serverName + "/task_reply.html?id=" + task.getId();
			return message + "Click here to set this task status to completed: " + link;
		} else {
			return message;
		}
	}

	private String generateSMS(String title, String content) {
		return title + "\r\n" + content;
	}

}
