package com.innolux.dems.source;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.innolux.dems.DBConnector;
import com.innolux.dems.DBConnector.ConnectionInfo;
import com.innolux.dems.GlobleVar;

class FunctionAttribute {
	public String EventName = "";
	public String SubKey = "";
	public String ItemName = "";
	public String Position = "";
	public String Lentgh = "";
	public int StartIdx = 0;
	public int EndIdx = 0;
	public Hashtable<String, String> StringMapping = new Hashtable<String, String>();
}

public class BCCampsite {
	private Hashtable<String, BC> BCList = new Hashtable<String, BC>();
	private Logger logger = Logger.getLogger(this.getClass());
	private Vector<FunctionAttribute> PushEventList = new Vector<FunctionAttribute>();
	private Tools tools = new Tools();

	private String Fab = "";

	public BCCampsite(String _Fab) {

		Fab = _Fab;
		GetBC(Fab);
	}

	private void GetPushEventList() {

		try {
			String filePath = "PushEventList.json";
			FileReader FileStream = new FileReader(filePath);

			BufferedReader BufferedStream = new BufferedReader(FileStream);

			String data;
			// Vector<FunctionAttribute> FuncList = new
			// Vector<FunctionAttribute>();
			do {
				data = BufferedStream.readLine();
				if (data == null) {
					break;
				}

				try {

					JSONObject revJson = new JSONObject(data);

					String EventName = revJson.getString("EventName");
					String SubKey = revJson.getString("SubKey");
					JSONArray Items = revJson.getJSONArray("Items");
					for (int i = 0; i < Items.length(); i++) {
						FunctionAttribute eachFuncAttr = new FunctionAttribute();
						JSONObject eachItem = Items.getJSONObject(i);
						String FunctionName = eachItem.getString("FunctionName");
						String Position = eachItem.getString("Position");
						int startIdx = Integer.parseInt(Position);
						String Length = eachItem.getString("Length");
						int endIdx = startIdx + Integer.parseInt(Length);
						eachFuncAttr.ItemName = FunctionName;
						eachFuncAttr.Position = Position;
						eachFuncAttr.Lentgh = Length;
						eachFuncAttr.EventName = EventName;
						eachFuncAttr.SubKey = SubKey;
						eachFuncAttr.StartIdx = startIdx;
						eachFuncAttr.EndIdx = endIdx;
						JSONArray StringMappingList = eachItem.getJSONArray("StringMapping");
						for (int k = 0; k < StringMappingList.length(); k++) {
							JSONObject eachObject = StringMappingList.getJSONObject(k);

							String keyStr = eachObject.getString("Key");
							String valueStr = eachObject.getString("Value");
							eachFuncAttr.StringMapping.put(keyStr, valueStr);

						}
						PushEventList.add(eachFuncAttr);
					}

				} catch (Exception e1) {
					Tools tools = new Tools();
					logger.error(tools.StackTrace2String(e1));
				}

			} while (true);
			BufferedStream.close();

		} catch (Exception e) {
			Tools tools = new Tools();
			logger.error(tools.StackTrace2String(e));
		}
	}

	private void GetBC(String fab) {
		GetPushEventList();
		DBConnector DEMS = GlobleVar.DEMS;

		ConnectionInfo conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		String SQL = "";
		try {
			conn = DEMS.getConnection();
			stmt = conn.conn.createStatement();

			SQL = "select t.bc_name,t.ip from dems_bcip t where t.fab='" + fab + "'";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);

			while (rs.next()) {
				if (!BCList.containsKey(rs.getString("bc_name"))) {
					BC eachBC = new BC(rs.getString("bc_name"), rs.getString("ip"), fab);
					eachBC.BCInfo.ConePushEventList(PushEventList);
					eachBC.BCInfo.start();
					// eachBC.PLCData.updateEvent();
					BCList.put(rs.getString("bc_name"), eachBC);
				}
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

	}

}
