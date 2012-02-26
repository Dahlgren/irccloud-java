package org.vatvit.irccloud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.vatvit.irccloud.events.EventListener;


public class Connection {
	private String email;
	private String password;
	private boolean connected;
	private JSONParser parser;
	private String session;
	private int reqCount=0;
	private HashMap<String, ArrayList<EventListener>> eventListeners = new HashMap<String, ArrayList<EventListener>>();


	private String hostUrl = "https://irccloud.com";
	private String streamUrl = "https://irccloud.com/chat/stream";
	private String actionUrl = "https://irccloud.com/chat/";
	
	public Connection(String email, String password) {
		this.email = email;
		this.password = password;
		this.connected = false;
		this.parser = new JSONParser();
	}

	public boolean login() {
		this.session = null;
		this.connected = false;
		String data = "";
		try {
			data = URLEncoder.encode("email", "UTF-8") + "="
					+ URLEncoder.encode(this.email, "UTF-8");
			data += "&" + URLEncoder.encode("password", "UTF-8") + "="
					+ URLEncoder.encode(this.password, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		URL loginURL = null;
		try {
			loginURL = new URL(actionUrl+"login");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpURLConnection loginConn = null;
		try {
			loginConn = (HttpURLConnection) loginURL.openConnection();

			loginConn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(
					loginConn.getOutputStream());
			wr.write(data);
			wr.flush();

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					loginConn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				//JSONObject response = new JSONObject(line);
				JSONObject response = (JSONObject)parser.parse(line);
				String session = (String)response.get("session");
				if (session != null) {
					this.session = session;
				}
			}

			wr.close();
			rd.close();

		} catch (IOException e) {
			try {
				if (loginConn.getResponseCode() == 400) {
					return false;
				}
			} catch (IOException e1) {
				e1.printStackTrace();
				return false;
			}
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}

		if (this.session != null) {
			this.connected = true;
		}

		// Start reading stream.
		readStream();
		
		return this.connected;
	}

	private void readStream() {
		final Connection self = this;
		if (this.connected) {
			(new Thread() {
				@Override
				public void run() {
					URL streamURL = null;
					try {
						streamURL = new URL(self.streamUrl);
					} catch (MalformedURLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						URLConnection streamConn = streamURL.openConnection();
						streamConn.addRequestProperty("Cookie", "session="+self.session);
						streamConn.connect();
						BufferedReader rd = new BufferedReader(new InputStreamReader(
								streamConn.getInputStream()));
						String line;
						while ((line = rd.readLine()) != null) {
							line = line.trim();
							if (line.length() <= 0) {
								continue;
							}
							try {
								JSONObject response = (JSONObject)parser.parse(line);
								self.onEvent(response);
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

						rd.close();
						connected = false;
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
		}
	}

	public void readOobInclude(JSONObject event) {
		final Connection self = this;
		final JSONObject _event = event;
		URL oobIncludeURL = null;
		try {
			oobIncludeURL = new URL(self.hostUrl+(String)_event.get("url"));
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			URLConnection oobIncludeConn = oobIncludeURL.openConnection();
			oobIncludeConn.addRequestProperty("Cookie", "session="+self.session);
			oobIncludeConn.connect();
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					oobIncludeConn.getInputStream()));
			String line;
			List<JSONObject> list = new ArrayList<JSONObject>();
			while ((line = rd.readLine()) != null) {
				line = line.trim();
				if (line.length() <= 0) {
					continue;
				}
				if (line.equals("[") || line.equals("]")) {
					continue;
				}
				if (line.charAt(line.length() - 1) == ',') {
					line = line.substring(0, line.length() - 1);
				}
				try {
					JSONObject response = (JSONObject)parser.parse(line);
					String type = (String)response.get("type");
					if (type.equals("makeserver") || type.equals("makebuffer") || type.equals("connecting") ||
						type.equals("connected") || type.equals("channel_init")) {
						self.onEvent(response);
					} else {
						list.add(response);
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (JSONObject response:list) {
					self.onEvent(response);
				}
			}

			rd.close();
			connected = false;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public void addEventListener(String type, EventListener listener) {
		ArrayList<EventListener> list = this.eventListeners.get(type);
		if(list == null) {
			list = new ArrayList<EventListener>();
			this.eventListeners.put(type, list);
		}
		list.add(listener);
	}

	public void removeEventListener(String type, EventListener listener) {
		ArrayList<EventListener> list = this.eventListeners.get(type);
		if(list == null) {
			return;
		}
		list.remove(listener);
	}

	public HashMap<String, ArrayList<EventListener>> getEventListeners() {
		return eventListeners;
	}

	public void setEventListeners(HashMap<String, ArrayList<EventListener>> eventListeners) {
		this.eventListeners = eventListeners;
	}

	private void onEvent(JSONObject event) {
		String type = null;
		type = (String)event.get("type");
		if(type == null) {
			return;
		}
		ArrayList<EventListener> listeners = this.eventListeners.get(type);
		if(listeners == null) {
			return;
		}
		for (EventListener listener : listeners) {
			listener.onEvent(event);
		}
	}
	
	public String postData(String type, ArrayList<NameValuePair> values) {
		String result = "";
		String data = "session=" + this.session + "&";
		data += "_regid=" + ++this.reqCount + "&";
		for(NameValuePair nvp : values) {
			data += nvp;
			if(values.indexOf(nvp)+1 < values.size()) {
				data += "&";
			}
		}
		System.out.println(data);
		URL loginURL = null;
		try {
			loginURL = new URL(actionUrl+type);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpURLConnection loginConn = null;
		try {
			loginConn = (HttpURLConnection) loginURL.openConnection();
			loginConn.addRequestProperty("Cookie", "session="+this.session);
			loginConn.setDoOutput(true);
			loginConn.connect();
			OutputStreamWriter wr = new OutputStreamWriter(
					loginConn.getOutputStream());
			wr.write(data);
			wr.flush();

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					loginConn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				result += line + "\n";
			}

			wr.close();
			rd.close();

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} 
		return result;
	}
	
}
