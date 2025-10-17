package songs;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class WebClient extends WebSocketClient {


	protected final ClientInfo clientInfo = new ClientInfo();
	private final TokenManager tokenManager;

	public WebClient(URI serverUri, String tokenFileName) {
		super(serverUri);
		
		this.tokenManager = new TokenManager(tokenFileName, clientInfo);
	}
	
	protected String getBearerToken() {
		tokenManager.updateTokens();
		return tokenManager.getBearerToken();
	}

	
	
	
	
	@Override
	public void onOpen(ServerHandshake handshakedata) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessage(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(Exception ex) {
		// TODO Auto-generated method stub

	}
	
	
}
