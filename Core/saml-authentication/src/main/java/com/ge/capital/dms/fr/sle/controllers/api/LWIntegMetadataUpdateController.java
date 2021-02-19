package com.ge.capital.dms.fr.sle.controllers.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.codec.binary.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.capital.dms.api.SearchApiController;
import com.ge.capital.dms.fr.sle.model.JSONExcelObject;
import com.ge.capital.dms.service.SelectService;
import com.ge.capital.dms.service.UpdateService;
import org.apache.log4j.Logger;
import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.swagger.annotations.ApiParam;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/secure")
public class LWIntegMetadataUpdateController {

	@SuppressWarnings("unused")
	private final ObjectMapper objectMapper;

	@SuppressWarnings("unused")
	private final HttpServletRequest request;

	@Autowired
	SelectService selectService;

	@Value("${download.path}")
	private String DIRECTORY;

	@Autowired
	EmailPackageController emailPackageController;

	@org.springframework.beans.factory.annotation.Autowired
	public LWIntegMetadataUpdateController(ObjectMapper objectMapper, HttpServletRequest request) {
		this.objectMapper = objectMapper;
		this.request = request;
	}

	@Autowired
	UpdateService updateService;

	private static final Logger log = Logger.getLogger(SearchApiController.class);

	@SuppressWarnings({ "unchecked", "finally", "unused" })
	@RequestMapping(value = "/lwIntegMetadataUpdate", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> metadataUpdatePost(HttpServletRequest request,
			@ApiParam(value = "", required = true) @Valid @RequestBody JSONObject metadata) {
		Map<String, Object> statusObj = new HashMap<String, Object>();
		String message = "";
		int count = 0;
		try {
			HashMap<String, String> metadataMap = new HashMap<String, String>();
			metadataMap = (HashMap<String, String>) metadata.clone();

			if (metadataMap.get("messageType").equalsIgnoreCase("CustomerUpdate")) {
				count = updateService.updateCustomerName(metadataMap.get("partyNumber"), metadataMap.get("partyName"));
			}
			if (metadataMap.get("messageType").equalsIgnoreCase("LOCUpdate")) {
				String decodedPartyName = new String(Base64.decodeBase64(metadataMap.get("partyName")), "ISO-8859-1");
				count = updateService.locUpdate(metadataMap.get("partyNumber"), decodedPartyName,
						metadataMap.get("creditNumber"), metadataMap.get("opportunityID"));
			}
			if ((metadataMap.get("messageType").equalsIgnoreCase("LoanUpdate"))
					|| (metadataMap.get("messageType").equalsIgnoreCase("LeaseUpdate"))) {
				if (metadataMap.containsKey("commencementDate")) {
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					Date date = df.parse(metadataMap.get("commencementDate"));
					Date newRetentionDate = getRetentionDate(date, 10);
					count = updateService.loanUpdate(metadataMap.get("creditNumber"), metadataMap.get("sequenceNumber"),
							newRetentionDate, metadata.toJSONString(), metadataMap.get("messageType"));
				} else {
					Date date = new Date();
					Date newRetentionDate = getRetentionDate(date, 10);
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					count = updateService.loanUpdate(metadataMap.get("creditNumber"), metadataMap.get("sequenceNumber"),
							newRetentionDate, metadata.toJSONString(), metadataMap.get("messageType"));	
				}
				//updateBookingReport(request, metadataMap.get("sequenceNumber"));//uncomment to deploy it for booking report--Nidhish
				
			}
			if (count != 0) {
				statusObj.put("code", "200");
				statusObj.put("name", "update-success");
				statusObj.put("description", "Metadata updated Successfully");
				message = "Metadata updated Successfully";
				// return new ResponseEntity(statusObj, HttpStatus.OK);
			} else {
				statusObj.put("code", "200");
				statusObj.put("name", "Not Updated");
				statusObj.put("description", "Couldn't find the data with the corresponding Metadata");
				message = "Couldn't find the data with the corresponding Metadata";
			}
		} catch (NullPointerException e) {
			statusObj.put("code", "400");
			statusObj.put("name", "Invalid field");
			statusObj.put("description", "Required Field is null/empty in the request");
			message = e.getMessage();
			log.error(e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			statusObj.put("code", "400");
			statusObj.put("name", "Invalid field");
			statusObj.put("description", e.getMessage());
			message = e.getMessage();
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			statusObj.put("code", "500");
			statusObj.put("name", "Invalid field");
			statusObj.put("description", "Error while Updating " + e);
			message = "Error while Updating " + e.getMessage();
			log.error(e.getMessage(), e);
		} finally {
			Map<String, Object> model = new HashMap<>();
			model.put("message", message);
			model.put("status", statusObj);
			return model;
		}
	}

	private void updateBookingReport(HttpServletRequest request, String sequenceNumber)
			throws ParseException, IOException {
		log.info("Booking Report is Updated for Sequence Number" + sequenceNumber);
		List<JSONExcelObject> excelData = new ArrayList<JSONExcelObject>();
		updateService.updateBookingReport(sequenceNumber);
		String data = selectService.getReport(sequenceNumber);
		if (!data.isEmpty() && null != data) {
			data = data.replace("[", "");
			data = data.replace("]", "");
			String[] al = data.split("},");
			for (int i = 0; i < al.length - 1; i++) {
				al[i] = al[i].trim() + "}";
			}
			al[al.length - 1] = al[al.length - 1].trim();
			JSONParser parser = new JSONParser();
			for (String keyValue : al) {
				JSONObject json = (JSONObject) parser.parse(keyValue);
				JSONExcelObject obj = new JSONExcelObject();
				for (Object key : json.keySet()) {
					String metaKey = (String) key;
					String metaValue = (String) json.get(metaKey);
					switch (metaKey) {
					case "id":
						obj.setId(metaValue);
						break;
					case "item":
						obj.setItem(metaValue);
						break;
					case "lwdata":
						obj.setLWData(metaValue);
						break;
					case "additionalComments":
						obj.setAdditionalComments(metaValue);
						break;
					case "applies":
						obj.setApplies(metaValue);
						break;
					case "information":
						obj.setInformation(metaValue);
						break;
					}

				}
				excelData.add(obj);
			}
		}
		createExcel(request, sequenceNumber, excelData);
	}

	private void createExcel(HttpServletRequest request, String sequenceNumber, List<JSONExcelObject> resultList)
			throws IOException {
		File excelFile = new File(DIRECTORY + "\\" + sequenceNumber + "-Booking-Funding-Report.xls");
		FileOutputStream fileOutputStream = new FileOutputStream(excelFile);
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Failed_Invoices");

		XSSFRow headerRow = sheet.createRow((short) 0);
		XSSFFont font = workbook.createFont();
		font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
		XSSFCellStyle style = workbook.createCellStyle();
		style.setFont(font);
		headerRow.setRowStyle(style);
		headerRow.createCell(0).setCellValue("Item");
		headerRow.createCell(1).setCellValue("Applies");
		headerRow.createCell(2).setCellValue("Additional Comments");
		headerRow.createCell(3).setCellValue("Information");
		headerRow.createCell(4).setCellValue("Id");
		headerRow.createCell(5).setCellValue("Leasewave Data");

		int rowNum = 1;
		for (JSONExcelObject temp : resultList) {
			XSSFRow headerRown = sheet.createRow((short) rowNum);
			headerRown.createCell(0).setCellValue(temp.getItem());
			headerRown.createCell(1).setCellValue(temp.getApplies());
			headerRown.createCell(2).setCellValue(temp.getAdditionalComments());
			headerRown.createCell(3).setCellValue(temp.getInformation());
			headerRown.createCell(4).setCellValue(temp.getId());
			headerRown.createCell(5).setCellValue(temp.getLWData());

			rowNum++;
		}
		workbook.write(fileOutputStream);
		if (resultList.size() > 0)
			uploadFile(request, excelFile.getName(), sequenceNumber);
	}

	private void uploadFile(HttpServletRequest request, String fileName, String sequenceNumber) {
		HashMap<String, String> finalPackMetadata = new HashMap<String, String>();
		finalPackMetadata = selectService.getDocMetadata(sequenceNumber);

		emailPackageController.uploadDocument(request, fileName, finalPackMetadata, "Booking");

	}

	private Date getRetentionDate(Date commenceDate, int years) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(commenceDate);
		calendar.add(Calendar.YEAR, years);
		return calendar.getTime();
	}

}