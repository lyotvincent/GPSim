package calSim;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jdbc.BaseDao;
import model.OntEntry;
import calSim.ExtractDo;

public class GpSim {
	
	Connection conn = null;
	Map<String,List<String>> genemap = new HashMap<>();
	Map<String,Set<String>> hpomap = new HashMap<>();

	public static void main(String[] args) {
		GpSim fs = new GpSim();
		fs.sim();
		System.out.println("end");
	}
	
	public void sim(){
		//get all diseases
		List<OntEntry> dolist = ExtractDo.getDisease("./HumanDO.owl");
		
		int len = dolist.size();
		float[][] matrix = new float[len][len];
		float w = 0.9f;
		
		PreparedStatement genepstmt = null;
		PreparedStatement hpopstmt = null;
        ResultSet rs = null;
        String genesql = "select distinct gene_id from disease_gene2 where disease_id = ?";
        String hposql = "select distinct hpo_id from disease_hpo2 where disease_id = ?";
        
      //get the disease-gene and disease-hpo associations of all diseases
        try {
        	conn = BaseDao.getConnection();
			genepstmt = conn.prepareStatement(genesql);
			hpopstmt = conn.prepareStatement(hposql);
			for(OntEntry e : dolist){
				//get gene sets
				List<String> gene = new ArrayList<>();
				genepstmt.setString(1, e.getId());
				rs = genepstmt.executeQuery();
				while(rs.next()) {
					gene.add(rs.getString(1));
				}
				genemap.put(e.getId(), gene);
				rs.close();
				
				//get phenotype set
				Set<String> hpo = new HashSet<>();
				hpopstmt.setString(1, e.getId());
				rs = hpopstmt.executeQuery();
				while(rs.next()) {
					hpo.add(rs.getString(1));
				}
				hpomap.put(e.getId(), hpo);
				rs.close();
			}
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
        //start to compute disease similarities
		for(int i=0;i<dolist.size();i++){
			long t1 = new Date().getTime();
			for(int j =i+1;j<dolist.size();j++){
				//the similarity of gene sets
				float numg = geneSetSim(dolist.get(i),dolist.get(j));
				//the similarity of phenotype sets
				float nump = hpoSetSim(dolist.get(i),dolist.get(j));
				//integrate
				matrix[i][j] = w*numg + (1-w)*nump;
			}
			writePerRow(matrix,i);
			long t2 = new Date().getTime();
			System.out.println(i+" trained. Time: "+((t2-t1)*1.0/1000)+"s");
		}
	}


	private float geneSetSim(OntEntry e1, OntEntry e2) {
		List<String> gene1 = genemap.get(e1.getId());
		List<String> gene2 = genemap.get(e2.getId());
		return simSet(gene1,gene2);
	}
	
	private float simSet(List<String> gene1, List<String> gene2) {
		float sim = 0.0f;
		if(gene1.size() == 0||gene2.size()==0){
			return sim;
		}
		for(String g : gene1){
			sim += maxg(g,gene2);
		}
		for(String g : gene2){
			sim += maxg(g,gene1);
		}
		return sim/(gene1.size()+gene2.size());
	}

	private float maxg(String g, List<String> gene2) {
		PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        if(gene2.contains(g)){
        	return 1.0f;
        }
        
        String sql = "select similarity from gene_sim where "
        		+ "( gene_id1 = ? and gene_id2 = ? ) or ( gene_id1 = ? and gene_id2 = ? )";
        float max = 0.0f;
        try {
			pstmt = conn.prepareStatement(sql);
			for(String gi : gene2){
				pstmt.setString(1, gi);
				pstmt.setString(2, g);
				pstmt.setString(3, g);
				pstmt.setString(4, gi);
				rs = pstmt.executeQuery();
				if(rs.next()){
					float sim = rs.getFloat(1);
					if(sim > max){
						max = sim;
					}
				}
				rs.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				BaseDao.closeAll(null, pstmt, rs);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return max;
	}
	
	private float hpoSetSim(OntEntry e1, OntEntry e2) {
		Set<String> hpo1 = hpomap.get(e1.getId());
		Set<String> hpo2 = hpomap.get(e2.getId());
		if(hpo1.size()==0 || hpo2.size()==0){
			return 0.0f;
		}
		
		Set<String> inter = new HashSet<>();
		inter.addAll(hpo1);
		inter.retainAll(hpo2);
		return 2*inter.size()*1.0f/(hpo1.size()+hpo2.size());
	}

	public  void writePerRow(float[][] matrix,int i) {
		String path = "./similarity.txt";
		BufferedWriter out = null;     
        try {     
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, true)));     
            StringBuffer sb = new StringBuffer();
            for(int j = i+1;j<matrix[i].length;j++) {
            	if(sb.length()==0) {
            		sb.append(matrix[i][j]);
            	}else {
            		sb.append("\t"+matrix[i][j]);
            	}
            }
            sb.append("\n");
            out.write(sb.toString());  
        } catch (Exception e) {     
            e.printStackTrace();     
        } finally {     
            try {     
                if(out != null){  
                    out.close();     
                }  
            } catch (IOException e) {     
                e.printStackTrace();     
            }     
        } 
	}
}
