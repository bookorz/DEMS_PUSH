package com.innolux.dems.source;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.innolux.dems.DBConnector;

public class BCData {
	private Logger logger = Logger.getLogger(this.getClass());
	private Hashtable<String, String> Events = new Hashtable<String, String>();
	private Hashtable<String, String> EventChangeList = new Hashtable<String, String>();
	private Hashtable<String, String> WIP = new Hashtable<String, String>();
	private Hashtable<String, String> WIPChangeList = new Hashtable<String, String>();
	private Hashtable<String, String> LineInfo = new Hashtable<String, String>();
	private Hashtable<String, String> LineInfoChangeList = new Hashtable<String, String>();
	public Vector<String> SubEQPList = new Vector<String>();
	DBConnector BCDB = null;
	public String BCName = "";
	public String BCIP = "";

	public BCData(String _BCName, String _BCIP) {
		BCName = _BCName;

		BCIP = _BCIP;

		BCDB = new DBConnector("jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS_LIST =(ADDRESS = (PROTOCOL = TCP)(HOST = "
				+ _BCIP + ")(PORT = 1521)))(CONNECT_DATA =(SERVICE_NAME =ORCL)))", "innolux", "innoluxabc123");

	}

	// public void run() {
	// long lastTime = 0;
	// while (true) {
	// if (System.currentTimeMillis() - lastTime > 5000) {
	// this.updateEvent();
	// this.updateWIPCount();
	// lastTime = System.currentTimeMillis();
	// }
	// }
	// }

	public void updateWIPCount() {

		ResultSet rs = null;
		String SQL = "select subeqid, WMSYS.WM_CONCAT(count)count" + " from (select t.hostsubeqid subeqid,"
				+ "             nvl2(t.currentcstid,"
				+ "'[' || t.currentcstid ||'(' || substr(t.hostportid,instr(t.hostportid,'.')+1) || ')]=' ||"
				+ "                  to_char(count(t.hostsubeqid))," + "                  case"
				+ "                    when t.bufferno = '0' then"
				+ "                     '[IN EQP]=' || to_char(count(t.hostsubeqid))" + "                    else"
				+ "                     '[BUFFER(' || to_char(t.bufferno) || ')]=' ||"
				+ "                     to_char(count(t.hostsubeqid))" + "                  end) count"
				+ "        from wipdata t" + "       where t.updatetime > to_char(sysdate - 100, 'yyyy-mm-dd')"
				+ "       group by t.hostsubeqid, t.bufferno, t.currentcstid,t.hostportid)" + " group by subeqid";
		rs = BCDB.Query(SQL);

		try {
			EventChangeList.clear();
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
			// TODO Auto-generated catch block
			logger.error("getSubEqpID while (rs.next()) error, exception=" + e.getMessage());

		} finally {
			try {

				rs.getStatement().close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("getSubEqpID rs.close error :" + e.getMessage());
			}
		}

	}

	private void getSubEqpID() {
		ResultSet rs = null;
		String SQL = "select node.hostsubeqid subeqid from main_bc_line line,main_bc_node node where line.bcno = node.bcno and line.bclineno = node.bclineno and line.fabtype = node.fabtype and line.hostlineid='"
				+ BCName + "'";
		rs = BCDB.Query(SQL);
		try {
			while (rs.next()) {
				synchronized (SubEQPList) {
					SubEQPList.add(rs.getString("subeqid"));

				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("getSubEqpID while (rs.next()) error, exception=" + e.getMessage());

		} finally {
			try {

				rs.getStatement().close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("getSubEqpID rs.close error :" + e.getMessage());
			}
		}
	}

	public void updatePortInfo() {
		ResultSet rs = null;
		String SQL = "select node.hostsubeqid || '.' || port.hostportid || '.CSTID' subeqpid,"
				+ "      to_char(substr(plc.outputdata, (port.bcportno - 1) * 20 + 301, 20)) state"
				+ " from main_bc_line t, main_bc_node node, main_bc_port port, plcdata plc" + " where t.hostlineid = '"
				+ BCName + "'" + "  and t.bcno = port.bcno" + "  and t.bclineno = port.bclineno"
				+ "  and t.fabtype = port.fabtype" + "  and t.bcno = plc.bcno" + "  and t.bclineno = plc.bclineno"
				+ "  and t.fabtype = plc.fabtype" + "  and t.bcno = node.bcno" + "  and t.bclineno = node.bclineno"
				+ "  and t.fabtype = node.fabtype" + "  and port.nodeno = node.nodeno" + "  and plc.devicetype = '1'";

		rs = BCDB.Query(SQL);
		try {
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
			// TODO Auto-generated catch block
			logger.error("updatePortInfo while (rs.next()) error, exception=" + e.getMessage());

		} finally {
			try {

				rs.getStatement().close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("updatePortInfo rs.close error :" + e.getMessage());
			}
		}
	}

	public void updateHostMode() {
		ResultSet rs = null;
		String SQL = "select t.hostlineid || '.HostMode' subeqpid," + "      case"
				+ "        when to_char(substr(plc.outputdata, 28, 1)) = '0' then" + "         'Off Line'"
				+ "        when to_char(substr(plc.outputdata, 28, 1)) = '1' then" + "         'Online Control'"
				+ "        else" + "         'Online Monitor'" + "      end state" + " from main_bc_line t, plcdata plc"
				+ " where t.hostlineid = '" + BCName + "'" + "  and t.bcno = plc.bcno"
				+ "  and t.bclineno = plc.bclineno" + "  and t.fabtype = plc.fabtype" + "  and plc.devicetype = '1'";

		rs = BCDB.Query(SQL);
		try {
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
			// TODO Auto-generated catch block
			logger.error("updateHostMode while (rs.next()) error, exception=" + e.getMessage());

		} finally {
			try {

				rs.getStatement().close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("updateHostMode rs.close error :" + e.getMessage());
			}
		}
	}

	public void updateInlineMode() {
		ResultSet rs = null;
		String SQL = "select t.hostlineid || '.InlineMode' subeqpid," + "      case"
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

		rs = BCDB.Query(SQL);
		try {
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
			// TODO Auto-generated catch block
			logger.error("updateInlineMode while (rs.next()) error, exception=" + e.getMessage());

		} finally {
			try {

				rs.getStatement().close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("updateInlineMode rs.close error :" + e.getMessage());
			}
		}
	}

	public void updateEvent(/*
							 * Vector<String> subEqpList, Vector<String>
							 * eventList
							 */) {
		getSubEqpID();
		ResultSet rs = null;
		String SQL = "select node.hostsubeqid," + "evt.funckey," + "evt.subfunckey,"
				+ "to_char(substr(plc.outputdata,to_number(evt.startadr,'xxxxxxx')*4+1,evt.datalen*4))plcdata"
				+ " from main_bc_line t, io_event evt, main_bc_node node, plcdata plc" + " where t.hostlineid = '"
				+ BCName + "'" + " and t.bcno = evt.bcno" + " and t.bclineno = evt.bclineno"
				+ " and t.fabtype = evt.fabtype" + " and t.bcno = node.bcno" + " and t.bclineno = node.bclineno"
				+ " and t.fabtype = node.fabtype" + " and t.bcno = plc.bcno" + " and t.bclineno = plc.bclineno"
				+ " and t.fabtype = plc.fabtype" + " and plc.devicetype = '2'" + " and evt.nodeno = node.nodeno";
		// String tmpSubEqpList = "'";
		// for (int i = 0; i < subEqpList.size(); i++) {
		// if (i == 0) {
		// tmpSubEqpList += subEqpList.get(i);
		// } else {
		// tmpSubEqpList += "','" + subEqpList.get(i);
		// }
		// }
		// tmpSubEqpList += "'";
		// SQL += " and node.hostsubeqid in (" + tmpSubEqpList + ")";
		//
		// String tmpEventList = "'";
		// for (int i = 0; i < eventList.size(); i++) {
		// if (i == 0) {
		// tmpEventList += eventList.get(i);
		// } else {
		// tmpEventList += "','" + eventList.get(i);
		// }
		// }
		// tmpEventList += "'";
		// SQL += " and evt.funckey in (" + tmpEventList + ")";

		rs = BCDB.Query(SQL);
		try {
			while (rs.next()) {
				String key = rs.getString("hostsubeqid") + rs.getString("funckey") + rs.getString("subfunckey");
				String HexPlcData = rs.getString("plcdata");
				int binLegth = HexPlcData.length() * 4;

				String BinPlcData = new BigInteger(rs.getString("plcdata"), 16).toString(2);

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
			// TODO Auto-generated catch block
			logger.error("updateEvent while (rs.next()) error,BC=" + BCName + " exception=" + e.getMessage() + " SQL="
					+ SQL);

		} finally {
			try {

				rs.getStatement().close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("updateEvent rs.close error :" + e.getMessage());
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
