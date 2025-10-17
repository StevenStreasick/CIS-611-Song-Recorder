package songs;

import java.net.URISyntaxException;

public class Main implements MainInterface {
	
//	private static final String streamerName = "a_couple_streams";
	private static final String streamerName = "PeteZahHutt";

	private static final String clientId = "1234";
	
	private static boolean listenForStreamEvents() {
		try {
			TwitchAPI twitchAPI = new TwitchAPI(streamerName);
			twitchAPI.setClientId(clientId);
	
			StreamerSonglistAPI songlistAPI = new StreamerSonglistAPI(streamerName);
			
			if(!twitchAPI.isStreamerLive()) {
				
				twitchAPI.listenForStreamStart();
				
			} else {
				//Streamer is currently live.
				String startTime = twitchAPI.getStartTime();
				boolean setStartTimeSuccess = songlistAPI.setStartTime(startTime);
				
				if(!setStartTimeSuccess) {
					throw new Exception("Unable to set the songlist start time");
				}
			}
			
			twitchAPI.listenForStreamEnd();
			
			return true;
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static void main(String[] args) {
		
		try {
//			TwitchAPI twitchAPI = new TwitchAPI("wss://eventsub.wss.twitch.tv/ws");
			
			//TokenManager(String fileName)
			System.out.println(System.getProperty("user.dir"));
			TokenManager manager = new TokenManager("tokens.txt");
			System.out.println(manager.getBearerToken());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		listenForStreamEvents();
//		
//		//The idea here is that I want to 'freeze' the main thread to prevent program termination and allow the 
//		// events (streamStarted/streamEnded) to handle changes.
//		while(true) {
//			try {
//				Thread.sleep(Long.MAX_VALUE);
//			} catch(InterruptedException e) {
//				
//			}
//		}

	}

	public static void StreamWentLive() {
		// TODO: Fill out this method
		
	}

	public static void StreamEnded() {
		//Do stuff here to handle a 'restart'
		
		listenForStreamEvents();
	}
}
