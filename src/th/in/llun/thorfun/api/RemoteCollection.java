package th.in.llun.thorfun.api;

import java.util.List;

public class RemoteCollection<E extends RemoteObject> implements RemoteObject {

	protected List<E> raws;

	public RemoteCollection(List<E> raws) {
		this.raws = raws;
	}

	public List<E> collection() {
		return this.raws;
	}

	public String rawString() {
		StringBuilder builder = new StringBuilder("[");
		for (RemoteObject raw : raws) {
			builder.append(String.format("%s,", raw));
		}
		builder.deleteCharAt(builder.length() - 1);
		builder.append("]");
		return builder.toString();
	}

}
