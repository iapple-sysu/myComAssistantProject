package com.bjw.bean;

import java.io.Serializable;

/**
 * @author benjaminwan
 *���ڱ���������
 */
public class AssistBean implements Serializable{
	private static final long serialVersionUID = -5620661009186692227L;
	private boolean isTxt=true;
	private String SendTxtA="CTO1 F69401";
	private String SendHexA="";
	public String sTimeA="500";
	public boolean isTxt()
	{
		return isTxt;
	}
	public void setTxtMode(boolean isTxt)
	{
		this.isTxt = isTxt;
	}
	
	public String getSendA()
	{
		if (isTxt)
		{
			return SendTxtA;
		} else
		{
			return SendHexA;
		}
	}
	
	public void setSendA(String sendA)
	{
		if (isTxt)
		{
			SendTxtA = sendA;
		} else
		{
			SendHexA = sendA;
		}
	}
}