public class StateMachineServer {

	private static ServerStates currState;

	public StateMachineServer() {
	}

	public static void switchStates() {
		if (currState == ServerStates.LISTEN) {
			currState = ServerStates.SYN_RCV;
		} else if (currState == ServerStates.SYN_RCV) {
			currState = ServerStates.EST;
		} else if (currState == ServerStates.EST) {
			currState = ServerStates.CLOSE_WAIT;
		} else if (currState == ServerStates.CLOSE_WAIT) {
			currState = ServerStates.LAST_ACK;
		} else if (currState == ServerStates.LAST_ACK) {
			currState = ServerStates.FINISHED;
		}
	}

	public static ServerStates getCurrState() {
		return currState;
	}

	public static void setCurrState(ServerStates state) {
		currState = state;
	}

}