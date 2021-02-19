package com.ge.capital.dms.fr.sle.controllers.api;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.ge.capital.dms.fr.sle.controllers.api.MarkAsFinalRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.ge.capital.dms.api.MarkasfinalApi;
import com.ge.capital.dms.api.MarkasfinalApiController;
import com.ge.capital.dms.fr.sle.config.DecodeSSO;
import com.ge.capital.dms.model.MarkAsFinalIVO;
import com.ge.capital.dms.repository.DocSubTypeRepository;
import com.ge.capital.dms.service.UpdateService;
import com.ge.capital.dms.utility.DmsUtilityConstants;
import com.ge.capital.dms.utility.DmsUtilityService;
import com.google.gson.Gson;

import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-08-14T22:22:38.380+05:30")

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/secure")
public class MarkAsFinalController implements MarkasfinalApi{
	
	@Autowired
	MarkAsFinalRestController markAsFinalRestController;
	
	@Autowired
	UpdateService updateService;
	
	@Autowired
	private DmsUtilityService dmsUtilityService;	
	
	@Autowired
	HttpServletRequest request;
	
	@Autowired
	DocSubTypeRepository docSubTypeRepo;
	
	@Autowired
	DecodeSSO decodeSSO;
		
	private static final Logger log = Logger.getLogger(MarkasfinalApiController.class);
	
	
	@SuppressWarnings({ "unused", "unchecked" })
	@RequestMapping(value = "/markasfinal", method = RequestMethod.POST)
	@ResponseBody
	public Object markasfinalMetadataPost(
			@ApiParam(value = "", required = true) @Valid @RequestBody List<MarkAsFinalIVO> multipleMetadata, @RequestHeader HttpHeaders headers) {
		
		String URL_MarkasFinal_Update=null;
		boolean respflg = false;
		String respMsg = null;
		try
		{
			String loggedinUser=decodeSSO.getDecodedSSO(request.getHeader("loggedinuser"));
			RestTemplate restTemplate = new RestTemplate();
			
			Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.applicationConstantsResource);
	
			URL_MarkasFinal_Update = props.getProperty("updateMetadaUri");
			
			if(multipleMetadata.isEmpty())
			{
				try {
						respflg = false;					
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				for(Iterator<MarkAsFinalIVO> i = multipleMetadata.iterator(); i.hasNext(); ) {
					
					MarkAsFinalIVO metadata = i.next();				
		
					Map<String, String> inputMetadataMap = (Map<String, String>) metadata.getMetadata();
					String inDocType = "";
					String inRetentionDate = "";
					String inDocSubType = "";
					if(inputMetadataMap.get("docType") != null) {
						inDocType = inputMetadataMap.get("docType");
						log.info(inDocType);
						inputMetadataMap.remove("docType");	
					}
					if(inputMetadataMap.get("retentionDate") != null ) {
						inRetentionDate = inputMetadataMap.get("retentionDate");
						log.info(inRetentionDate);
						inputMetadataMap.remove("retentionDate");	
					}
					if(inputMetadataMap.get("docSubType") != null) {
						inDocSubType = inputMetadataMap.get("docSubType");
						inputMetadataMap.remove("docSubType");	
					}
					
					Date commenceDate = new Date();
					Date newRetentionDate=getRetentionDate(commenceDate,10);
					
				    DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					if(!inDocType.equalsIgnoreCase("Contract")) {				    
					    inputMetadataMap.put("permName","Capital_DMS_HEF_FINAL_MANAGER");
						inputMetadataMap.put("docState","FINAL");
						inputMetadataMap.put("retentionDate",df.format(newRetentionDate));
						inputMetadataMap.put("modifier", loggedinUser);
						inputMetadataMap.put("modifyDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
							
					} if(inDocType.equalsIgnoreCase("Contract") && inRetentionDate.isEmpty()) {
					 	inputMetadataMap.put("docState","FINAL");
					 	inputMetadataMap.put("modifier", loggedinUser);
						inputMetadataMap.put("modifyDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
						log.info(inputMetadataMap.get("modifyDate"));
					} else if(inDocType.equalsIgnoreCase("Contract") && !inRetentionDate.isEmpty()) {
						Date temp = new SimpleDateFormat("dd/MM/yyyy").parse(inRetentionDate);
						Date newRetentionDate1 = getRetentionDate(temp,9);
						
						if(inDocSubType.equalsIgnoreCase("Bookings")) {
							inDocSubType = "Booking";
						}
						if(this.docSubTypeRepo.isFinal(inDocSubType) != null) {
							if(!this.docSubTypeRepo.isFinal(inDocSubType).equals("TRUE")) {
								inputMetadataMap.put("permName","Capital_DMS_HEF_FINAL_MANAGER");
								inputMetadataMap.put("docState","FINAL");
								inputMetadataMap.put("retentionDate",df.format(newRetentionDate1));
								inputMetadataMap.put("modifier", loggedinUser);
								inputMetadataMap.put("modifyDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
							} else {
								inputMetadataMap.put("docState","FINAL");
							}
						}

					}
					
					markAsFinalRestController.markAsFinalUpdate(URL_MarkasFinal_Update, multipleMetadata);
					respflg = true;
					log.debug("Updated metadata for docid : "+inputMetadataMap.get("docId"));
					
				}
			}			
		
		} catch (Exception e) {
			respflg = false;
			log.error(e.getMessage(), e);
			
		}
		
		if (respflg == true)
			respMsg = "Updated successfully";
		else
			respMsg = "Something went wrong could not process 'Mark as Final'";
		
		Gson g = new Gson();
		String Jsonstr = g.toJson(respMsg);
		return Jsonstr;
	}

	private Date getRetentionDate(Date commenceDate, int years){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(commenceDate);
		calendar.add(Calendar.YEAR, years);
		return calendar.getTime();
	}

	@Override
	public ResponseEntity<Void> markasfinalMetadataPost(MarkAsFinalIVO metadata) {
		return null;
	}

}
