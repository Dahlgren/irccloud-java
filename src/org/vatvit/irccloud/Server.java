package org.vatvit.irccloud;

import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.vatvit.irccloud.events.EventListener;
import org.vatvit.irccloud.events.ServerListener;

public class Server {
	private Connection connection;
	private String name;
	private String nick;
	private String nickservNick;
	private String nickservPass;
	private String realname;
	private String hostname;
	private long port;
	private String away;
	private boolean disconnected;
	private boolean ssl;
	private String serverPass;
	private long cid;
	
	private ArrayList<ServerListener> listeners = new ArrayList<ServerListener>();
	private ArrayList<Channel> channels = new ArrayList<Channel>();
	private ArrayList<Private> privates = new ArrayList<Private>();
	

	public Server(Connection conn, JSONObject object) {
		this.connection = conn;
		this.cid = (Long)object.get("cid");
		this.name = (String)object.get("name");
		this.nick = (String)object.get("nick");
		this.nickservNick = (String)object.get("nickserv_nick");
		this.realname = (String)object.get("realname");
		this.hostname = (String)object.get("hostname");
		this.port = (Long)object.get("port");
		this.away = (String)object.get("away");
		this.disconnected = (Boolean)object.get("disconnected");
		this.ssl = (Boolean)object.get("ssl");
		this.serverPass = (String)object.get("server_pass");
		initListeners();
	}
	
	private void initListeners() {
		final Server self = this;
		this.connection.addEventListener("channel_init", new EventListener(){
			public void onEvent(JSONObject event) {
				if((Long)event.get("cid") == cid) {
					Channel channel = new Channel(connection, event);
					channels.add(channel);
					newChannel(channel);
				}
			}
		});
		
		//you_parted_channel
		this.connection.addEventListener("you_parted_channel", new EventListener(){
			public void onEvent(JSONObject event) {
				long ecid = 0;
				ecid = (Long)event.get("cid");
				if(ecid != self.cid) {
					return;
				}
				String chan;
				chan = (String)event.get("chan");
				for(Channel channel : channels) {
					if(channel.getName().equalsIgnoreCase(chan)) {
						channels.remove(channel);
						channelRemoved(channel);
						return;
					}
				}
			}
		});
		
		this.connection.addEventListener("makebuffer", new EventListener(){
			public void onEvent(JSONObject event) {
				
			}
		});
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getNickservNick() {
		return nickservNick;
	}

	public void setNickservNick(String nickservNick) {
		this.nickservNick = nickservNick;
	}

	public String getNickservPass() {
		return nickservPass;
	}

	public void setNickservPass(String nickservPass) {
		this.nickservPass = nickservPass;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public long getPort() {
		return port;
	}

	public void setPort(long port) {
		this.port = port;
	}

	public String getAway() {
		return away;
	}

	public void setAway(String away) {
		this.away = away;
	}

	public boolean isDisconnected() {
		return disconnected;
	}

	public void setDisconnected(boolean disconnected) {
		this.disconnected = disconnected;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public String getServerPass() {
		return serverPass;
	}

	public void setServerPass(String serverPass) {
		this.serverPass = serverPass;
	}

	public long getCid() {
		return cid;
	}

	public void setCid(long cid) {
		this.cid = cid;
	}

	public ArrayList<Channel> getChannels() {
		return channels;
	}

	public void setChannels(ArrayList<Channel> channels) {
		this.channels = channels;
	}

	public ArrayList<Private> getPrivates() {
		return privates;
	}

	public void setPrivates(ArrayList<Private> privates) {
		this.privates = privates;
	}

	@Override
	public String toString() {
		return name;
	}
	
	public void addServerListener(ServerListener listener) {
		listeners.add(listener);
	}
	
	public void removeServerListener(ServerListener listener) {
		listeners.remove(listener);
	}
	
	public ArrayList<ServerListener> getServerListeners() {
		return listeners;
	}

	public void setServerListeners(ArrayList<ServerListener> serverListeners) {
		listeners = serverListeners;
	}
	
	private void newChannel(Channel channel) {
		for(ServerListener listener : this.listeners) {
			listener.newChannel(channel);
		}
	}
	private void channelRemoved(Channel channel) {
		for(ServerListener listener : this.listeners) {
			listener.channelRemoved(channel);
		}
	}
	
}
