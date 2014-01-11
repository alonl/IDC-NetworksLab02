


import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * Abstract service for the business-side of the application. Instantiates a
 * repository. Holds the nearest job to run, and updates it, if necessary, on
 * every new item.
 */
public abstract class ServiceAbstractScheduled<T extends ModelScheduledItem> extends ServiceAbstract<T> {

	private final Object nearestItemLock = new Object();
	private T nearestItem;

	private ScheduledExecutorService timer;
	private Runnable timerTask;
	private ScheduledFuture<?> nextToRun;

	public ServiceAbstractScheduled(SMTPThreadPool smtpThreadPool, File dbFilePath, String serverName)
			throws IOException, SQLException {
		super(smtpThreadPool, dbFilePath, serverName);
	}

	@Override
	protected void init(File dbFilePath) throws IOException, SQLException {
		repo = initRepo(dbFilePath);
		timer = Executors.newSingleThreadScheduledExecutor();
		timerTask = new Runnable() {
			@Override
			public void run() {
				synchronized (nearestItemLock) {
					try {
						if (nearestItem != null) {
							performDueDateAction(nearestItem);
							updateDue(nearestItem);
						}
						updateNearest();
					} catch (SQLException e) {
						logger.error("Unexpected error.", e);
					}
				}
			}
		};
	}

	protected abstract RepositoryAbstractScheduled<T> initRepo(File dbFilePath) throws IOException, SQLException;

	@Override
	public void start() throws SQLException {
		updateNearest();
	}

	@Override
	public final T insert(T item) throws SQLException, WebServerRuntimeException {
		T inserted = insertNoScheduler(item);
		if (inserted == null || inserted.getDueDate() == null) {
			return null;
		}
		updateNearest(item);
		return inserted;
	}

	public T insertNoScheduler(T item) throws SQLException, WebServerRuntimeException {
		return super.insert(item);
	}

	@Override
	public boolean update(T item) throws SQLException {
		boolean res = updateNoScheduler(item);
		if (res) {
			updateNearest(item);
		}
		return res;
	}

	public boolean updateNoScheduler(T item) throws SQLException {
		return super.update(item);
	}

	@Override
	public boolean remove(Integer id) throws SQLException {
		boolean res = removeNoScheduler(id);
		if (res && nearestItem != null && nearestItem.getId() == id) {
			updateNearest();
		}
		return res;
	}
	
	public boolean removeNoScheduler(Integer id) throws SQLException {
		return super.remove(id);
	}

	public abstract void performDueDateAction(T nearestItem);

	public boolean updateDue(T item) throws SQLException {
		item.setDue(true);
		return update(item);
	}

	public boolean updateComplete(Integer id) throws SQLException {
		T item = findOne(id);
		if (item != null) {
			return updateDue(item);
		}
		return false;
	}
	
	public void updateNearest() throws SQLException {
		logger.info("Updating nearest item...");
		nearestItem = null;
		T newNearest = ((RepositoryAbstractScheduled<T>) repo).findNearest();
		updateNearest(newNearest);
	}

	private void updateNearest(T item) throws SQLException {
		synchronized (nearestItemLock) {
			if (item != null && nearestItem != null && item.getId() == nearestItem.getId()) {
				updateNearest();
			}
			if (item != null
					&& (nearestItem == null || (!item.isDue() && item.getDueDate().before(nearestItem.getDueDate())))) {
				logger.debug("New nearest item found: " + item.getTitle());
				nearestItem = item;
				setTimer(nearestItem.getDueDate());
			} else {
				logger.debug("No new nearest item found.");
			}
		}
	}

	private void setTimer(Date dueDate) {
		if (nextToRun != null) {
			nextToRun.cancel(true);
		}
		nextToRun = timer.schedule(timerTask, dueDate.getTime() - new Date().getTime(), TimeUnit.MILLISECONDS);
		logger.info("Timer set to perform an action on: " + dueDate);
	}

}
