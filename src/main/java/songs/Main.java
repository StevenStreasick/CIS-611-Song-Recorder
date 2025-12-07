package songs;

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
		songlistAPI.listenForUpdates();
	}
	
	@Override
	public void onStreamEnd() {
		System.out.println("Stream finished");

		try {

			songlistAPI.writeSonglistToFile();
			songlistAPI.stopListening();
			songlistAPI.clearSonglist();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			new Main();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
