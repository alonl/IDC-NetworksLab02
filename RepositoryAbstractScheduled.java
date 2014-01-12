
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public abstract class RepositoryAbstractScheduled<T extends ModelScheduledItem> extends RepositoryAbstract<T> {

	public RepositoryAbstractScheduled(File dbFilePath) throws IOException, SQLException {
		super(dbFilePath);
	}

	public T findNearest() throws SQLException {
		return dao.queryBuilder().orderBy("dueDate", true).where().eq("isDue", false).queryForFirst();
	}

}
