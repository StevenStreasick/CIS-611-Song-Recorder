package songs;

interface StreamObserver {
	void onStreamStart(String startTime);
	void onStreamEnd();
}	
