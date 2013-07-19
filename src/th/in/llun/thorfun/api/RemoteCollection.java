package th.in.llun.thorfun.api;

public class RemoteCollection<E extends RemoteObject> implements RemoteObject {

	protected E[] raws;

	public RemoteCollection(E[] raws) {
		this.raws = raws;
	}
	
	public E[] collection() {
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
