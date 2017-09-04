package com.innolux.dems.output;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;
import com.innolux.dems.DBConnector;
import com.innolux.dems.GlobleVar;
import com.innolux.dems.interfaces.ItemState;
import com.innolux.dems.interfaces.ParserInterface;
import com.innolux.dems.source.Tools;

public class UpdateStatus implements ParserInterface {

	private Logger logger = Logger.getLogger(this.getClass());
	private DBConnector DEMS = GlobleVar.DEMS;
	private Tools tools = new Tools();

	public UpdateStatus() {

	}

	@Override
	public void onRvMsg(ItemState msg) {
		// TODO Auto-generated method stub
		Update2DB(msg);
	}

	public void Update2DB(ItemState eachItem) {

		String SQL = "";
		// if(Name.equals("ALERT")){
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		int rowCount = 0;
		boolean isChange = false;
		try {
			conn = DEMS.getConnection();
			stmt = conn.createStatement();
			SQL = "select * from dems_current_state t where t.fab = '" + eachItem.Fab + "' and t.item_name = '"
					+ eachItem.ItemName + "'";
			logger.debug(SQL);
			rs = stmt.executeQuery(SQL);

			while (rs.next()) {
				rowCount++;
				if (!eachItem.UpdateValue.equals(rs.getString(eachItem.UpdateType))) {
					isChange = true;
				}
			}
			/*
			 * } catch (Exception e) { logger.error(tools.StackTrace2String(e));
			 * 
			 * } finally { if (stmt != null) { try { stmt.close(); } catch
			 * (SQLException e) { logger.error(tools.StackTrace2String(e)); } }
			 * if (conn != null) { try {
			 * 
			 * DEMS.closeConnection(conn);
			 * 
			 * } catch (SQLException e) {
			 * logger.error(tools.StackTrace2String(e)); } } }
			 */

			if (rowCount != 0) {
				if (isChange) {
					SQL = "update dems_current_state t set t." + eachItem.UpdateType + " = '" + eachItem.UpdateValue
							+ "',t.updatetime=sysdate,t." + eachItem.UpdateType
							+ "_updatetime=sysdate where t.item_name = '" + eachItem.ItemName + "'";
					logger.debug(SQL);
					// try {
					//conn = DEMS.getConnection();
					//stmt = conn.createStatement();
					stmt.executeUpdate(SQL);
					/*
					 * } catch (Exception e) {
					 * logger.error(tools.StackTrace2String(e));
					 * 
					 * } finally { if (stmt != null) { try { stmt.close(); }
					 * catch (SQLException e) {
					 * logger.error(tools.StackTrace2String(e)); } } if (conn !=
					 * null) { try {
					 * 
					 * DEMS.closeConnection(conn);
					 * 
					 * } catch (SQLException e) {
					 * logger.error(tools.StackTrace2String(e)); } } }
					 */
				}
			}

		} catch (Exception e) {
			logger.error(tools.StackTrace2String(e));

		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					logger.error(tools.StackTrace2String(e));
				}
			}
			if (conn != null) {
				try {

					DEMS.closeConnection(conn);

				} catch (SQLException e) {
					logger.error(tools.StackTrace2String(e));
				}
			}
		}
		// }

	}

}
