package songs;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/** This class deals with getting/maintaining a bearer token from Twitch's API **/
public class TokenManager extends FileWriter {
	private final CloseableHttpClient httpclient = HttpClientSingleton.HTTPCLIENT;

	private final Path filePath;
	private String bearerToken;
	private Long expiresIn;
	
	private ClientInfo clientInfo;

	private final String ENDPOINT = "https://id.twitch.tv/oauth2/";

	public TokenManager(String fileName, ClientInfo clientInfo) {
		super(fileName);
		this.filePath = Path.of(fileName);
		this.clientInfo = clientInfo;
		loadTokens();
//		updateTokens();
	}
	
	public TokenManager(String fileName, ClientInfo clientInfo, String bearerToken, Long expiresIn) {
		super(fileName);
		this.filePath = Path.of(fileName);
		this.clientInfo = clientInfo;
		setTokens(bearerToken, expiresIn);
//		updateTokens();
	}
	
	private synchronized void setTokens(String newBearer, long newExpiresIn) {
		this.bearerToken = newBearer;
		this.expiresIn = newExpiresIn;

		String content = String.join("\n", bearerToken, String.valueOf(expiresIn));
		super.writeToFile(content);
	}
	
	private void readTokenFile() throws IOException {
		List<String> content = super.readFromFile();
		
		if (content.size() >= 2) {
			this.bearerToken = content.get(0);
			this.expiresIn = Long.parseLong(content.get(1));
		} else {
			this.bearerToken = "";
			this.expiresIn = (long) -1;
		}
	}

	private void loadTokens() {
		try {
			if(Files.exists(filePath)) {
				readTokenFile();
			} 
			
			updateTokens();
		} catch (IOException | NumberFormatException e) {
			System.err.println("Error reading token file: " + e.getMessage());
			this.bearerToken = "";
			this.expiresIn = (long) 0;
		}
	}

	private boolean isTokenValid() {
		try {
			if(this.isClientInformationBlank()) {
				return false;
			}
			
			if(expiresIn == null) {
				return false;
			}
			
			Long currentEpochSeconds = Instant.now().getEpochSecond();
			if(currentEpochSeconds > expiresIn || expiresIn < 0) {
				return false;
			}
			
			URI uri = new URIBuilder(ENDPOINT + "/validate").build();

			HttpGet get = new HttpGet(uri);

			get.addHeader("Authorization", "OAuth " + bearerToken);

			HttpResponse response = httpclient.execute(get);

			JSONObject responseObject = new JSONObject(EntityUtils.toString(response.getEntity()));

			if (responseObject.has("status")) {
				String status = responseObject.optString("status");

				if (status.equalsIgnoreCase("401")) {
					return false;
				}

				throw new Exception("Unable to verify token: " + status);
			}

			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private boolean createNewTokens() {
		try {
			if(this.isClientInformationBlank()) {
				return false;
			}
			
			URIBuilder uri = new URIBuilder(ENDPOINT + "token");
			HttpPost post = new HttpPost(uri.build());
			JSONObject body = new JSONObject();
			
			JSONObject response;
			
			body.put("client_id", clientInfo.getClientID());
			body.put("client_secret", clientInfo.getClientSecret());
			body.put("grant_type", "client_credentials");
			
			post.setHeader("Content-Type", "application/json");
			
			StringEntity entity = new StringEntity(body.toString(), StandardCharsets.UTF_8);
			post.setEntity(entity);
			
			Long requestTime = Instant.now().getEpochSecond();

			HttpResponse httpresponse = httpclient.execute(post);

			response = new JSONObject(EntityUtils.toString(httpresponse.getEntity()));

			if (!response.has("token_type")) {
				System.out.println(response);
				throw new Exception("Unable to find token_type in twitch's response");
			}
			
			if (!response.has("access_token")) {
				System.out.println(response);
				throw new Exception("Unable to find access_token in twitch's response");
			}
			
			if (!response.has("expires_in")) {
				throw new Exception("Unable to find expires_in in twitch's response");
			}
			
//			Successful return:
//			{
//			    "access_token": "_",
//			    "expires_in": 5153665,
//			    "token_type": "bearer"
//			}

			String newBearerToken = response.optString("access_token");
			Long expiresInOffset = response.optLong("expires_in");
			Long newExpiresIn = requestTime + expiresInOffset; // Store absolute expiration time
			
			setTokens(newBearerToken, newExpiresIn);

			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	
	private boolean isClientInformationBlank() {
		boolean clientIdBlank = clientInfo.getClientID() == null || clientInfo.getClientID().isBlank();
		boolean clientSecretBlank = clientInfo.getClientSecret() == null || clientInfo.getClientSecret().isBlank();
		
		return clientIdBlank || clientSecretBlank;
	}
	
	public boolean updateTokens() {
		try {
			
//			if(isClientInformationBlank()) {
//				throw new Exception("Client information is not valid");
//			}
			//Tokens are still valid...
			if (isTokenValid()) {
				return false;
			}
	
			return createNewTokens();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

	public synchronized String getBearerToken() {
		updateTokens();
		return bearerToken;
	}
}
