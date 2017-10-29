package com.innolux.dems.output;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;
import com.innolux.dems.DBConnector;
import com.innolux.dems.DBConnector.ConnectionInfo;
import com.innolux.dems.GlobleVar;
import com.innolux.dems.interfaces.MMSMsg;
import com.innolux.dems.interfaces.MMSParserInterface;
import com.innolux.dems.source.Tools;

public class UpdateResistStatus implements MMSParserInterface {

	private Logger logger = Logger.getLogger(this.getClass());
	private DBConnector BC2FBML100 = GlobleVar.BC2FBML100;
	private Tools tools = new Tools();

	@SuppressWarnings("resource")
	@Override
	public void onRvMsg(MMSMsg msg) {
		// TODO Auto-generated method stub

		String SQL = "";
		String eqpName = msg.EqpID;
		String status = msg.MaterialType;
		String resistID = msg.ResistID;
		String mainEqpID = msg.MainEqpID;
		ConnectionInfo conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		int numUpd;
		try {
			if (status.equals("UnMount")) {
				SQL = "delete cf_mtrl_status t where t.eqp_id = '" + mainEqpID + "' and t.subeqp_id = '" + eqpName
						+ "' and t.mtrl_id = '" + resistID + "'";
				logger.info(SQL);
				try {
					conn = BC2FBML100.getConnection();
					stmt = conn.conn.createStatement();
					stmt.executeUpdate(SQL);
				} catch (Exception e) {
					logger.error(tools.StackTrace2String(e));

				} /*
					 * finally { if (stmt != null) { try { stmt.close(); } catch
					 * (SQLException e) {
					 * logger.error(tools.StackTrace2String(e)); } } if (conn !=
					 * null) { try {
					 * 
					 * BC2FBML100.closeConnection(conn);
					 * 
					 * } catch (SQLException e) {
					 * logger.error(tools.StackTrace2String(e)); } } }
					 */
			} else {
				int rowCount = 0;
				try {
					//conn = BC2FBML100.getConnection();
					//stmt = conn.createStatement();
					SQL = "select * from cf_mtrl_status t where t.eqp_id = '" + mainEqpID + "' and t.subeqp_id = '"
							+ eqpName + "' and t.mtrl_id = '" + resistID + "'";
					logger.info(SQL);

					rs = stmt.executeQuery(SQL);

					while (rs.next()) {
						rowCount++;
					}
					rs.close();
				} catch (Exception e) {
					logger.error(tools.StackTrace2String(e));

				} /*
					 * finally { if (stmt != null) { try { stmt.close(); } catch
					 * (SQLException e) {
					 * logger.error(tools.StackTrace2String(e)); } } if (conn !=
					 * null) { try {
					 * 
					 * BC2FBML100.closeConnection(conn);
					 * 
					 * } catch (SQLException e) {
					 * logger.error(tools.StackTrace2String(e)); } } }
					 */

				if (rowCount != 0) {
					SQL = "update cf_mtrl_status t set t.status = '" + status
							+ "',t.updatetime = to_char(sysdate,'yyyy-mm-dd hh24:mi:ss') where t.eqp_id = '" + mainEqpID
							+ "' and t.subeqp_id = '" + eqpName + "' and t.mtrl_id = '" + resistID + "'";
					logger.info(SQL);
					try {
						//conn = BC2FBML100.getConnection();
						//stmt = conn.createStatement();
						numUpd = stmt.executeUpdate(SQL);
					} catch (Exception e) {
						logger.error(tools.StackTrace2String(e));

					} /*
						 * finally { if (stmt != null) { try { stmt.close(); }
						 * catch (SQLException e) {
						 * logger.error(tools.StackTrace2String(e)); } } if
						 * (conn != null) { try {
						 * 
						 * BC2FBML100.closeConnection(conn);
						 * 
						 * } catch (SQLException e) {
						 * logger.error(tools.StackTrace2String(e)); } } }
						 */
				} else {
					SQL = "insert into cf_mtrl_status t (eqp_id,subeqp_id,mtrl_id,status,updatetime) values('"
							+ mainEqpID + "','" + eqpName + "','" + resistID + "','" + status
							+ "',to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'))";
					logger.info(SQL);
					try {
						//conn = BC2FBML100.getConnection();
						//stmt = conn.createStatement();
						numUpd = stmt.executeUpdate(SQL);
					} catch (Exception e) {
						logger.error(tools.StackTrace2String(e));

					} /*
						 * finally { if (stmt != null) { try { stmt.close(); }
						 * catch (SQLException e) {
						 * logger.error(tools.StackTrace2String(e)); } } if
						 * (conn != null) { try {
						 * 
						 * BC2FBML100.closeConnection(conn);
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

					BC2FBML100.closeConnection(conn);

				} catch (SQLException e) {
					logger.error(tools.StackTrace2String(e));
				}
			}
		}
	}

}
