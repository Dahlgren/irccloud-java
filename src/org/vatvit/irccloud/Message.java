package org.vatvit.irccloud;

import org.json.simple.JSONObject;

public class Message {
	private Connection connection;
	private String from;
	private String chan;
	private String msg;
	private long time;
	private long cid;

	public Message(Connection conn, JSONObject object) {
		this.connection = conn;
		this.from = (String)object.get("from");
		this.chan = (String)object.get("chan");
		this.msg = (String)object.get("msg");
		this.time = (Long)object.get("time");
		this.cid = (Long)object.get("cid");
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getChan() {
		return chan;
	}

	public void setChan(String chan) {
		this.chan = chan;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getCid() {
		return cid;
	}

	public void setCid(long cid) {
		this.cid = cid;
	}
	
	
}
