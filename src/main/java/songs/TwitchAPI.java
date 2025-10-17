package songs;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class TwitchAPI extends WebClient  {
	
	/** [[VALUES TO CHANGE]] **/
	
	private String broadcaster = null;	
	
	/** [[END VALUES TO CHANGE] **/
	
	
	//DO NOT CHANGE THE VALUES BELOW
	
	final static CloseableHttpClient httpclient = HttpClientSingleton.HTTPCLIENT;

	private String startTime = null;
	private int streamerID = -1;
	
	private String sessionId = "";
	
	private Instant expirationTime = null;
	
	private static final String TOKENFILENAME = "Tokens";

	
	
	//BEGIN TEST VARIABLES
//	private String clientID = "6gjebalqg5yvco31ww72u7i0o4swiz";
//	
//	private int streamerID = 21837508;
//	private String sessionId = "";
//	private String bearerToken = "bnhfeypf5txclm9nat15sap6yf2ara";
	//END TEST VARIABLES

	
	private final static String TWITCHURI = "wss://eventsub.wss.twitch.tv/ws";
	private final static String HELIXURI = "https://api.twitch.tv/helix";

	private boolean isBroadcasterValid(String streamerName) {
		//TODO: Complete this method
		return true;
	}

	public TwitchAPI(String streamerName) throws URISyntaxException {
		super(new URI(TWITCHURI), TOKENFILENAME);

		try {
			
			if (streamerName == null || streamerName.isBlank()) {
				throw new Exception("The broadcaster must be supplied");
			}
			
			if (!isBroadcasterValid(streamerName)) {
				throw new Exception("The broadcaster is not a valid Twitch username");
			}

			broadcaster = streamerName;
			//TODO: Add the client information

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
			
			get.setHeader("Client-Id", getClientID());
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
			String optedIDString = streamerData.optString("user_id");
			
			if(optedIDString.isBlank()) {
				throw new Exception("Unable to find user_id.");
			}

			Integer optedID = Integer.parseInt(optedIDString);
			
			streamerID = optedID;
			
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
			get.setHeader("Client-Id", clientID);
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

	public boolean listenForStreamStart() {
		try {
			//TODO: Fill this information out
			//TODO: Setup the websocket information
			// Webhook to listen

			// Main.streamStarted();
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean listenForStreamEnd() {
		try {
			// TODO: Setup the websocket information
			// Webhook to listen

			// Main.streamEnded();
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private boolean parseForIdAndSubscribe(JSONObject response) {
		try {
			//TODO: Fill this information out
			
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;

	}

	private boolean handleReconnect(JSONObject reconnectResponse) {
		try {
			//TODO: Fill this information out
			
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	
	private boolean confirmSubscription(JSONObject response, String iDoNotRememberWhatThisVariableIsForButItsImportant) {
		try {
			//TODO: Fill this information out
			
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	
	private boolean subscribeToEvent(String event) {
		try {
			//TODO: Fill this information out
			
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	
	public String getStartTime() {
		return startTime;
	}
}
