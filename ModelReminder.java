
import java.util.Date;

import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "reminders")
public class ModelReminder extends ModelScheduledItem {

	public ModelReminder() {
	}

	public ModelReminder(Integer id, String usermail, String title, String content, Date createdAt, Date dueDate,
			String phone) {
		this(usermail, title, content, createdAt, dueDate, phone);
		this.id = id;
	}

	public ModelReminder(String usermail, String title, String content, Date createdAt, Date dueDate, String phone) {
		super(usermail, content, false, dueDate, phone, title);
	}

}
