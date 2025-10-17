package songs;

import java.util.LinkedHashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


public class StreamerSonglistAPI implements StreamerSonglistAPIInterface {
	final static CloseableHttpClient httpclient = HttpClients.createDefault();

	private static final String SOCKET_URI = "https://api.streamersonglist.com";

	private LinkedHashMap<String, Instant> songlist = new LinkedHashMap<String, Instant>();
	private Instant startTime = null; 
	
	private String broadcaster = null; //Update this

	public StreamerSonglistAPI(String streamerName) {
		try {
			if(broadcaster == null) {
				throw new Exception("The broadcaster must be supplied");
			}
			
			if(!isBroadcasterValid(streamerName)) {
				throw new Exception("The broadcaster is either not a valid twitch user or does not use the StreamerSonglist application");
			}
			
			broadcaster = streamerName;
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private boolean isBroadcasterValid(String streamerName) {
		//TODO: Fill this information out
		return true;
	}
	
	private void listenForUpdates() {
		
	}
	
	private Instant convertStringToInstant(String timeString) {
		ZonedDateTime zonedDateTime = ZonedDateTime.parse(timeString);

		Instant parsedTime = zonedDateTime.toInstant();

		return parsedTime;

	}
	
	private void updateSonglist() {
		try {

			URIBuilder builder = new URIBuilder("https://api.streamersonglist.com/v1/streamers/"
					+ broadcaster + "/playHistory");
//			builder.addParameter("client_id", clientID);

			HttpGet get = new HttpGet(builder.build());

			HttpResponse response = httpclient.execute(get);

			String responseMessage = EntityUtils.toString(response.getEntity());

			System.out.println(responseMessage);

			JSONObject jsonresponse = new JSONObject(responseMessage);

			JSONArray songs = jsonresponse.optJSONArray("items");

			for (int i = 0; i < songs.length(); i++) {
				JSONObject songInfo = songs.optJSONObject(i);

				if (!songInfo.has("playedAt") || !songInfo.has("song")) {
					continue;
				}

				JSONObject song = songInfo.optJSONObject("song");

				String songTitle = song.optString("title");

				if (songlist.containsKey(songTitle)) {
					continue;
				}

				String playedAt = songInfo.optString("playedAt");

				Instant playedAtInstant = convertStringToInstant(playedAt);

				songlist.put(songTitle, playedAtInstant);
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean setStartTime(String startTime) {
		try {
			convertStringToInstant(startTime);
			return true;
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

}
