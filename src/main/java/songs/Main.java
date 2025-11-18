package songs;

import java.net.URISyntaxException;

public class Main implements StreamObserver {
	
	private static final String streamerName = "CERIANMusic";

	String clientId = System.getenv("TWITCH_CLIENT_ID");
	String clientSecret = System.getenv("TWITCH_CLIENT_SECRET");
	
	private TwitchAPI twitchAPI;
	private StreamerSonglistAPI songlistAPI;
	
	public Main() {
		try {
			twitchAPI = new TwitchAPI(streamerName);
			
			twitchAPI.clientInfo.setClientId(clientId);
			twitchAPI.clientInfo.setClientSecret(clientSecret);
			
			System.out.println("Adding as observer");
			twitchAPI.addObserver(this);
			
		} catch(Exception e) {
			
		}
	}

	@Override
	public void onStreamStart(String startTime) {
		System.out.println("Stream is starting at " + startTime);
		songlistAPI = new StreamerSonglistAPI(streamerName);
		songlistAPI.setStartTime(startTime);
	}
	
	@Override
	public void onStreamEnd() {
		//Do stuff here to handle a 'restart'
		try {
			songlistAPI.writeSonglistToFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		listenForStreamEvents();
	}
	
	public static void main(String[] args) {
		try {
			Main main = new Main();
			
//			TokenManager manager = new TokenManager("tokens.txt");

			
			//TokenManager(String fileName)
	        Thread.currentThread().join(); // Wait indefinitely
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
