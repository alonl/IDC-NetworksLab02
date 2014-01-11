


import java.util.Date;



import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.j256.ormlite.field.DatabaseField;

public class ModelScheduledItem extends ModelBaseItem {

    @JsonProperty(value = "isDue")
    @DatabaseField(canBeNull = false)
    protected boolean isDue;
    
    @JsonProperty(value = "dueDate")
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @JsonSerialize(using = JsonDateSerializer.class)
    @DatabaseField(canBeNull = false)
    protected Date dueDate;    
    
    @JsonProperty(value = "phone")
    @DatabaseField(canBeNull = true)
    protected String phone;

    public ModelScheduledItem() {
    }
    
    public ModelScheduledItem(int id, Date createdAt, String usermail, String content, boolean isDue, Date dueDate, String phone, String title) {
		super(id, createdAt, usermail, title, content);
		this.isDue = isDue;
		this.dueDate = dueDate;
		this.phone = phone;
	}

	public ModelScheduledItem(String usermail, String content, boolean isDue, Date dueDate, String phone, String title) {
		super(usermail, title, content);
		this.content = content;
		this.isDue = isDue;
		this.dueDate = dueDate;
		this.phone = phone;
		this.title = title;
	}

    public Date getDueDate() {
        return dueDate;
    }

    public String getPhone() {
        return phone;
    }


    public boolean isDue() {
        return isDue;
    }

    public void setDue(boolean isDue) {
        this.isDue = isDue;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
