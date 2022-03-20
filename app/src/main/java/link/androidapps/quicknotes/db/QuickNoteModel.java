package link.androidapps.quicknotes.db;

/**
 * Model class represents quick_notes table in the database.
 *
 * Created by PKamenov on 22.09.15.
 */
public class QuickNoteModel {
    /**
     * The database ID of the quick note.
     */
    private long id;
    /**
     * The title of the quick note, unique field.
     */
    private String title;
    /**
     * The content of the quick note.
     */
    private String content;

    private long remindTime;

    public QuickNoteModel() {

    }

    public QuickNoteModel(long id, String title, String content, long remindTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.remindTime = remindTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

	public long getRemindTime() {
		return remindTime;
	}

	public void setRemindTime(long remindTime) {
		this.remindTime = remindTime;
	}

    @Override
	public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if(other instanceof QuickNoteModel) {
            QuickNoteModel casted = (QuickNoteModel) other;
            if(this.title == null && casted.getTitle() == null) {
                return true;
            } else {
                return this.title != null && this.title.equals(casted.getTitle());
            }
        } else {
            return false;
        }

    }

}
