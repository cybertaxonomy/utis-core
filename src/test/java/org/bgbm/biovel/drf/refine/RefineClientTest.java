package org.bgbm.biovel.drf.refine;

import java.io.IOException;
import java.net.URISyntaxException;

import org.bgbm.biovel.drf.checklist.DRFChecklistException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RefineClientTest {
	private static RefineClient refineClient;
	private String jobID;
	private static String csvData;
	
	@BeforeClass 
	public static void setup() {
		refineClient = new RefineClient("90.147.102.41","80");
		csvData = "cola,colb,colc" + System.getProperty("line.separator") +
				"vala1,valb1,valc1" + System.getProperty("line.separator") +
				"vala2,valb2,valc2";
	}
	
	@Test
	public void uploadJobTest() throws IOException, DRFChecklistException, RefineException, URISyntaxException, InterruptedException {
		jobID = refineClient.initJob();
		System.out.println("jobID : " + jobID);
		
		Assert.assertNotNull("jobID should not be null", jobID);
		Assert.assertTrue("jobID should not be an empty string", !jobID.equals(""));
		
		System.out.println("up test jobID : " + jobID);
		String uploadResponse = refineClient.uploadData(csvData, jobID);
		System.out.println("uploadResponse: " + uploadResponse);
		
		System.out.println("checking import");
		boolean checkImportJob = refineClient.checkImportJobStatus(jobID);
		System.out.println("import job status : " + checkImportJob);
		
		System.out.println("initialize parser");
		refineClient.initializeParser(jobID);
		
		System.out.println("update format");
		String updateResponse = refineClient.updateFormat(jobID);
		//System.out.println("update format response : " + updateResponse);
		
		System.out.println("create project");
		refineClient.createProject(jobID);
		
		System.out.println("checking project creation");
		String projectID = refineClient.checkCreateProjectStatus(jobID);
		
		String data = refineClient.exportData(projectID);
		System.out.println(data);
		
		if(projectID != null) {
			System.out.println("deleting project id : " + projectID);
			refineClient.deleteProject(projectID);			
		}				
	}
	
	
	

}


