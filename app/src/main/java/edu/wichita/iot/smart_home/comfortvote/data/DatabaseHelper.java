package edu.wichita.iot.smart_home.comfortvote.data;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	// name of the database file for your application -- change to something appropriate for your app
	private static final String DATABASE_NAME = "comfortDb.db";
	// any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 4;

	// the DAO object we use to access the ComfData table
    private Dao<ComfData, Integer> comfDao = null;
	private Dao<SensorData, Integer> sensorDao = null;
	private Dao<SensorSampleData, Integer> sensorSampleDao = null;
    private Dao<SettingData, Integer> settingDao = null;
	private static final AtomicInteger usageCounter = new AtomicInteger(0);

	// we do this so there is only one helper
	private static DatabaseHelper helper = null;

	private DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * Get the helper, possibly constructing it if necessary. For each call to this method, there should be 1 and only 1
	 * call to {@link #close()}.
	 */
	public static synchronized DatabaseHelper getHelper(Context context) {
		if (helper == null) {
			helper = new DatabaseHelper(context);
		}
		usageCounter.incrementAndGet();
		return helper;
	}

	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, ComfData.class);
			TableUtils.createTable(connectionSource, SensorData.class);
			TableUtils.createTable(connectionSource, SensorSampleData.class);
            TableUtils.createTable(connectionSource, SettingData.class);

		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, ComfData.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the Database Access Object (DAO) for our ComfData class. It will create it or just give the cached
	 * value.
	 */
    public Dao<ComfData, Integer> getComfDataDao() throws SQLException {
        if (comfDao == null) {
            comfDao = getDao(ComfData.class);
        }
        return comfDao;
    }

	public Dao<SensorData, Integer> getSendorDataDao() throws SQLException {
		if (sensorDao == null) {
			sensorDao = getDao(SensorData.class);
		}
		return sensorDao;
	}


	public Dao<SensorSampleData, Integer> getSendorSampleDataDao() throws SQLException {
		if (sensorSampleDao == null) {
			sensorSampleDao = getDao(SensorSampleData.class);
		}
		return sensorSampleDao;
	}


	public Dao<SettingData, Integer> getSettingDataDao() throws SQLException {
        if (settingDao == null) {
            settingDao = getDao(SettingData.class);
        }
        return settingDao;
    }


	public SettingData getSettignData(){
		try {
			Dao<SettingData, Integer> settingDao = getSettingDataDao();
			if (settingDao.countOf() >  1){
				TableUtils.clearTable(connectionSource, SettingData.class);
			}

			if (settingDao.countOf() == 0){
				SettingData settingData = new SettingData();
				settingData.samplingInterval  = 30;
				settingData.samplingWaitTime = 15 ;
				settingData.notificationInterval = 0;

				settingDao.create(settingData);
				return settingData;
			}
			return settingDao.queryForAll().get(0);
		} catch (SQLException e){
			e.printStackTrace();
		}
		return null;
	}

	public void saveSettignData(int samplingInterval, int samplingWaitTime, int notificationInterval){
		try {
			Dao<SettingData, Integer> settingDao = getSettingDataDao();
			if (settingDao.countOf() >  1){
				TableUtils.clearTable(connectionSource, SettingData.class);
			}

			SettingData settingData;
			if (settingDao.countOf() == 0) {
				settingData = new SettingData();
				settingDao.create(settingData);
			} else {
				settingData = settingDao.queryForAll().get(0);
			}

			settingData.samplingInterval = samplingInterval;
			settingData.samplingWaitTime   = samplingWaitTime;
			settingData.notificationInterval = notificationInterval;
			settingDao.update(settingData);
		} catch (SQLException e){
			e.printStackTrace();
		}
	}



	/**
	 * Close the database connections and clear any cached DAOs. For each call to {@link #getHelper(Context)}, there
	 * should be 1 and only 1 call to this method. If there were 3 calls to {@link #getHelper(Context)} then on the 3rd
	 * call to this method, the helper and the underlying database connections will be closed.
	 */
	@Override
	public void close() {
		if (usageCounter.decrementAndGet() == 0) {
			super.close();
			comfDao = null;
			helper = null;
		}
	}

    public void reCreateDB() throws SQLException {
		TableUtils.dropTable(connectionSource,  ComfData.class, true);
		TableUtils.createTable(connectionSource,  ComfData.class);

		TableUtils.dropTable(connectionSource,  SensorData.class, true);
		TableUtils.createTable(connectionSource,  SensorData.class);

		TableUtils.dropTable(connectionSource,  SensorSampleData.class, true);
		TableUtils.createTable(connectionSource,  SensorSampleData.class);

		TableUtils.dropTable(connectionSource,  SettingData.class, true);
		TableUtils.createTable(connectionSource,  SettingData.class);

	}

	public void clearComfDaoTable( ) throws SQLException{
		TableUtils.clearTable(connectionSource, ComfData.class);
	}

	public void clearSensorDataTable( ) throws SQLException{
		TableUtils.clearTable(connectionSource, SensorData.class);
	}

	public void clearSensorSampleDataTable( ) throws SQLException{
		TableUtils.clearTable(connectionSource, SensorSampleData.class);
	}


	public List<SensorData> getLasSensorData(int number){
		List<SensorData> list=null;
		try {
			QueryBuilder<SensorData, Integer> builder = getSendorDataDao().queryBuilder();
			builder.limit(number);
			builder.orderBy("id", false);  // true for ascending, false for descending
			list = getSendorDataDao().query(builder.prepare());  // returns list of ten items
		} catch (SQLException e){
			System.out.println(e);
		}
		return list;
	}

	public List<ComfData> getLasComfData(int number){
		List<ComfData> list=null;
		try {
			QueryBuilder<ComfData, Integer> builder = getComfDataDao().queryBuilder();
			builder.limit(number);
			builder.orderBy("id", false);  // true for ascending, false for descending
			list = getComfDataDao().query(builder.prepare());  // returns list of ten items
		} catch (SQLException e){
			System.out.println(e);
		}
		return list;
	}

	public List<ComfData> getLasComfDataVote(int number){
		List<ComfData> list=null;
		try {
			QueryBuilder<ComfData, Integer> builder = getComfDataDao().queryBuilder();
			Where<ComfData, Integer> where = builder.where();
			where.eq("dataType", "1");
			builder.limit(number);
			builder.orderBy("id", false);  // true for ascending, false for descending
			list = getComfDataDao().query(builder.prepare());  // returns list of ten items
		} catch (SQLException e){
			System.out.println(e);
		}
		return list;
	}

}
