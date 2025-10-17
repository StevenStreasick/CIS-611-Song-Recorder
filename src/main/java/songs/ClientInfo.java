package songs;

public class ClientInfo {
	private String clientID;
	private String clientSecret;
	
	public ClientInfo() {
		
	}
	
	public ClientInfo(String clientID, String clientSecret) {
		this.clientID = clientID;
		this.clientSecret = clientSecret;
	}
	
	protected String getClientID() {
		return clientID;
	}
	
	protected boolean setClientId(String clientId) {
		try {
			if (clientId.isBlank()) {
				throw new Exception("Client Id cannot be blank");
			}

			this.clientID = clientId;
//			tokenManager.setClientId(clientID);


			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	
	protected String getClientSecret() {
		return clientSecret;
	}
	
	protected boolean setClientSecret(String clientSecret) {
		try {
			if (clientSecret.isBlank()) {
				throw new Exception("Client Id cannot be blank");
			}

			this.clientSecret = clientSecret;
//			tokenManager.setClientSecret(clientSecret);

			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
}
