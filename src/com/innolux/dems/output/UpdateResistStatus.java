package com.innolux.dems.output;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.innolux.dems.DBConnector;
import com.innolux.dems.interfaces.CallBackInterface;

public class UpdateResistStatus implements CallBackInterface {

	private Logger logger = Logger.getLogger(this.getClass());
	private DBConnector BC2FBML100 = new DBConnector(
			"jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS_LIST =(ADDRESS = (PROTOCOL = TCP)(HOST =172.20.36.131)(PORT = 1521)))(CONNECT_DATA =(SERVICE_NAME =ORCL)))",
			"innolux", "innoluxabc123");

	@Override
	public void onRvMsg(String jsonMsg) {
		// TODO Auto-generated method stub
		try {
			JSONObject revJson = new JSONObject(jsonMsg);

			// String fab = revJson.getString("fab");
			String eqpName = revJson.getString("eqpName");
			String status = revJson.getString("status");
			String resistID = revJson.getString("resistID");
			String mainEqpID = revJson.getString("mainEqpID");
			ResultSet rs = null;
			if (status.equals("UnMount")) {
			 	String SQL = "delete cf_mtrl_status t where t.eqp_id = '" + mainEqpID + "' and t.subeqp_id = '" + eqpName + "' and t.mtrl_id = '" + resistID + "'";
				logger.info(SQL);
				BC2FBML100.Execute(SQL);
			} else {
				int rowCount = 0;
				String SQL = "select * from cf_mtrl_status t where t.eqp_id = '" + mainEqpID + "' and t.subeqp_id = '"
						+ eqpName + "' and t.mtrl_id = '" + resistID + "'";
				logger.info(SQL);
				rs = BC2FBML100.Query(SQL);

				try {
					while (rs.next()) {
						rowCount++;

					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error("Functon:CheckPalletID rs.next() error, exception=" + e.getMessage());
				} finally {
					try {
						rs.getStatement().close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
					}
				}

				if (rowCount != 0) {
					SQL = "update cf_mtrl_status t set t.status = '" + status
							+ "',t.updatetime = to_char(sysdate,'yyyy-mm-dd hh24:mi:ss') where t.eqp_id = '" + mainEqpID
							+ "' and t.subeqp_id = '" + eqpName + "' and t.mtrl_id = '" + resistID + "'";
					logger.info(SQL);
					BC2FBML100.Execute(SQL);
				} else {
					SQL = "insert into cf_mtrl_status t (eqp_id,subeqp_id,mtrl_id,status,updatetime) values('"
							+ mainEqpID + "','" + eqpName + "','" + resistID + "','" + status
							+ "',to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'))";
					logger.info(SQL);
					BC2FBML100.Execute(SQL);
				}
			}

		} catch (Exception e) {
			logger.error("UpdateResistStatus error: " + e.getMessage() + " jsonMsg:" + jsonMsg);
		}

	}

}
