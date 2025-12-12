package songs;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import songs.CallbackServer.OAuthCallbackListener;

public class TokenManager extends FileWriter implements OAuthCallbackListener {
	private final CloseableHttpClient httpclient = HttpClientSingleton.getInstance();

	private final Path filePath;
	
	//Access Token
	private String bearerToken;
	private String refreshToken;
	private Long expiresAt;
	
	//OAUTH flow
	private CompletableFuture<Boolean> authFuture;
	
	private ClientInfo clientInfo;
	private CallbackServer callbackServer;
	

	private final String ENDPOINT = "https://id.twitch.tv/oauth2/";
	
    private static final long EXPIRY_BUFFER_SECONDS = 300;

    public TokenManager(String fileName, ClientInfo clientInfo, CallbackServer callbackServer) {
        super(fileName);

        this.filePath = Path.of(fileName);
        this.clientInfo = clientInfo;
        this.callbackServer = callbackServer;

        loadTokens();
    }
    
    public TokenManager(String fileName, ClientInfo clientInfo, String bearerToken, Long expiresIn, String refreshToken) {
        super(fileName);

        this.filePath = Path.of(fileName);
        this.clientInfo = clientInfo;
        
        Long expiresAt = Instant.now().getEpochSecond() + expiresIn;

        setTokens(bearerToken, expiresAt, refreshToken);
    }
    
    private synchronized void setTokens(String newBearer, Long newExpiresAt, String newRefreshToken) {
        this.bearerToken = newBearer;
        this.expiresAt = newExpiresAt;
        this.refreshToken = newRefreshToken;

        String content = String.join("\n", 
            bearerToken != null ? bearerToken : "", 
            refreshToken != null ? refreshToken : "", 
            String.valueOf(expiresAt != null ? expiresAt : -1)
        );

        super.writeToFile(content);
    }
    
    private void readTokenFile() throws IOException {
        List<String> content = super.readFromFile();
        
        if (content.size() < 3) {
            this.bearerToken = null;
            this.refreshToken = null;
            this.expiresAt = null;

            return;
        }

        this.bearerToken = content.get(0).isEmpty() ? null : content.get(0);
        this.refreshToken = content.get(1).isEmpty() ? null : content.get(1);
        
        try {
            this.expiresAt = Long.parseLong(content.get(2));

            if (this.expiresAt < 0) {
                this.expiresAt = null;
            }

        } catch (NumberFormatException e) {
            this.expiresAt = null;
        }
    }

    private void loadTokens() {
        try {
            
            if (Files.exists(filePath)) {
                readTokenFile();
            }

        } catch (IOException e) {
            System.err.println("Error reading token file: " + e.getMessage());

            this.bearerToken = null;
            this.refreshToken = null;
            this.expiresAt = null;
        }
    }

    private boolean isTokenValid() {
        try {
            if (isClientInformationBlank()) {
                return false;
            }
            
            if (bearerToken == null || bearerToken.isEmpty()) {
                return false;
            }
            
            if (expiresAt == null) {
                return false;
            }
            
            Long currentEpochSeconds = Instant.now().getEpochSecond();
            if (currentEpochSeconds >= (expiresAt - EXPIRY_BUFFER_SECONDS)) {
                return false;
            }
            
            URI uri = new URI(ENDPOINT + "validate");
            HttpGet get = new HttpGet(uri);
            get.addHeader("Authorization", "OAuth " + bearerToken);

            CloseableHttpResponse response = httpclient.execute(get);
            JSONObject responseObject = new JSONObject(EntityUtils.toString(response.getEntity()));

            if (responseObject.has("status")) {
                int status = responseObject.optInt("status", 200);
                return status == 200;
            }

            return true;

        } catch (Exception e) {
            System.err.println("Error validating token: " + e.getMessage());
            return false;
        }
    }

    private boolean refreshBearerToken() {
        try {
            if (isClientInformationBlank()) {
                System.err.println("Client information is blank, cannot refresh token");
                return false;
            }
            
            if (!doesRefreshTokenExist()) {
                System.err.println("No refresh token available");
                return false;
            }
            
            URI uri = new URI(ENDPOINT + "token");
            HttpPost post = new HttpPost(uri);
            
            List<String> params = Arrays.asList(
                "client_id=" + URLEncoder.encode(clientInfo.getClientID(), StandardCharsets.UTF_8),
                "client_secret=" + URLEncoder.encode(clientInfo.getClientSecret(), StandardCharsets.UTF_8),
                "grant_type=refresh_token",
                "refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
            );
            
            String body = String.join("&", params);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");
            post.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
            
            Long requestTime = Instant.now().getEpochSecond();
            CloseableHttpResponse httpresponse = httpclient.execute(post);
            JSONObject response = new JSONObject(EntityUtils.toString(httpresponse.getEntity()));

            return handleTokenResponse(response, requestTime);

        } catch (Exception e) {
            System.err.println("Error refreshing bearer token: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean isClientInformationBlank() {
        return clientInfo.getClientID() == null || clientInfo.getClientID().isBlank() ||
               clientInfo.getClientSecret() == null || clientInfo.getClientSecret().isBlank();
    }
    
    private boolean doesRefreshTokenExist() {
        return refreshToken != null && !refreshToken.isBlank();
    }
    

    public String getBearerToken() {
        if (updateTokens()) {
            return bearerToken;
        }

        return bearerToken;
    }
    
    private boolean exchangeCodeForToken(String code) throws Exception {
        String url = "https://id.twitch.tv/oauth2/token" +
                "?client_id=" + URLEncoder.encode(clientInfo.getClientID(), StandardCharsets.UTF_8) +
                "&client_secret=" + URLEncoder.encode(clientInfo.getClientSecret(), StandardCharsets.UTF_8) +
                "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                "&grant_type=authorization_code" +
                "&redirect_uri=" + URLEncoder.encode(callbackServer.getCallbackAddress(), StandardCharsets.UTF_8);

        Long requestTime = Instant.now().getEpochSecond();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            throw new Exception("Token exchange failed with status: " + res.statusCode() + " - " + res.body());
        }

        JSONObject body = new JSONObject(res.body());
        return handleTokenResponse(body, requestTime);
    }

  
    public void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop d = Desktop.getDesktop();
                if (d.isSupported(Desktop.Action.BROWSE)) {
                    d.browse(new URI(url));
                    return;
                }
            }
            
            // Linux
            new ProcessBuilder("xdg-open", url).start();
        } catch (Exception e) {
            System.err.println("Unable to open browser. Please manually visit:\n" + url);
        }
    }

    private boolean userAuthentication() {
        try {
            if (callbackServer == null) {
                System.err.println("Callback server not configured");
                return false;
            }
            
            authFuture = new CompletableFuture<>();
            
            String authUrl = ENDPOINT + "authorize" +
                    "?client_id=" + URLEncoder.encode(clientInfo.getClientID(), StandardCharsets.UTF_8) +
                    "&redirect_uri=" + URLEncoder.encode(callbackServer.getCallbackAddress(), StandardCharsets.UTF_8) +
                    "&response_type=code" +
                    "&scope=moderator:read:followers";
            
            callbackServer.addListener(this);
            callbackServer.startCallbackServer();
            
            System.out.println("Opening browser for authentication...");
            openBrowser(authUrl);
            
            boolean success = authFuture.get(5, TimeUnit.MINUTES);
            return success;
        
        } catch (TimeoutException e) {

            System.err.println("Authentication timed out");
            authFuture.complete(false);
        
        } catch (Exception e) {
        
            System.err.println("Authentication error: " + e.getMessage());
            e.printStackTrace();

        } finally {
            callbackServer.stopCallbackServer();
        }
        
        return false;
    }

    public synchronized boolean updateTokens() {
        try {
            if (isClientInformationBlank()) {
                System.err.println("Client information is not configured");
                return false;
            }
    
            if (isTokenValid()) {
                return true;
            }
            
            System.out.println("Token invalid or expired, attempting to refresh...");
            
            if (doesRefreshTokenExist()) {
                if (refreshBearerToken()) {
                    System.out.println("Successfully refreshed token");
                    return true;
                }

                System.out.println("Refresh failed, falling back to user authentication");
            }
    
            return userAuthentication();
                        
        } catch (Exception e) {
            System.err.println("Error updating tokens: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    private boolean handleTokenResponse(JSONObject response, Long requestTime) throws Exception {
        if (!response.has("token_type")) {
            throw new Exception("Missing token_type in response: " + response);
        }
        
        if (!response.has("access_token")) {
            throw new Exception("Missing access_token in response: " + response);
        }
        
        if (!response.has("expires_in")) {
            throw new Exception("Missing expires_in in response: " + response);
        }
        
        if (!response.has("refresh_token")) {
            throw new Exception("Missing refresh_token in response: " + response);
        }

        String newBearerToken = response.getString("access_token");
        Long expiresInOffset = response.getLong("expires_in");
        Long newExpiresAt = requestTime + expiresInOffset;
        String newRefreshToken = response.getString("refresh_token");
        
        setTokens(newBearerToken, newExpiresAt, newRefreshToken);
        
        System.out.println("Tokens updated successfully. Expires at: " + 
            java.time.Instant.ofEpochSecond(newExpiresAt));
        
        return true;
    }

    @Override
    public void onCodeReceived(Map<String, String> params) {
        try {
            String code = params.get("code");

            if (code == null || code.isEmpty()) {
                throw new Exception("No authorization code received");
            }
            
            exchangeCodeForToken(code);

            authFuture.complete(true);
            System.out.println("Successfully authenticated user");
        } catch (Exception e) {
            System.err.println("Error processing authorization code: " + e.getMessage());
            e.printStackTrace();
            authFuture.complete(false);
        }
    }
    
    @Override
    public void onError(String error, String errorDescription) {
        System.err.println("OAuth error: " + error + " - " + errorDescription);

        if (authFuture != null && !authFuture.isDone()) {
            authFuture.complete(false);
        }
        
        callbackServer.stopCallbackServer();
    }
    
    
    public synchronized void clearTokens() {
        setTokens(null, -1L, null);
        System.out.println("Tokens cleared");
    }

    public boolean hasTokens() {
        return bearerToken != null && !bearerToken.isEmpty();
    }
}
