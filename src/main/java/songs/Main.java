package songs;

import java.awt.Desktop;
import java.net.URISyntaxException;

public class Main implements StreamObserver {
	
//	private static final String streamerName = "LizKayTv";
	private static final String streamerName = "JenniferJess";

	String clientId = System.getenv("TWITCH_CLIENT_ID");
	String clientSecret = System.getenv("TWITCH_CLIENT_SECRET");
	
	private TwitchAPI twitchAPI;
	private StreamerSonglistAPI songlistAPI;
	
	public Main() {
		try {
			twitchAPI = new TwitchAPI(streamerName);
			
			twitchAPI.clientInfo.setClientId(clientId);
			twitchAPI.clientInfo.setClientSecret(clientSecret);
//			
//			System.out.println("Adding as observer");
			twitchAPI.addObserver(this);
			
			twitchAPI.connect();
			
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
		System.out.println("Stream finished");

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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
