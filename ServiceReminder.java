
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class ServiceReminder extends ServiceAbstractScheduled<ModelReminder> {

	public ServiceReminder(SMTPThreadPool smtpThreadPool, File dbFilePath, String serverName) throws IOException,
			SQLException {
		super(smtpThreadPool, dbFilePath, serverName);
	}

	@Override
	protected RepositoryAbstractScheduled<ModelReminder> initRepo(File dbFilePath) throws IOException, SQLException {
		return new RepositoryAbstractScheduled<ModelReminder>(dbFilePath) {
			@Override
			protected Class<ModelReminder> getModelClass() {
				return ModelReminder.class;
			}
		};
	}

	@Override
	public void performDueDateAction(ModelReminder nearestItem) {
		logger.info("Due date is here! " + nearestItem.getDueDate());
		logger.info("Sending a mail message for the reminder: " + nearestItem.getTitle());
		smtpThreadPool.sendMessage(new ModelMailMessage(nearestItem.getUsermail(), nearestItem.getUsermail(),
				generateSubject(nearestItem), generateMessage(nearestItem)));
		smtpThreadPool.sendSMS(nearestItem.getPhone(), generateSMS(nearestItem.getTitle(), nearestItem.getContent()));
	}

	private String generateSubject(ModelReminder reminder) {
		return "Reminder: Hello! The due date for your reminder is here! (ID: " + reminder.getId() + ")";
	}

	private String generateMessage(ModelReminder reminder) {
		return "Title: '" + reminder.getTitle() + "'\r\n" + "Content: '" + reminder.getContent() + "'\r\n"
				+ "Due Date: '" + reminder.getDueDate() + "'\r\n" + "Created At: '" + reminder.getCreatedAt() + "'\r\n"
				+ "User: '" + reminder.getUsermail() + "'\r\n";
	}

	private String generateSMS(String title, String content) {
		return title + "\r\n" + content;
	}
}
