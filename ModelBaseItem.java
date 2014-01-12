
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.j256.ormlite.field.DatabaseField;

public class ModelBaseItem {

	@JsonProperty(value = "id")
	@DatabaseField(generatedId = true, canBeNull = false)
	protected int id;

	@JsonProperty(value = "title")
	@DatabaseField(canBeNull = false)
	protected String title;

	@JsonProperty(value = "content")
	@DatabaseField(canBeNull = true)
	protected String content;

	@JsonProperty(value = "createdAt")
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	@DatabaseField(canBeNull = false)
	protected Date createdAt = new Date();

	@JsonProperty(value = "usermail")
	@DatabaseField(canBeNull = false)
	protected String usermail;

	public ModelBaseItem() {
	}

	public ModelBaseItem(int id, Date createdAt, String usermail, String title, String content) {
		this.createdAt = createdAt;
		this.id = id;
		this.usermail = usermail;
		this.title = title;
		this.content = content;
	}

	public ModelBaseItem(String usermail, String title, String content) {
		this.usermail = usermail;
		this.title = title;
		this.content = content;
		this.createdAt = new Date();
	}

	public String getContent() {
		return content;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getUsermail() {
		return usermail;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUsermail(String usermail) {
		this.usermail = usermail;
	}
}
