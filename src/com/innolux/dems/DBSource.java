package com.innolux.dems;
import java.sql.SQLException;

import com.innolux.dems.DBConnector.ConnectionInfo;

public interface DBSource {
    public ConnectionInfo getConnection() throws SQLException;
    public void closeConnection(ConnectionInfo conn) throws SQLException;
}
