package songs;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class WebClient {

	protected final ClientInfo clientInfo;
	protected final CallbackServer callbackServer;

	private final TokenManager tokenManager;
	
	private WebSocketClient client;
	
	public WebClient(URI serverURI, String tokenFileName) {
		this(serverURI, tokenFileName, new ClientInfo());	
	}
	
	public WebClient(URI serverURI, String tokenFileName, ClientInfo clientInfo) {
		this(serverURI, tokenFileName, clientInfo, new CallbackServer());
	}
	
	public WebClient(URI serverURI, String tokenFileName, ClientInfo clientInfo, CallbackServer callbackServer) {
		if(tokenFileName == null || tokenFileName.isBlank()) { 
            throw new IllegalArgumentException(); 
        }
		
		this.clientInfo = clientInfo;
		this.callbackServer = callbackServer;
		
		tokenManager = new TokenManager(tokenFileName, clientInfo, callbackServer);
        this.client = createWebSocketClient(serverURI);
	}
	
	 private WebSocketClient createWebSocketClient(URI serverURI) {
        return new WebSocketClient(serverURI) {
            
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                WebClient.this.onOpen(handshakedata);
            }
            
            @Override
            public void onMessage(String message) {
                WebClient.this.onMessage(message);
            }
            
            @Override
            public void onClose(int code, String reason, boolean remote) {
                WebClient.this.onClose(code, reason, remote);
            }
            
            @Override
            public void onError(Exception ex) {
                WebClient.this.onError(ex);
            }
        };
    }
	 
	protected void updateURI(URI newUri) throws Exception {
	    client.close();
	    client = createWebSocketClient(newUri);
	    client.connectBlocking();
	}
	   
	public void connect() {
		client.connect();
	}
    
    public void connectBlocking() throws InterruptedException {
    	client.connectBlocking();
    }
    
    public void close() {
    	client.close();
    }
    
    public void send(String text) {
    	client.send(text);
    }
    
    public boolean isOpen() {
        return client.isOpen();
    }
    
    protected void onOpen(ServerHandshake handshakedata) {

    }
    
    protected void onMessage(String message) {
    }
    
    protected void onClose(int code, String reason, boolean remote) {
    }
    
    protected void onError(Exception ex) {
    }
    
	protected String getBearerToken() {
		
		return tokenManager.getBearerToken();
	}
}
