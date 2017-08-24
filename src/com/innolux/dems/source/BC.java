package com.innolux.dems.source;

import com.innolux.dems.output.UpdateState;

public class BC {
	public String BCName="";
	public String IPAdress="";
	public BCData BCInfo = null;
	public BC(String _BCName,String _IPAdress,String Fab){
		BCName = _BCName;
		IPAdress = _IPAdress;
		BCInfo = new BCData(BCName,IPAdress,Fab,new UpdateState());
	}
	
}
