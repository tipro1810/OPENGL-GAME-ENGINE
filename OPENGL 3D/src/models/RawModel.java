package models;

public class RawModel {
	private int ID;
	private int vertexCount;

	public RawModel(int ID, int vertexCount) {
		this.ID = ID;
		this.vertexCount = vertexCount;
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
	
	public int getVaoID() {
		return ID;
	}
}
