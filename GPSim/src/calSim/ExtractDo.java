package calSim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import model.OntEntry;

public class ExtractDo {
	
	private static Map<String,OntEntry> map = new HashMap<>();//来自DO文件的疾病信息
	
	public static List<OntEntry> getDisease(String path) {
		List<OntEntry> doList = new ArrayList<>();
		String idPrefix = "DOID";
		
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		//生成一个本体模型
		model.read(path,"");
		
		for (Iterator<?> i = model.listClasses(); i.hasNext();) {
			OntClass c = (OntClass) i.next(); // 返回类型强制转换
			if (!c.isAnon()) {//如果不是匿名类
				String tmpId = c.getModel().getGraph().getPrefixMapping().shortForm(c.getURI()).substring(4).replace("_", ":");
				if(tmpId.contains(idPrefix)) {
					doList.add(convert2OntE(c,map));
				}
			}
		}
		
		return doList;
	}
	
	private static OntEntry convert2OntE(OntClass c, Map<String, OntEntry> map) {
		String idPrefix = "DOID";
		OntEntry cls = null;
		String cid = c.getModel().getGraph().getPrefixMapping().shortForm(c.getURI()).substring(4).replace("_", ":");
		String define = null;
		String label = null;
		boolean isOMIM = false;
		
		StmtIterator iterator = c.listProperties();
		while(iterator.hasNext()){
			Statement statement = iterator.next();
            String predict = statement.getPredicate().toString().substring(statement.getPredicate().toString().indexOf("#")+1);
            String object = statement.getObject().toString();
		    if(predict.contains("IAO_0000115")) {
		    	define = object;
		    	while(define.endsWith(".")) {
		    		define = define.substring(0, define.length()-1);
		    	}
		    }
		    if(predict.contains("label")) {
		    	label = object;
		    }
		    if(predict.contains("hasDbXref") && object.contains("OMIM")) {
		    	isOMIM = true;
		    }
		    if(cid!=null && label !=null && define!=null && isOMIM) {
		    	break;
		    }
		}
		if(cid!=null && label!=null) {
	    	if(define==null) {
	    		 define = label;
	    	}
	    	if(map.containsKey(cid)) {
	    		cls =  map.get(cid);
	    	}else {
	    		cls = new OntEntry();
	    		cls.setId(cid);
	    	}
	    	cls.setLabel(label);
	    	cls.setDefine(define);
	    	cls.setOMIM(isOMIM);
		}
		// 迭代显示当前类的直接父类
		for (Iterator<?> it = c.listSuperClasses(); it.hasNext();){
			OntClass sp = (OntClass) it.next();
		    //得到的id号为obo:SYMP_0000743 因此要去掉obo:因此从3开始截取
			if(sp.getURI()!=null) {
				String pId = sp.getModel().getGraph().getPrefixMapping().shortForm(sp.getURI()).substring(4).replace("_", ":");
				if(cid!=null && pId.contains(idPrefix)) {
					if(map.containsKey(pId)){
						cls.getParents().add(map.get(pId));
					}else {
						OntEntry p = new OntEntry();
						p.setId(pId);
						cls.getParents().add(p);
						map.put(pId, p);
					}
					
				}
			}
        }
		//迭代显示当前类的直接子类
		for (Iterator<?> it = c.listSubClasses(); it.hasNext();) {
			OntClass sb = (OntClass) it.next();
			if(sb.getURI()!=null) {
				String cId = sb.getModel().getGraph().getPrefixMapping().shortForm(sb.getURI()).substring(4).replace("_", ":");
				if(cid!=null && cId.contains(idPrefix)) {
					if(map.containsKey(cId)){
						cls.getChildren().add(map.get(cId));
					}else {
						OntEntry child = new OntEntry();
						child.setId(cId);
						cls.getChildren().add(child);
						map.put(cId, child);
					}
				}
			}
		}
		map.put(cid, cls);
		return cls;
	}

	public static Map<String, OntEntry> getMap() {
		return map;
	}

	public static void setMap(Map<String, OntEntry> map) {
		ExtractDo.map = map;
	}
	
	
}
