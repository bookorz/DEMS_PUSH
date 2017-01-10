package com.innolux.dems.source;

public class BC {
	public String BCName="";
	public String IPAdress="";
	public BCData BCInfo = null;
	public BC(String _BCName,String _IPAdress){
		BCName = _BCName;
		IPAdress = _IPAdress;
		BCInfo = new BCData(BCName,IPAdress);
	}
	
}
