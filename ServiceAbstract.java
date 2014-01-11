



import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public abstract class ServiceAbstract<T extends ModelBaseItem> {

	protected RepositoryAbstract<T> repo;
    protected SMTPThreadPool smtpThreadPool;
    protected String serverName;
	
	protected HelperLogger logger = HelperLogger.getLogger(this.getClass());

    
	public ServiceAbstract(SMTPThreadPool smtpThreadPool, File dbFilePath, String serverName) throws IOException, SQLException {
		this.serverName = serverName;
        this.smtpThreadPool = smtpThreadPool;
        init(dbFilePath);
	}

    protected abstract void init(File dbFilePath) throws IOException, SQLException;

    public List<T> findAll(ModelUser user) throws SQLException {
        return repo.findAll(user);
    }

    public boolean remove(Integer id) throws SQLException {
        return repo.remove(id);
    }

	public T insert(T item) throws SQLException, WebServerRuntimeException {
		return repo.insert(item);
	}

    public boolean update(T item) throws SQLException {
        return repo.update(item);
    }

    public T findOne(Integer id) throws SQLException {
        return repo.findOne(id);
    }

    public void start() throws SQLException {
    }

}
