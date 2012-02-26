package org.vatvit.irccloud;

import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.vatvit.irccloud.events.ChannelListener;
import org.vatvit.irccloud.events.EventListener;

public class Channel {
	private Connection connection;
	
	private ArrayList<ChannelListener> listeners = new ArrayList<ChannelListener>();
	
	private String name;
	private String topic;
	private String topicAuthor;
	private long topicTime;
	private long cid;
	private ArrayList<Message> messages = new ArrayList<Message>();
	private int maxMessages = 100;
	
	public Channel(Connection conn, JSONObject object) {
		this.connection = conn;
		
		this.cid = (Long)object.get("cid");
		this.name = (String)object.get("chan");
		JSONObject topicObj = (JSONObject)object.get("topic");
		if(topicObj.get("topic_text") != null) {
			this.topic = (String)topicObj.get("topic_text");
			this.topicAuthor = (String)topicObj.get("topic_author");
			this.topicTime = (Long)topicObj.get("topic_time");
		}
		
		initListeners();
	}
	private void initListeners() {
		this.connection.addEventListener("buffer_msg", new EventListener(){
			@Override
			public void onEvent(JSONObject event) {
				long ecid = 0;
				ecid = (Long)event.get("cid");
				String chan = null;
				chan = (String)event.get("chan");
				if(ecid != cid || !name.equalsIgnoreCase(chan)) {
					return;
				}
				
				Message message = new Message(connection, event);
				while (messages.size() >= maxMessages) {
					messages.remove(0);
				}
				messages.add(message);
				newMessage(message);
				
			}
			
		});
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getTopicAuthor() {
		return topicAuthor;
	}

	public void setTopicAuthor(String topicAuthor) {
		this.topicAuthor = topicAuthor;
	}

	public long getTopicTime() {
		return topicTime;
	}

	public void setTopicTime(long topicTime) {
		this.topicTime = topicTime;
	}

	
	public long getCid() {
		return cid;
	}

	public void setCid(long cid) {
		this.cid = cid;
	}

	@Override
	public String toString() {
		return name;
	}

	public ArrayList<Message> getMessages() {
		return messages;
	}

	public void setMessages(ArrayList<Message> messages) {
		this.messages = messages;
	}

	public void addChannelListener(ChannelListener listener) {
		listeners.add(listener);
	}
	
	public void removeChannelListener(ChannelListener listener) {
		listeners.remove(listener);
	}
	
	public ArrayList<ChannelListener> getChannelListeners() {
		return listeners;
	}

	public void setChannelListeners(ArrayList<ChannelListener> serverListeners) {
		listeners = serverListeners;
	}
	
	private void newMessage(Message message) {
		for(ChannelListener listener : this.listeners) {
			listener.newMessage(message);
		}
	}
	
	public void sendMessage(String message) {
		ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new NameValuePair("cid", this.cid+""));
		values.add(new NameValuePair("msg", message));
		values.add(new NameValuePair("to", this.name));
		this.connection.postData("say", values);
	}
	
	public void setMaxMessages(int maxMessages) {
		this.maxMessages = maxMessages;
	}
	
	public int getMaxMessages() {
		return maxMessages;
	}
}
