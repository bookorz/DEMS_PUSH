package com.innolux.dems.source;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import com.innolux.dems.DBConnector;
import com.innolux.dems.interfaces.ItemState;
import com.innolux.dems.interfaces.UpdateInterface;

public class BCData extends Thread {
	private Logger logger = Logger.getLogger(this.getClass());
	private Hashtable<String, String> Events = new Hashtable<String, String>();
	private Hashtable<String, String> WIP = new Hashtable<String, String>();
	private Hashtable<String, List<String>> GlassList = new Hashtable<String, List<String>>();
	private Hashtable<String, String> LineInfo = new Hashtable<String, String>();
	public Vector<String> SubEQPList = new Vector<String>();
	DBConnector BCDB = null;
	public String BCName = "";
	public String BCIP = "";
	public String Fab = "";
	public Vector<FunctionAttribute> PushEventList = new Vector<FunctionAttribute>();
	private Tools tools = new Tools();

	private UpdateInterface sourceObj;

	public BCData(String _BCName, String _BCIP, String _Fab, UpdateInterface _sourceObj) {
		BCName = _BCName;

		BCIP = _BCIP;

		Fab = _Fab;

		BCDB = new DBConnector("jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS_LIST =(ADDRESS = (PROTOCOL = TCP)(HOST = "
				+ _BCIP + ")(PORT = 1521)))(CONNECT_DATA =(SERVICE_NAME =ORCL)))", "innolux", "innoluxabc123", 1);
		sourceObj = _sourceObj;
	}

	public void run() {
		while (true) {
			Vector<ItemState> ItemStateList = new Vector<ItemState>();
			try {
				updateEvent();
				updateWIPCount();
				updateGlassList();
				updateHostMode();
				updateInlineMode();
				updatePortInfo();
			

				for (String key : getLineInfoKeys()) {
					String value = getLineInfo(key);

					ItemState eachEqp = new ItemState();
					eachEqp.Fab = Fab;
					eachEqp.ItemName = key;
					eachEqp.ItemState = value;
					ItemStateList.add(eachEqp);

				}

				for (int i = 0; i < PushEventList.size(); i++) {
					FunctionAttribute eachEvent = PushEventList.get(i);
					for (int k = 0; k < SubEQPList.size(); k++) {
						String eachSubEQP = SubEQPList.get(k);
						String EventData = getEventData(eachSubEQP, eachEvent.EventName, eachEvent.SubKey);
						String ItemData = "";
						if (EventData.equals("")) {
							continue;
						}
						try {
							ItemData = EventData.substring(eachEvent.StartIdx, eachEvent.EndIdx);
						} catch (Exception e) {
							logger.error("run EventData.substring error:" + e.getMessage() + " ---- "
									+ eachEvent.EventName + eachEvent.SubKey + "-" + eachSubEQP + "("
									+ eachEvent.Position + "," + eachEvent.Lentgh + "):"
									+ getEventData(eachSubEQP, eachEvent.EventName, eachEvent.SubKey));
							continue;
						}
						ItemData = "" + Integer.parseInt(ItemData, 2);
						if (!eachEvent.StringMapping.containsKey("NUM")) {
							if (eachEvent.StringMapping.containsKey(ItemData)) {
								ItemData = eachEvent.StringMapping.get(ItemData);
							}
						}

						ItemState eachEqp = new ItemState();
						eachEqp.Fab = Fab;
						eachEqp.ItemName = eachSubEQP + "." + eachEvent.ItemName;
						eachEqp.ItemState = ItemData;

						ItemStateList.add(eachEqp);

						String WIPInfo = getWIP(eachSubEQP);

						eachEqp = new ItemState();
						eachEqp.Fab = Fab;
						eachEqp.ItemName = eachSubEQP + ".WIP";
						eachEqp.ItemState = WIPInfo;

						ItemStateList.add(eachEqp);

						String GlassList = getGlass(eachSubEQP);

						eachEqp = new ItemState();
						eachEqp.Fab = Fab;
						eachEqp.ItemName = eachSubEQP + ".GLASS";
						eachEqp.ItemState = GlassList;
						ItemStateList.add(eachEqp);
						

					}
				}
				
				sourceObj.onRvMsg(ItemStateList);

				try {
					Thread.sleep(600000);
				} catch (Exception e) {
					logger.error("run EventData.sleep error:" + tools.StackTrace2String(e));
				}
			} catch (Exception e) {
				logger.error("BC Refresh data error, exception=" + tools.StackTrace2String(e));
				try {
					Thread.sleep(600000);
				} catch (Exception e1) {
					logger.error("run EventData.sleep error:" + tools.StackTrace2String(e1));
				}
			}
		}

	}

	public void ConePushEventList(Vector<FunctionAttribute> source) {
		for (FunctionAttribute each : source) {
			FunctionAttribute newFuncAttr = new FunctionAttribute();
			newFuncAttr.EndIdx = each.EndIdx;
			newFuncAttr.EventName = each.EventName;
			newFuncAttr.ItemName = each.ItemName;
			newFuncAttr.Lentgh = each.Lentgh;
			newFuncAttr.Position = each.Position;
			newFuncAttr.StartIdx = each.StartIdx;
			newFuncAttr.StringMapping = each.StringMapping;
			newFuncAttr.SubKey = each.SubKey;

			PushEventList.add(newFuncAttr);
		}
	}

	public void updateGlassList() {

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		String SQL = "";
		try {
			conn = BCDB.getConnection();
			stmt = conn.createStatement();

			SQL = "select t.hostsubeqid subeqid, t.glassid glass" + "  from wipdata t"
					+ " where t.updatetime > to_char(sysdate - 10, 'yyyy-mm-dd') And InUseFlag = 1";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);

			while (rs.next()) {
				synchronized (GlassList) {

					if (GlassList.containsKey(rs.getString("subeqid"))) {

						List<String> tmp = GlassList.get(rs.getString("subeqid"));
						if (tmp.size() < 30) {
							tmp.add(rs.getString("glass"));
						}
					} else {
						List<String> tmp = new ArrayList<String>();
						tmp.add(rs.getString("glass"));
						GlassList.put(rs.getString("subeqid"), tmp);
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

					BCDB.closeConnection(conn);

				} catch (SQLException e) {
					logger.error(tools.StackTrace2String(e));
				}
			}
		}

	}

	public void updateWIPCount() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		String SQL = "";
		try {
			conn = BCDB.getConnection();
			stmt = conn.createStatement();

			SQL = "select subeqid, WMSYS.WM_CONCAT(count)count" + " from (select t.hostsubeqid subeqid,"
					+ "             nvl2(t.currentcstid,"
					+ "'[' || t.currentcstid ||'(' || substr(t.hostportid,instr(t.hostportid,'.')+1) || ')]=' ||"
					+ "                  to_char(count(t.hostsubeqid))," + "                  case"
					+ "                    when t.bufferno = '0' then"
					+ "                     '[IN EQP]=' || to_char(count(t.hostsubeqid))" + "                    else"
					+ "                     '[BUFFER(' || to_char(t.bufferno) || ')]=' ||"
					+ "                     to_char(count(t.hostsubeqid))" + "                  end) count"
					+ "        from wipdata t"
					+ "       where t.updatetime > to_char(sysdate - 100, 'yyyy-mm-dd') And InUseFlag = 1"
					+ "       group by t.hostsubeqid, t.bufferno, t.currentcstid,t.hostportid)" + " group by subeqid";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);
			
			while (rs.next()) {
				synchronized (WIP) {

					if (WIP.containsKey(rs.getString("subeqid"))) {
						if (!WIP.get(rs.getString("subeqid")).equals(rs.getString("count"))) {
							WIP.remove(rs.getString("subeqid"));
							WIP.put(rs.getString("subeqid"), rs.getString("count"));

						}

					} else {
						WIP.put(rs.getString("subeqid"), rs.getString("count"));
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

					BCDB.closeConnection(conn);

				} catch (SQLException e) {
					logger.error(tools.StackTrace2String(e));
				}
			}
		}

	}

	private void getSubEqpID() {
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		String SQL = "";
		try {
			conn = BCDB.getConnection();
			stmt = conn.createStatement();

			SQL = "select node.hostsubeqid subeqid from main_bc_line line,main_bc_node node where line.bcno = node.bcno and line.bclineno = node.bclineno and line.fabtype = node.fabtype and line.hostlineid='"
					+ BCName + "'";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);
			
			while (rs.next()) {
				synchronized (SubEQPList) {
					SubEQPList.add(rs.getString("subeqid"));

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

					BCDB.closeConnection(conn);

				} catch (SQLException e) {
					logger.error(tools.StackTrace2String(e));
				}
			}
		}

	}

	public void updatePortInfo() {
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		String SQL = "";
		try {
			conn = BCDB.getConnection();
			stmt = conn.createStatement();

			SQL = "select node.hostsubeqid || '.' || port.hostportid || '.CSTID' subeqpid,"
					+ "      to_char(substr(plc.outputdata, (port.bcportno - 1) * 20 + 301, 20)) state"
					+ " from main_bc_line t, main_bc_node node, main_bc_port port, plcdata plc" + " where t.hostlineid = '"
					+ BCName + "'" + "  and t.bcno = port.bcno" + "  and t.bclineno = port.bclineno"
					+ "  and t.fabtype = port.fabtype" + "  and t.bcno = plc.bcno" + "  and t.bclineno = plc.bclineno"
					+ "  and t.fabtype = plc.fabtype" + "  and t.bcno = node.bcno" + "  and t.bclineno = node.bclineno"
					+ "  and t.fabtype = node.fabtype" + "  and port.nodeno = node.nodeno" + "  and plc.devicetype = '1'";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);
			
			while (rs.next()) {
				String key = rs.getString("subeqpid");
				String value = rs.getString("state").trim();
				synchronized (LineInfo) {
					if (LineInfo.containsKey(key)) {
						LineInfo.remove(key);
						LineInfo.put(key, value);
					} else {
						LineInfo.put(key, value);
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

					BCDB.closeConnection(conn);

				} catch (SQLException e) {
					logger.error(tools.StackTrace2String(e));
				}
			}
		}


	}

	public void updateHostMode() {
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		String SQL = "";
		try {
			conn = BCDB.getConnection();
			stmt = conn.createStatement();

			SQL = "select t.hostlineid || '.HostMode' subeqpid," + "      case"
					+ "        when to_char(substr(plc.outputdata, 28, 1)) = '0' then" + "         'Off Line'"
					+ "        when to_char(substr(plc.outputdata, 28, 1)) = '1' then" + "         'Online Control'"
					+ "        else" + "         'Online Monitor'" + "      end state" + " from main_bc_line t, plcdata plc"
					+ " where t.hostlineid = '" + BCName + "'" + "  and t.bcno = plc.bcno"
					+ "  and t.bclineno = plc.bclineno" + "  and t.fabtype = plc.fabtype" + "  and plc.devicetype = '1'";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);
			
			while (rs.next()) {
				String key = rs.getString("subeqpid");
				String value = rs.getString("state");
				synchronized (LineInfo) {
					if (LineInfo.containsKey(key)) {
						LineInfo.remove(key);
						LineInfo.put(key, value);
					} else {
						LineInfo.put(key, value);
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

					BCDB.closeConnection(conn);

				} catch (SQLException e) {
					logger.error(tools.StackTrace2String(e));
				}
			}
		}

	}

	public void updateInlineMode() {
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		String SQL = "";
		try {
			conn = BCDB.getConnection();
			stmt = conn.createStatement();

			SQL = "select t.hostlineid || '.InlineMode' subeqpid," + "      case"
					+ "        when to_char(substr(plc.outputdata, 26, 1)) = '0' then" + "         'Normal'"
					+ "        when to_char(substr(plc.outputdata, 26, 1)) = '1' then" + "         'Sort'"
					+ "        when to_char(substr(plc.outputdata, 26, 1)) = '2' then" + "         'Dummy Laser Mode'"
					+ "        when to_char(substr(plc.outputdata, 26, 1)) = '3' then" + "         'RUB Single Product'"
					+ "        when to_char(substr(plc.outputdata, 26, 1)) = '4' then"
					+ "         'RUB Double Product Single Dummy'"
					+ "        when to_char(substr(plc.outputdata, 26, 1)) = '5' then"
					+ "         'RUB Double Product Double Dummy'"
					+ "        when to_char(substr(plc.outputdata, 26, 1)) = '6' then" + "         'Cycle Exchange'"
					+ "        when to_char(substr(plc.outputdata, 26, 1)) = '7' then" + "         'Traffic'"
					+ "        else" + "         'NonDefine'" + "      end state" + " from main_bc_line t, plcdata plc"
					+ " where t.hostlineid = '" + BCName + "'" + "  and t.bcno = plc.bcno"
					+ "  and t.bclineno = plc.bclineno" + "  and t.fabtype = plc.fabtype" + "  and plc.devicetype = '1'";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);
			
			while (rs.next()) {
				String key = rs.getString("subeqpid");
				String value = rs.getString("state");
				synchronized (LineInfo) {
					if (LineInfo.containsKey(key)) {
						LineInfo.remove(key);
						LineInfo.put(key, value);
					} else {
						LineInfo.put(key, value);
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

					BCDB.closeConnection(conn);

				} catch (SQLException e) {
					logger.error(tools.StackTrace2String(e));
				}
			}
		}

	}

	public void updateEvent() {
		getSubEqpID();
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String plcData = "";
		
		String SQL = "";
		
		
		
		try {
			conn = BCDB.getConnection();
			stmt = conn.createStatement();

			SQL = "select plc.outputdata plcdata"
					+"  from plcdata plc, main_bc_line t"
					+" where t.hostlineid = '"+BCName+"'"
					+"   and t.bcno = plc.bcno"
					+"   and t.bclineno = plc.bclineno"
					+"   and t.fabtype = plc.fabtype"
					+"   and plc.devicetype = '2'";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);
			
			while (rs.next()) {
				plcData = rs.getString("plcdata");
				
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

					BCDB.closeConnection(conn);

				} catch (SQLException e) {
					logger.error(tools.StackTrace2String(e));
				}
			}
		}
		
		if(plcData.equals("")){
			logger.error("PLC data is empty");
			return;
		}
		
		
		try {
			conn = BCDB.getConnection();
			stmt = conn.createStatement();

			SQL = "select node.hostsubeqid,"
					+"       evt.funckey,"
					+"       evt.subfunckey,"
					+"       evt.startadr,"
					+"       evt.datalen"
					+"  from main_bc_line t, io_event evt, main_bc_node node"
					+" where t.hostlineid = '"+BCName+"'"
					+"   and t.bcno = evt.bcno"
					+"   and t.bclineno = evt.bclineno"
					+"   and t.fabtype = evt.fabtype"
					+"   and t.bcno = node.bcno"
					+"   and t.bclineno = node.bclineno"
					+"   and t.fabtype = node.fabtype"
					+"   and evt.nodeno = node.nodeno";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);
			
			while (rs.next()) {
				String key = rs.getString("hostsubeqid") + rs.getString("funckey") + rs.getString("subfunckey");
				int start = new BigInteger(rs.getString("startadr"), 16).intValue()*4;
				int len = rs.getInt("datalen")*4;
				
				String HexPlcData = plcData.substring(start, start+len) ;
				int binLegth = HexPlcData.length() * 4;

				String BinPlcData = new BigInteger(HexPlcData, 16).toString(2);

				if (binLegth != BinPlcData.length()) {
					String formatStr = "%0" + (binLegth - BinPlcData.length()) + "d";
					String formatAns = String.format(formatStr, 0);
					BinPlcData = formatAns + BinPlcData;
				}
				synchronized (Events) {
					if (Events.containsKey(key)) {
						Events.remove(key);
						Events.put(key, BinPlcData);
					} else {
						Events.put(key, BinPlcData);
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

					BCDB.closeConnection(conn);

				} catch (SQLException e) {
					logger.error(tools.StackTrace2String(e));
				}
			}
		}


	}

	public String getEventData(String subEqp, String eventName, String eventSubName) {
		String key = subEqp + eventName + eventSubName;
		String result = "";
		synchronized (Events) {
			if (Events.containsKey(key)) {
				result = Events.get(key);
			}
		}
		return result;
	}

	public String getWIP(String subEqp) {
		String key = subEqp;
		String result = "";
		synchronized (WIP) {
			if (WIP.containsKey(key)) {
				result = WIP.get(key);
			}
		}
		return result;
	}

	public String getGlass(String subEqp) {
		String key = subEqp;
		String result = " ";
		synchronized (GlassList) {

			if (GlassList.containsKey(key)) {
				List<String> tmp = GlassList.get(key);
				for (String each : tmp) {
					if (result == null) {
						result = "";
					}
					if (result.equals("")) {
						result = each;
					} else {
						result += "," + each;
					}

				}
			}
		}
		return result;
	}

	public Vector<String> getSubEqpList() {
		// Vector<String> result = new Vector<String>();
		// synchronized (SubEQPList) {
		//
		// for (String each : SubEQPList) {
		// result.add(each);
		// }
		//
		// }
		return SubEQPList;

	}

	public Set<String> getLineInfoKeys() {
		return LineInfo.keySet();
	}

	public String getLineInfo(String key) {

		String result = "";
		synchronized (LineInfo) {
			if (LineInfo.containsKey(key)) {
				result = LineInfo.get(key);
			}
		}
		return result;
	}
}
