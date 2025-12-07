package songs;

import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.http.impl.client.CloseableHttpClient;

public class StreamerSonglistAPI extends FileWriter implements StreamerSonglistAPIInterface {
	final static CloseableHttpClient httpclient = HttpClientSingleton.HTTPCLIENT;

	private final ExecutorService executor = Executors.newSingleThreadExecutor(); 

	private static final String SOCKET_URI = "https://api.streamersonglist.com";
	private static final String API_PREFIX = "https://api.streamersonglist.com/v1/streamers";
	private static final int HISTORY_SIZE = 10;

	private LinkedHashMap<String, Duration> songlist = new LinkedHashMap<String, Duration>();
	private Instant startTime = null; 
	private String previousPlayedAt = null;
	
	private String broadcaster = null; 
	private int broadcasterID = -1;
	
	Socket socket;

	public StreamerSonglistAPI(String streamerName) throws IllegalArgumentException {
		if(streamerName == null || streamerName.isBlank()) {
			throw new IllegalArgumentException("The provided broadcaster is not a valid twitch user");
		}
		broadcaster = streamerName.toLowerCase();
		
		setBroadcasterID(broadcaster);
	}
	
	private int setBroadcasterID(String streamerName) throws IllegalArgumentException {
		try {
			URIBuilder builder = new URIBuilder(API_PREFIX + "/" + streamerName + "?platform=twitch");
			HttpGet get = new HttpGet(builder.build());
			
			HttpResponse response = httpclient.execute(get);
			
			StatusLine status = response.getStatusLine();
			if(status.getStatusCode() == 400) {
				throw new IllegalArgumentException("The provided broadcaster is not found in streamersonglist");
			}
			if(status.getStatusCode() != 200) {
				return -1;
			}
			
			String responseMessage = EntityUtils.toString(response.getEntity());
			JSONObject jsonresponse = new JSONObject(responseMessage);
			
			int id = jsonresponse.optInt("id");
			broadcasterID = id;
			return broadcasterID;

		} catch(URISyntaxException e) {
			e.printStackTrace();
		} catch(ClientProtocolException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public void listenForUpdates() {
		try {
			if(broadcasterID == -1) {
				throw new IllegalArgumentException("Broadcaster ID is invalid");
			}

			// Connect to the Socket.IO server
			IO.Options options = new IO.Options();
			options.transports = new String[] { "websocket" };
			options.reconnection = true; // Enable reconnection attempts
			options.timeout = 5000; // 5 seconds timeout

			socket = IO.socket(SOCKET_URI, options);
			socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
				System.err.println("Connection error: " + args[0]);
			});

			socket.on(Socket.EVENT_DISCONNECT, args -> {
				System.out.println("Disconnected from Songlist server: " + args[0]);
			});
			// Event: When connected
			socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					System.out.println("Connected to Songlist server");

					// Emit the 'join-room' event with the streamer ID
					socket.emit("join-room", broadcasterID);
					System.out.println("Joined room with Streamer ID: " + broadcasterID);
				}
			});

			// Event: update-playhistory (re-fetch play history)
			socket.on("new-playhistory", new Emitter.Listener() {
				@Override
				public void call(Object... args) {

					executor.submit(() -> { updateSonglist(); });
				}
			});

			// Connect to the server
			System.out.println("Connecting Songlist Socket");
			socket.connect();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void stopListening() { 
		try { 
			if (socket == null) return; 
			
			socket.off("new-playhistory"); 
			socket.off(Socket.EVENT_CONNECT); 
			socket.off(Socket.EVENT_CONNECT_ERROR); 
			socket.off(Socket.EVENT_DISCONNECT);
			socket.disconnect(); 
			socket.close(); 

		} catch (Exception e) { 
			e.printStackTrace(); 
		} finally { 
			socket = null; 
		} 
	}
	
	private Instant convertStringToInstant(String timeString) {
		ZonedDateTime zonedDateTime = ZonedDateTime.parse(timeString);

		Instant parsedTime = zonedDateTime.toInstant();

		return parsedTime;
	}
		
	private void updateSonglist(int current) {
		if(startTime == null) {
			throw new IllegalArgumentException("Please provide a start time before attempting to update the songlist");
		}
		
		try {

			URIBuilder builder = new URIBuilder(API_PREFIX + "/" + broadcaster + "/playHistory")
					.addParameter("current", Integer.toString(current))
					.addParameter("size", Integer.toString(HISTORY_SIZE));
			
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
				//TODO: Handle same titled songs/replayed songs
				if (songlist.containsKey(songTitle)) {
					continue;
				}
				
				
				String playedAt = songInfo.optString("playedAt");

				Instant playedAtInstant = convertStringToInstant(playedAt);
				
				if(playedAtInstant.isBefore(startTime)) {
					continue;
				}

				Duration timeElapsed = Duration.between(startTime,  playedAtInstant);
				
				System.out.printf("%s: %s%n", playedAtInstant, songTitle);
				songlist.put(songTitle, timeElapsed);
								
				if(i == songs.length() - 1) {
					updateSonglist(current + 1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void updateSonglist() {
		updateSonglist(0);
	}
	
	public boolean setStartTime(String startTimeString) {
		try {
			startTime = convertStringToInstant(startTimeString);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
					.withZone(ZoneId.of("UTC"));
			
			String filePath = "\\";
			String fileName = formatter.format(startTime);
			String fileType = ".txt";
			
			super.setPath(filePath + fileName + fileType);
			return true;
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean writeSonglistToFile() throws Exception {
		if(startTime == null) {
			throw new Exception("Please initialize startTime before attempting to write the songlist to file");
		}
		
		try {
			
			DateTimeFormatter startTimeFormatter = DateTimeFormatter.ofPattern("MM:dd:yyyy")
					.withZone(ZoneId.of("UTC"));
			
			String fileName = broadcaster + "-" + startTimeFormatter.format(startTime);
			super.setPath(fileName);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
					.withZone(ZoneId.of("UTC"));
			

			String result = songlist.entrySet().stream()
	        .map(e -> {
	            Duration value = e.getValue();
	            long seconds = value.getSeconds();
	            LocalTime currentTime = LocalTime.ofSecondOfDay(seconds);
	            
	            String currentTimeString = currentTime.format(formatter);
	
	            if(previousPlayedAt == null || previousPlayedAt.isBlank()) {
	            	previousPlayedAt = formatter.format(startTime);
	            }
	            String returnVal = String.format("%s: %s", e.getKey(), previousPlayedAt);
            	previousPlayedAt = currentTimeString;
	            return returnVal;
	        })
	        .collect(Collectors.joining("\n"));
			
			super.writeToFile(result);
						
			return true;
		} catch(Exception e) {
			System.out.println(e);
		}
		return false;
	}

	public boolean clearSonglist() {
		songlist.clear();

		return true;
	}

}
