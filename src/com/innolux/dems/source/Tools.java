package com.innolux.dems.source;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Tools {
	public String StackTrace2String(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString(); // stack trace as a string
	}
}
