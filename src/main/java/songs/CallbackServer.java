package songs;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class CallbackServer {
    private final int port;
    private final String callbackPath;
    private final String domain;

    private HttpServer server;
    private boolean isServerActive = false;

    private volatile String receivedCode;

    private final List<OAuthCallbackListener> listeners = new ArrayList<>();    
    
    public interface OAuthCallbackListener {
        void onCodeReceived(Map<String, String> params);
        
        default void onError(String error, String errorDescription) {
            System.err.println("OAuth error: " + error + " - " + errorDescription);
        }
     
        default void onServerStarted(String callbackAddress) {
            System.out.println("Callback server started on " + callbackAddress);
        }
        
        default void onServerStopped() {
            System.out.println("Callback server stopped");
        }
    }

    public CallbackServer() {
        this(8060);
    }
    
    public CallbackServer(int port) {
        this(port, "http://localhost", "/callback");
    }

    public CallbackServer(int port, String domain, String callbackPath) {
        this.port = port;
        this.domain = domain;
        this.callbackPath = callbackPath;
    }
    
    public void addListener(OAuthCallbackListener listener) {
        listeners.add(listener);
    }

    public void removeListener(OAuthCallbackListener listener) {
        listeners.remove(listener);
    }
  
    public void startCallbackServer() throws IOException {
    	if(isServerActive == true) {
    		System.out.println("Server already running");
    		return;
    	}
    	
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext(callbackPath, new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                try {
                    String query = exchange.getRequestURI().getQuery();
                    Map<String, String> params = parseQueryParams(query);
                    
                    if (params.containsKey("error")) {
                        handleError(params, exchange);
                        return;
                    }
                    
                    String response = getSuccessHtml();
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                                        
                    notifyCodeReceived(params);
                    
                } catch (Exception e) {
                    handleException(e, exchange);
                }
            }
        });

        isServerActive = true;
        server.start();
        notifyServerStarted();
    }
    
    public void stopCallbackServer() {
        if ( server == null ) { return; }
        if ( isServerActive == false ) { return; }

        isServerActive = false;
        server.stop(0);
        notifyServerStopped();

    }
    
    public String getCallbackAddress() {
        return domain + ":" + port + callbackPath;
    }
    
    public int getPort() {
        return port;
    }
   
    public String getCallbackPath() {
        return callbackPath;
    }
    
    public String getReceivedCode() {
        return receivedCode;
    }
    
    protected Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();

        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
        
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
        
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }
    
    private void handleError(Map<String, String> params, HttpExchange exchange) throws IOException {
        String error = params.get("error");
        String errorDescription = params.getOrDefault("error_description", "Unknown error");
        
        notifyError(error, errorDescription);
        
        String response = getErrorHtml(error, errorDescription);
        exchange.sendResponseHeaders(400, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
    
    
    private void handleException(Exception e, HttpExchange exchange) {
        try {
            String response = "<html><body><h1>Error processing callback</h1></body></html>";
            exchange.sendResponseHeaders(500, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        e.printStackTrace();
    }
        
    private void notifyCodeReceived(Map<String, String> params) {
        for (OAuthCallbackListener listener : listeners) {
            try {

                listener.onCodeReceived(params);
            } catch (Exception e) {
                System.err.println("Error in listener callback: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void notifyError(String error, String errorDescription) {
        for (OAuthCallbackListener listener : listeners) {
            try {
                
                listener.onError(error, errorDescription);
            } catch (Exception e) {
                System.err.println("Error in listener callback: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void notifyServerStarted() {
        for (OAuthCallbackListener listener : listeners) {
            try {

                listener.onServerStarted(getCallbackAddress());
            } catch (Exception e) {
                System.err.println("Error in listener callback: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void notifyServerStopped() {
        for (OAuthCallbackListener listener : listeners) {
            try {

                listener.onServerStopped();
            } catch (Exception e) {
                System.err.println("Error in listener callback: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    protected String getSuccessHtml() {
        return "<html><body><h1>Authorization Successful!</h1>"
             + "<p>You may now close this window.</p></body></html>";
    }
    
    protected String getErrorHtml(String error, String errorDescription) {
        return "<html><body><h1>Authorization Failed</h1>"
             + "<p>Error: " + error + "</p>"
             + "<p>" + errorDescription + "</p></body></html>";
    }
}