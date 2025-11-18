package songs;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class TwitchAPI extends WebClient {
	
	/** [[VALUES TO CHANGE]] **/
	
	private String broadcaster = null;	
	
	/** [[END VALUES TO CHANGE] **/
	
	
	//DO NOT CHANGE THE VALUES BELOW
	
	final static CloseableHttpClient httpclient = HttpClientSingleton.HTTPCLIENT;

	private String startTime = null;
	private int streamerID = -1;
	
	private String sessionId = "";
	
//	private Instant expirationTime = null;
	
    private final List<StreamObserver> observers = new ArrayList<>();

	private static final String TOKENFILENAME = "Tokens.txt";
	private final static String TWITCHURI = "wss://eventsub.wss.twitch.tv/ws";
	private final static String HELIXURI = "https://api.twitch.tv/helix";
    
    public void addObserver(StreamObserver observer) {
    	if(isStreamerLive()) {
    		observer.onStreamStart(startTime);
    	}
    	
        observers.add(observer);
    }

    public void removeObserver(StreamObserver observer) {
        observers.remove(observer);
    }

    private void notifyStreamWentLive() {
        for (StreamObserver obs : observers) {
            obs.onStreamStart(startTime);
        }
    }

    private void notifyStreamEnded() {
        for (StreamObserver obs : observers) {
            obs.onStreamEnd();
        }
    }

	private boolean isBroadcasterValid(String streamerName) {
		//TODO: Complete this method
		return true;
	}
	
	public TwitchAPI(String streamerName) throws URISyntaxException {
		this(streamerName, new ClientInfo());
	}

	public TwitchAPI(String streamerName, ClientInfo clientInfo) throws URISyntaxException {
		super(new URI(TWITCHURI), TOKENFILENAME, clientInfo);

		try {
			
			if (streamerName == null || streamerName.isBlank()) {
				throw new IllegalArgumentException("The broadcaster must be supplied");
			}
			
			if (!isBroadcasterValid(streamerName)) {
				throw new IllegalArgumentException("The broadcaster is not a valid Twitch username");
			}

			broadcaster = streamerName;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int getStreamerID() {
		try {
			URI uri = new URIBuilder(HELIXURI + "/users")
			                    .addParameter("login", broadcaster)
			                    .build();
			
			HttpGet get = new HttpGet(uri);
			get.setHeader("Client-Id", clientInfo.getClientID());
			get.setHeader("Authorization", "Bearer " + super.getBearerToken());
			
			HttpResponse response = httpclient.execute(get);

			JSONObject responseObject = new JSONObject(EntityUtils.toString(response.getEntity()));
			
			if (responseObject.has("error")) {
				throw new Exception(responseObject.optString("error") + ", "
						+ responseObject.optString("message"));
			}

			if (!responseObject.has("data")) {
				throw new Exception("Twitch API took a dump on me");
			}

			JSONArray data = responseObject.optJSONArray("data");

			if (data.length() == 0) {
				throw new Exception("Unable to find the twitch user. Is the streamerName a valid streamer?");
			}
			
			if (data.length() < 1) {
				//TODO: Make it so that users can submit a streamerID instead of streamerName
				throw new Exception("Too many streamers were found. Unable to identify which streamer to use.");
			}
			
			JSONObject streamerData = data.optJSONObject(0);
			String optedIDString = streamerData.optString("id");
			
			if(optedIDString.isBlank()) {
				throw new Exception("Unable to find user_id.");
			}

			Integer optedID = Integer.parseInt(optedIDString);
			
			streamerID = optedID;
			
			return streamerID;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return -1;
	}

	private JSONObject getStreamerDataFromStreamers(JSONArray streamers) {
		
		if(streamerID == -1) {
			getStreamerID();
		}
		
		for (int i = 0; i < streamers.length(); i++) {
			JSONObject currentStreamerData = streamers.optJSONObject(i);
			String optedIDString = currentStreamerData.optString("user_id");
			
			if(optedIDString.isBlank()) {
				continue;
			}

			Integer optedID = Integer.parseInt(optedIDString);
			
			if(streamerID == optedID) {
				return currentStreamerData;
			}
			
		}

		return new JSONObject("{}");
	}

	public boolean isStreamerLive() {
		try {
			// TODO: Call the twitch Kraken/Streamer endpoint.
			// NOTE: JSON will be empty if the streamer is not live.
			// NOTE: Once I call the endpoint, I will get back a JSON. Parse JSON for
			// startTime and set startTime
			URI uri = new URIBuilder(HELIXURI + "/streams")
			                    .addParameter("user_login", broadcaster)
			                    .build();
			
			HttpGet get = new HttpGet(uri);
			
//			post.setHeader("Content-type", "application/json");
			get.setHeader("Client-Id", clientInfo.getClientID());
			get.setHeader("Authorization", "Bearer " + super.getBearerToken()); 

			HttpResponse response = httpclient.execute(get);

			JSONObject responseObject = new JSONObject(EntityUtils.toString(response.getEntity()));

			if (responseObject.has("error")) {
				if (responseObject.has("status") && responseObject.optInt("status") == 400) {
					throw new Exception(
							"Unable to check if streamer is live. Is the streamerName a valid streamer?");
				}
				throw new Exception(responseObject.optString("error") + ", "
						+ responseObject.optString("message"));
			}
			// TODO: Check if data exists or is null for a non-live call
			if (!responseObject.has("data")) {
				throw new Exception("Twitch API took a dump on me");
			}

			JSONArray data = responseObject.optJSONArray("data");

			if (data.length() == 0) {
				// Streamer is not live
				return false;
			}

			JSONObject streamerData = getStreamerDataFromStreamers(data);
			
			if (streamerData.isEmpty()) {
				// A streamer that matched the name is live, but my Streamer is not live.
				return false;
			}

			if (!streamerData.has("started_at")) {
				throw new Exception("Unable to find streamer start time");
			}

			String started_at = streamerData.optString("started_at");

			if (started_at == null || started_at.isBlank()) {
				throw new Exception("Parsed started_at data provided by twitch is invalid");
			}

			startTime = started_at;

			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private boolean parseForID(JSONObject response) {
		JSONObject payload = response.optJSONObject("payload");

		if (payload == null) {
			return false;
		}

		JSONObject session = payload.optJSONObject("session");

		if (session == null) {
			return false;
		}

		sessionId = session.optString("id");
		
		return true;
	}

	private boolean handleReconnect(JSONObject reconnectResponse) {

		//TODO: Fill this information out
			
		JSONObject payload = reconnectResponse.optJSONObject("payload");
		if (payload == null || payload.isEmpty()) {
			throw new IllegalArgumentException();
		}

		JSONObject session = payload.optJSONObject("session");
		if (session == null || session.isEmpty()) {
			throw new IllegalArgumentException();
		}

		String reconnectUrl = session.optString("reconnect_url");
		if (reconnectUrl != null && !reconnectUrl.isEmpty()) {
			System.out.println("Reconnecting to: " + reconnectUrl);
			try {
				this.close(); // Close the current connection
				 URI reconnectUri = new URI(reconnectUrl);
				 
		        super.updateURI(reconnectUri);
		        this.reconnectBlocking();
				 // Establish the new connection
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	private boolean confirmSubscription(JSONObject response, String event) {
		if (!response.has("data")) {
			return false;
		}

		JSONArray data = response.optJSONArray("data");

		int size = data.length();
		if (size == 0) {
			return false;
		}

		for (int i = 0; i < size; i++) {
			JSONObject dataentry = data.optJSONObject(i);

			if (!dataentry.has("status") || !dataentry.has("type")) {
				continue;
			}

			String entryType = dataentry.optString("type");
			if (!entryType.equals(event)) {
				continue;
			}

			String entryStatus = dataentry.optString("status");
			if (!entryStatus.equals("enabled")) {
				return false;
			}

			return true;
		}

		return false;
	}
	
	private boolean subscribeToEvent(String event) {
		try {
			if(streamerID == -1) {
				getStreamerID();
			}
			// Construct the subscription message for stream.online event

			JSONObject condition = new JSONObject();
			condition.put("broadcaster_user_id", streamerID); 
			
			JSONObject transport = new JSONObject();
			transport.put("method", "websocket");
			transport.put("session_id", sessionId);

			JSONObject eventSub = new JSONObject();
			eventSub.put("condition", condition);
			eventSub.put("transport", transport);
			eventSub.put("type", event);
			eventSub.put("version", "-1");

			// Send the subscription message to Twitch
			// Post to https://api.twitch.tv/helix/eventsub/subscriptions
			// session_id
			// type
			// version
			// condition
			// transport

			HttpPost post = new HttpPost("https://api.twitch.tv/helix/eventsub/subscriptions");

			post.setHeader("Content-type", "application/json");
			post.setHeader("Client-Id", clientInfo.getClientID());
			post.setHeader("Authorization", "Bearer " + super.getBearerToken());

			StringEntity entity = new StringEntity(eventSub.toString());
			post.setEntity(entity);

			HttpResponse response = httpclient.execute(post);

			boolean success = confirmSubscription(
					new JSONObject(EntityUtils.toString(response.getEntity())), event);

			if (!success) {
				throw new Exception("Server declined to subscribe to event");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	
	private boolean handleWelcome(JSONObject jsonMessage) {
		boolean parsedID = parseForID(jsonMessage);
		
		if(parsedID == false) {
			return false;
		}

		boolean subscribedOnline = subscribeToEvent("stream.online");
		boolean subscribedOffline = subscribeToEvent("stream.offline");
		
		return subscribedOnline && subscribedOffline;
	}
	
	@Override
	public void onMessage(String message) {
		JSONObject jsonMessage = new JSONObject(message);

		JSONObject metadata = jsonMessage.optJSONObject("metadata");

		if (metadata == null || metadata.isEmpty()) {
			return;
		}
		// Check for "session_welcome" message type
		String message_type = metadata.optString("message_type");

		// Just a message to keep the connection alive
		if (message_type.equalsIgnoreCase("session_keepalive")) {
			return;
		}

		if (message_type.equalsIgnoreCase("session_reconnect")) {
			handleReconnect(jsonMessage);
			return;
		}

		System.out.println("Received: " + message);

		// Parse the message to extract the session ID
		if (message_type.equalsIgnoreCase("session_welcome")) {
			handleWelcome(jsonMessage);
		}

		JSONObject payload = jsonMessage.optJSONObject("payload");
		if (payload == null || payload.isEmpty()) {
			return;
		}

		JSONObject subscription = payload.optJSONObject("subscription");
		if (subscription == null || subscription.isEmpty()) {
			return;
		}

		String type = subscription.optString("type");

		String createdAt = subscription.optString("created_at");

		if (createdAt == null || createdAt.isEmpty()) {
			return;
		}
		//TODO: Somehow I need to get Twitch's start time
		if (type.equalsIgnoreCase("stream.online")) {
			notifyStreamWentLive();
		}

		if (type.equals("stream.offline")) {
			notifyStreamEnded();
		}
	}
	
	public String getStartTime() {
		return startTime;
	}
}
