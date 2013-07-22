package th.in.llun.thorfun.api.model;

public class StringRemoteObject implements RemoteObject {

	private String mOutput = "";

	public StringRemoteObject(String output) {
		mOutput = output;
	}

	@Override
	public String rawString() {
		return mOutput;
	}

}
