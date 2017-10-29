package com.innolux.dems.output;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.apache.log4j.Logger;
import com.innolux.dems.DBConnector;
import com.innolux.dems.DBConnector.ConnectionInfo;
import com.innolux.dems.GlobleVar;
import com.innolux.dems.interfaces.ItemState;
import com.innolux.dems.interfaces.UpdateInterface;
import com.innolux.dems.source.Tools;

public class UpdateStatus implements UpdateInterface {

	private Logger logger = Logger.getLogger(this.getClass());
	private DBConnector DEMS = GlobleVar.DEMS;
	private Tools tools = new Tools();

	public UpdateStatus() {

	}

	@Override
	public void onRvMsg(Vector<ItemState> msg) {
		// TODO Auto-generated method stub
		Update2DB(msg);
	}

	public void Update2DB(Vector<ItemState> ItemList) {

		String SQL = "";
		// if(Name.equals("ALERT")){
		ConnectionInfo conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		int rowCount = 0;
		boolean isChange = false;
		int numUpd;
		try {
			conn = DEMS.getConnection();
			stmt = conn.conn.createStatement();
			for (ItemState eachItem : ItemList) {

				ItemState old = GlobleVar.GetStatus(eachItem);
				switch (eachItem.UpdateType) {
				case "Item_Alert":
					if (!old.ItemAlert.equals(eachItem.UpdateValue)) {
						old.ItemAlert = eachItem.UpdateValue;
						GlobleVar.SetStatus(old);
					} else {
						continue;
					}
					break;
				case "Item_Mode":
					if (!old.ItemMode.equals(eachItem.UpdateValue)) {
						old.ItemMode = eachItem.UpdateValue;
						GlobleVar.SetStatus(old);
					} else {
						continue;
					}
					break;
				}
				SQL = "select * from dems_current_state t where t.fab = '" + eachItem.Fab + "' and t.item_name = '"
						+ eachItem.ItemName + "'";
				logger.debug(SQL);
				rs = stmt.executeQuery(SQL);
				rowCount = 0;
				while (rs.next()) {
					rowCount++;
					if (!eachItem.UpdateValue.equals(rs.getString(eachItem.UpdateType))) {
						isChange = true;
					}
				}
				rs.close();

				if (rowCount != 0) {
					if (isChange) {
						SQL = "update dems_current_state t set t." + eachItem.UpdateType + " = '" + eachItem.UpdateValue
								+ "',t.updatetime=sysdate,t." + eachItem.UpdateType
								+ "_updatetime=sysdate where t.item_name = '" + eachItem.ItemName + "'";
						logger.debug(SQL);
						
						numUpd = stmt.executeUpdate(SQL);
						
					}
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
