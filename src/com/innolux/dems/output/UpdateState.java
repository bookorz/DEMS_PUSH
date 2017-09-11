package com.innolux.dems.output;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.apache.log4j.Logger;
import com.innolux.dems.DBConnector;
import com.innolux.dems.GlobleVar;
import com.innolux.dems.interfaces.ItemState;
import com.innolux.dems.interfaces.UpdateInterface;
import com.innolux.dems.source.Tools;

public class UpdateState implements UpdateInterface {

	private Logger logger = Logger.getLogger(this.getClass());
	private DBConnector DEMS = GlobleVar.DEMS;

	private Tools tools = new Tools();

	@Override
	public void onRvMsg(Vector<ItemState> msg) {
		// TODO Auto-generated method stub
		Update2DB(msg);
	}

	private void Update2DB(Vector<ItemState> ItemList) {

		boolean isChange = false;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		int rowCount = 0;
		String SQL = "";

		try {
			conn = DEMS.getConnection();
			stmt = conn.createStatement();
			for (ItemState eachItem : ItemList) {
				
				ItemState old = GlobleVar.GetStatus(eachItem);
				if(!old.ItemState.equals(eachItem.ItemState)){
					GlobleVar.SetStatus(eachItem);
				}else{
					continue;
				}
				SQL = "select * from dems_current_state t where t.fab = '" + eachItem.Fab + "' and t.item_name = '"
						+ eachItem.ItemName + "'";
				logger.debug(SQL);

				rs = stmt.executeQuery(SQL);

				logger.debug(SQL);
				while (rs.next()) {
					rowCount++;
					if (eachItem.ItemState == null) {
						eachItem.ItemState = "";
					}
					if (!eachItem.ItemState.equals(rs.getString("item_state"))) {
						isChange = true;
					}
				}
				

				if (rowCount != 0) {
					if (isChange) {
						if (eachItem.ItemStateUpdateTime.equals("")) {
							SQL = "update dems_current_state t set t.item_state='" + eachItem.ItemState
									+ "',t.ITEM_STATE_UPDATETIME=sysdate,t.updatetime=sysdate where t.fab = '"
									+ eachItem.Fab + "' and t.item_name = '" + eachItem.ItemName + "'";
						} else {
							eachItem.ItemStateUpdateTime = eachItem.ItemStateUpdateTime.substring(0, 15);
							SQL = "update dems_current_state t set t.item_state='" + eachItem.ItemState
									+ "',t.ITEM_STATE_UPDATETIME=to_date('" + eachItem.ItemStateUpdateTime
									+ "','yyyymmdd hh24miss'),t.updatetime=sysdate where t.fab = '" + eachItem.Fab
									+ "' and t.item_name = '" + eachItem.ItemName + "'";
						}
						logger.debug(SQL);
						stmt.executeUpdate(SQL);
						
					}
				} else {

					if (eachItem.ItemStateUpdateTime.equals("")) {
						SQL = "insert into dems_current_state (fab,item_name,item_state) values('" + eachItem.Fab
								+ "','" + eachItem.ItemName + "','" + eachItem.ItemState + "')";
					} else {
						SQL = "insert into dems_current_state (fab,item_name,item_state,ITEM_STATE_UPDATETIME) values('"
								+ eachItem.Fab + "','" + eachItem.ItemName + "','" + eachItem.ItemState + "',to_date('"
								+ eachItem.ItemStateUpdateTime + "','yyyymmdd hh24miss'))";
					}
					logger.debug(SQL);
					
					stmt.executeUpdate(SQL);
					
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
	}

}
