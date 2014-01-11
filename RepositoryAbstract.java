


import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;




import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Abstract repository for the persistence of our database.
 *
 * @param <T>
 */
public abstract class RepositoryAbstract<T extends ModelBaseItem> {

	private HelperLogger logger = HelperLogger.getLogger(this.getClass());
	
    protected Dao<T, Integer> dao;

    public RepositoryAbstract(File dbFilePath) throws SQLException, IOException {
        ConnectionSource connectionSource = new JdbcConnectionSource("jdbc:sqlite:" + dbFilePath.getCanonicalPath());
        dao = createDao(connectionSource);
    }

    protected Dao<T, Integer> createDao(ConnectionSource connectionSource) throws SQLException {
        Dao<T, Integer> dao = DaoManager.createDao(connectionSource, getModelClass());
        TableUtils.createTableIfNotExists(connectionSource, getModelClass());
        return dao;
    }

    protected abstract Class<T> getModelClass();

    public List<T> findAll(ModelUser user) throws SQLException {
        return dao.queryForEq("usermail", user.getUserMail());
    }

    public T findOne(Integer id) throws SQLException {
        return dao.queryForId(id);
    }

    public T insert(T item) throws SQLException {
        return dao.createIfNotExists(item);
    }

    public boolean update(T item) throws SQLException {
        T currentItem = findOne(item.getId());
        if (currentItem == null) {
        	logger.warn("Cannot update a non-exsistent item.");
        	return false;
        }
        item.setCreatedAt(currentItem.getCreatedAt());
        item.setUsermail(currentItem.getUsermail());
    	return dao.update(item) == 1;
    }

    public boolean remove(Integer id) throws SQLException {
        return dao.deleteById(id) == 1;
    }

}
