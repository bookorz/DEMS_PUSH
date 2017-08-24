package com.innolux.dems.interfaces;

public class ItemState {
	public String Fab = "";
	public String ItemName = "";
	public String ItemState = "";
	public String ItemStateUpdateTime = "";

	public String ItemType = "";
	public String ItemMode = "";
	public String UpdateType = "";
	public String UpdateValue = "";

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{Fab:" + Fab + "," + "ItemName:" + ItemName + "," + "ItemState:"
				+ ItemState + "," + "ItemStateUpdateTime:" + ItemStateUpdateTime + "," + "ItemType:" + ItemType + ","
				+ "ItemMode:" + ItemMode + "," + "UpdateType:" + UpdateType + "," + "UpdateValue:" + UpdateValue
				+ "}";
	}
}
