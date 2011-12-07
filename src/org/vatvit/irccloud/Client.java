package org.vatvit.irccloud;

import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.vatvit.irccloud.events.EventListener;
import org.vatvit.irccloud.events.ServerListener;
import org.vatvit.irccloud.events.ServersListener;


public class Client {
	private Connection connection;
	private String name;
	private String email;
	private ArrayList<Server> servers = new ArrayList<Server>();
	private ArrayList<ServersListener> serverListeners = new ArrayList<ServersListener>();
	
	
	public Client() {
		
	}
	
	private void initListeners() {
		final Client self = this;
		this.connection.addEventListener("stat_user", new EventListener(){
			public void onEvent(JSONObject event) {
				self.name = (String)event.get("name");
				self.email = (String)event.get("email");
			}
		});
		
		this.connection.addEventListener("oob_include", new EventListener() {
			public void onEvent(JSONObject event) {
				//{"bid":-1,"eid":-1,"type":"oob_include","time":1323264089,"highlight":false,"url":"\/chat\/oob-loader?key=7ae0b85c-21bb-411e-b932-4957f642d226"}
				connection.readOobInclude(event);
			}
		});
		
		this.connection.addEventListener("makeserver", new EventListener(){
			public void onEvent(JSONObject event) {
				Server server = new Server(connection, event);
				server.addServerListener(new ServerListener(){
					@Override
					public void newChannel(Channel channel) {
						update();
					}

					@Override
					public void channelRemoved(Channel channel) {
						update();
					}
					
				});
				servers.add(server);
				connectedToServer(server);
			}
		});
		//connection_deleted
		this.connection.addEventListener("connection_deleted", new EventListener(){
			public void onEvent(JSONObject event) {
				int cid = 0;
				cid = (Integer)event.get("cid");
				Server server = null;
				for(Server serv : servers) {
					if(serv.getCid() == cid) {
						server = serv;
						break;
					}
				}
				servers.remove(server);
				if(server != null) {
					connectedToServer(server);
				}
			}
		});
	}
	
	public boolean login(String email, String password) {
		this.connection = new Connection(email, password);
		initListeners();
		return this.connection.login();
	}

	public boolean isLoggedIn() {
		return this.connection != null && this.connection.isConnected();
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public ArrayList<Server> getServers() {
		return servers;
	}

	public void setServers(ArrayList<Server> servers) {
		this.servers = servers;
	}

	public void addServerListener(ServersListener listener) {
		serverListeners.add(listener);
	}
	
	public void removeServerListener(ServersListener listener) {
		serverListeners.remove(listener);
	}
	
	public ArrayList<ServersListener> getServerListeners() {
		return serverListeners;
	}

	public void setServerListeners(ArrayList<ServersListener> serverListeners) {
		this.serverListeners = serverListeners;
	}
	
	private void connectedToServer(Server server) {
		for(ServersListener listener : this.serverListeners) {
			listener.connectedToServer(server);
		}
	}
	private void disconnectedFromServer(Server server) {
		for(ServersListener listener : this.serverListeners) {
			listener.disconnectedFromServer(server);
		}
	}
	private void update() {
		for(ServersListener listener : this.serverListeners) {
			listener.update();
		}
	}
	
}
