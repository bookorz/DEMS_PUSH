package com.innolux.dems.source;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.innolux.dems.DBConnector;
import com.innolux.dems.DBConnector.ConnectionInfo;
import com.innolux.dems.GlobleVar;
public class StateColor {
	private Logger logger = Logger.getLogger(this.getClass());
	private DBConnector DEMS = GlobleVar.DEMS;

	private Tools tools = new Tools();
	public String Color = "";
	public String State = "";
	public String Type = "";
	
	public Vector<StateColor> Get(){
		Vector<StateColor> result = new Vector<StateColor>();
		ConnectionInfo conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String SQL = "";
		try {
			conn = DEMS.getConnection();
			stmt = conn.conn.createStatement();
			
			
			SQL = "select * from dems_state_color t";
				
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);

			while (rs.next()) {
				StateColor each = new StateColor();
				each.Color = rs.getString("Color");
				each.Type = rs.getString("Type");
				each.State = rs.getString("State");
				result.add(each);
			}
			rs.close();
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
		return result;
	}
}
