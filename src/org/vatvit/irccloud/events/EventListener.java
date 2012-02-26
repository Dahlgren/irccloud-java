package org.vatvit.irccloud.events;

import org.json.simple.JSONObject;

public interface EventListener {
	public void onEvent(JSONObject event);
}
