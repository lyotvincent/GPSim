package model;
import java.util.ArrayList;
import java.util.List;

public class OntEntry {
	
	String id;
	String label;
	String define;
	List<OntEntry> parents;
	List<OntEntry> children;
	boolean isOMIM;
	int type;
	
	
	public OntEntry() {
		super();
		parents = new ArrayList<>();
		children = new ArrayList<>();
	}

	public OntEntry(String id, String label, String define, List<OntEntry> parents,
			List<OntEntry> children) {
		super();
		this.id = id;
		this.label = label;
		this.define = define;
		this.parents = parents;
		this.children = children;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDefine() {
		return define;
	}

	public void setDefine(String define) {
		this.define = define;
	}

	public List<OntEntry> getParents() {
		return parents;
	}

	public void setParents(List<OntEntry> parents) {
		this.parents = parents;
	}

	public List<OntEntry> getChildren() {
		return children;
	}

	public void setChildren(List<OntEntry> children) {
		this.children = children;
	}

	public boolean isOMIM() {
		return isOMIM;
	}

	public void setOMIM(boolean isOMIM) {
		this.isOMIM = isOMIM;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	
}
