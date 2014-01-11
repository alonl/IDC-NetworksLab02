

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DatabaseField;

public class ModelTask extends ModelScheduledItem {

	public enum TaskStatus {
		IN_PROGRESS, COMPLETED, TIME_DUE
	}

	@JsonProperty(value = "toMail")
	@DatabaseField(canBeNull = false)
	private String toMail;
	@JsonProperty(value = "status")
	@DatabaseField(canBeNull = false)
	private TaskStatus status = TaskStatus.IN_PROGRESS;

	public ModelTask() {
	}
	
	public ModelTask(String usermail, String content, boolean isDue, Date dueDate, String phone, String title, String toMail,
			TaskStatus status) {
		super(usermail, content, isDue, dueDate, phone, title);
		this.toMail = toMail;
		this.status = status;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
		if (status == TaskStatus.IN_PROGRESS) {
			this.setDue(false);
		} else if (status == TaskStatus.COMPLETED) {
			this.setDue(true);
		}
	}

	@Override
	public void setDue(boolean isDue) {
		this.isDue = isDue;
	}

	public String getToMail() {
		return toMail;
	}

	public void setToMail(String toMail) {
		this.toMail = toMail;
	}
}
