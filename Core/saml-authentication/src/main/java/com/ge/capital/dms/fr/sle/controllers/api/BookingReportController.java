package com.ge.capital.dms.fr.sle.controllers.api;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.capital.dms.fr.sle.model.JSONExcelObject;
import com.ge.capital.dms.service.SelectService;
import com.ge.capital.dms.service.UpdateService;
import com.google.gson.Gson;

@CrossOrigin(origins = "*", exposedHeaders = "message")
@RestController
@RequestMapping("/secure")
public class BookingReportController {

	@Value("${XML.file.path}")
	private String XML_FILE_PATH;

	@Autowired
	UpdateService updateService;

	@Autowired
	SelectService selectService;

	private static final Logger log = Logger.getLogger(BookingReportController.class);

	List<String> lwData;

	List<JSONExcelObject> excelData;

	Map<String, List<String>> finalNodeMap;

	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@RequestMapping(value = "/bookingFundingReport")
	@ResponseBody
	public List<JSONExcelObject> bookingReport(HttpServletRequest httprequest, @RequestBody String sequenceNumber)
			throws Exception {
		this.excelData = new ArrayList<JSONExcelObject>();
		// this.lwData = new ArrayList<String>();
		// this.finalNodeMap = new HashMap<String, List<String>>();
		// try {
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
		} else {
			Runtime.getRuntime().exec("java -jar D:\\DMSAmerica\\BookingReportXML.jar " + sequenceNumber);
			Thread.sleep(6000);
			if (new File(XML_FILE_PATH + "\\" + sequenceNumber + ".xml").exists()) {

			} else {
				throw new Exception("Sequence Number " + sequenceNumber + " not Found ");
			}
		}
		/*
		 * } catch (Exception e) { log.error(e.getMessage(), e); }
		 */
		return excelData;
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@RequestMapping(value = "/getDataFromLeasewave")
	@ResponseBody
	public List<JSONExcelObject> getDataFromLeasewave(HttpServletRequest httprequest,
			@RequestBody String sequenceNumber) throws Exception {
		this.excelData = new ArrayList<JSONExcelObject>();
		this.lwData = new ArrayList<String>();
		this.finalNodeMap = new HashMap<String, List<String>>();
		if (new File(XML_FILE_PATH + "\\" + sequenceNumber + ".xml").exists()) {
			parseXML(sequenceNumber);
			readFromJson();
			prepareJSON();
		} else {
			Runtime.getRuntime().exec("java -jar D:\\DMSAmerica\\BookingReportXML.jar " + sequenceNumber);
			Thread.sleep(6000);
			if (new File(XML_FILE_PATH + "\\" + sequenceNumber + ".xml").exists()) {
				parseXML(sequenceNumber);
				readFromJson();
				prepareJSON();
			} else {
				throw new Exception("Sequence Number " + sequenceNumber + " not Found ");
			}
			// throw new Exception("Sequence Number not Found " + sequenceNumber);
		}

		return excelData;
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@RequestMapping(value = "/saveBookingReport")
	@ResponseBody
	public String saveBookingReport(HttpServletRequest httprequest, @RequestBody List<JSONExcelObject> tableData)
			throws JsonProcessingException {
		String response = "";
		List<String> finalStrings = new ArrayList<String>();
		String sequenceNumber = httprequest.getHeader("seqNum").trim();
		ObjectMapper mapper = new ObjectMapper();
		for (JSONExcelObject obj : tableData) {
			String jsonString = mapper.writeValueAsString(obj);
			finalStrings.add(jsonString);
		}
		response = updateService.saveBookingReport(sequenceNumber, finalStrings);
		log.info("sequenceNumber :" + sequenceNumber);
		return response;
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@RequestMapping(value = "/getPermission")
	@ResponseBody
	public String PermissionReturn(HttpServletRequest httprequest, @RequestBody String sequenceNumber)
			throws JsonProcessingException {

		String response = selectService.getPermission(sequenceNumber);
		log.info("sequenceNumber :" + sequenceNumber);
		return response;
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@RequestMapping(value = "/refreshFromLeasewave")
	@ResponseBody
	public List<JSONExcelObject> refreshFromLeasewave(HttpServletRequest httprequest,
			@RequestBody List<JSONExcelObject> tableData) throws Exception {
		this.excelData = new ArrayList<JSONExcelObject>();
		this.lwData = new ArrayList<String>();
		this.finalNodeMap = new HashMap<String, List<String>>();
		this.excelData = tableData;
		String sequenceNumber = httprequest.getHeader("seqNum").trim();
		Runtime.getRuntime().exec("java -jar D:\\DMSAmerica\\BookingReportXML.jar " + sequenceNumber);
		Thread.sleep(6000);
		if (new File(XML_FILE_PATH + "\\" + sequenceNumber + ".xml").exists()) {
			parseXML(sequenceNumber);
			prepareJSONRefresh();
		} else {
			throw new Exception("Sequence Number " + sequenceNumber + " not Found ");
		}
		return this.excelData;
	}

	private void prepareJSONRefresh() {
		reconcileLWData();
		for (JSONExcelObject ob : excelData) {
			if (ob.getId().equals("3") || ob.getId().equals("4") || ob.getId().equals("5") || ob.getId().equals("6")
					|| ob.getId().equals("7") || ob.getId().equals("11") || ob.getId().equals("14")
					|| ob.getId().equals("16") || ob.getId().equals("20") || ob.getId().equals("21")
					|| ob.getId().equals("22")) {
				String lwvar = String.join("\n", finalNodeMap.get(ob.getId()));
				if (!lwvar.trim().isEmpty()) {
					ob.setLWData(lwvar);
					if (null == ob.getApplies() || ob.getApplies().isEmpty()) {
						int len = finalNodeMap.get(ob.getId()).size();
						int count = 0;
						for (String str : finalNodeMap.get(ob.getId())) {
							if (str.contains("null"))
								count++;
						}
						if (len == count)
							ob.setApplies("N");
						else
							ob.setApplies("Y");
					}
				}
			}
		}
	}

	private void prepareJSON() {
		reconcileLWData();
		for (JSONExcelObject ob : excelData) {
			// System.out.println(finalNodeMap.get(ob.getId()));
			if (ob.getId().equals("3")) {
				int len = finalNodeMap.get(ob.getId()).size();
				int count = 0;
				// commented due to change in requirement in Blendid items
				// double amount = 0;

				for (String str : finalNodeMap.get(ob.getId())) {
					if (str.contains("null")) {
						count++;
					}
					/*
					 * if (str.contains("BlendedItemsAmount=") && !str.contains("null")) { amount =
					 * amount + Double.parseDouble(str.replace("BlendedItemsAmount=", "")); }
					 */
				}

				// String addComm = ob.getAdditionalComments();
				// ob.setAdditionalComments(addComm.replace("$_____", "$" +
				// String.valueOf(amount)));
				String lwvar = String.join("\n", finalNodeMap.get(ob.getId()));
				ob.setLWData(lwvar);
				if (len == count)
					ob.setApplies("N");
				else
					ob.setApplies("Y");
			}
			if (ob.getId().equals("4")) {
				int len = finalNodeMap.get(ob.getId()).size();
				int count = 0;
				for (String str : finalNodeMap.get(ob.getId())) {
					if (str.contains("null")) {
						count++;
					}
				}
				String lwvar = String.join("\n", finalNodeMap.get(ob.getId()));
				ob.setLWData(lwvar);
				if (len == count)
					ob.setApplies("N");
				else
					ob.setApplies("Y");
			}
			if (ob.getId().equals("5")) {
				int len = finalNodeMap.get(ob.getId()).size();
				int count = 0;
				for (String str : finalNodeMap.get(ob.getId())) {
					if (str.contains("null")) {
						count++;
					}
				}
				String lwvar = String.join("\n", finalNodeMap.get(ob.getId()));
				ob.setLWData(lwvar);
				if (len == count)
					ob.setApplies("N");
				else
					ob.setApplies("Y");
			}
			if (ob.getId().equals("6")) {
				int len = finalNodeMap.get(ob.getId()).size();
				int count = 0;
				for (String str : finalNodeMap.get(ob.getId())) {
					if (str.contains("null")) {
						count++;
					}
				}
				String lwvar = String.join("\n", finalNodeMap.get(ob.getId()));
				ob.setLWData(lwvar);
				if (len == count)
					ob.setApplies("N");
				else
					ob.setApplies("Y");
			}
			if (ob.getId().equals("7")) {
				int len = finalNodeMap.get(ob.getId()).size();
				int count = 0;
				for (String str : finalNodeMap.get(ob.getId())) {
					if (str.contains("null")) {
						count++;
					}
				}
				String lwvar = String.join("\n", finalNodeMap.get(ob.getId()));
				ob.setLWData(lwvar);
				if (len == count)
					ob.setApplies("N");
				else
					ob.setApplies("Y");
			}
			if (ob.getId().equals("11")) {
				int len = finalNodeMap.get(ob.getId()).size();
				int count = 0;
				for (String str : finalNodeMap.get(ob.getId())) {
					if (str.contains("null")) {
						count++;
					}
				}
				String lwvar = String.join("\n", finalNodeMap.get(ob.getId()));
				ob.setLWData(lwvar);
				if (len == count)
					ob.setApplies("N");
				else
					ob.setApplies("Y");
			}
			if (ob.getId().equals("14")) {
				String lwvar = String.join("\n", finalNodeMap.get(ob.getId()));
				ob.setLWData(lwvar);
				if (lwvar.endsWith("1"))
					ob.setApplies("Y");
				else
					ob.setApplies("N");
			}
			if (ob.getId().equals("16")) {
				int len = finalNodeMap.get(ob.getId()).size();
				int count = 0;
				double amount = 0;
				for (String str : finalNodeMap.get(ob.getId())) {
					if (str.contains("null")) {
						count++;
					}
					if (str.contains("TotalUpfrontTaxAmount=") && !str.contains("null")) {
						amount = amount + Double.parseDouble(str.replace("TotalUpfrontTaxAmount=", ""));
					}
				}
				String addComm = ob.getAdditionalComments();
				ob.setAdditionalComments(addComm.replace("$_____", "$" + String.valueOf(amount)));
				String lwvar = String.join("\n", finalNodeMap.get(ob.getId()));
				ob.setLWData(lwvar);
				if (len == count)
					ob.setApplies("N");
				else
					ob.setApplies("Y");
			}
			if (ob.getId().equals("20")) {
				int len = finalNodeMap.get(ob.getId()).size();
				int count = 0;
				for (String str : finalNodeMap.get(ob.getId())) {
					if (str.contains("null")) {
						count++;
					}
				}
				String lwvar = String.join("\n", finalNodeMap.get(ob.getId()));
				ob.setLWData(lwvar);
				if (len == count)
					ob.setApplies("N");
				else
					ob.setApplies("Y");
			}
			if (ob.getId().equals("21")) {
				int len = finalNodeMap.get(ob.getId()).size();
				int count = 0;
				for (String str : finalNodeMap.get(ob.getId())) {
					if (str.contains("null")) {
						count++;
					}
				}
				String lwvar = String.join("\n", finalNodeMap.get(ob.getId()));
				ob.setLWData(lwvar);
				if (len == count)
					ob.setApplies("N");
				else
					ob.setApplies("Y");
			}
			if (ob.getId().equals("22")) {
				int len = finalNodeMap.get(ob.getId()).size();
				int count = 0;
				for (String str : finalNodeMap.get(ob.getId())) {
					if (str.contains("null")) {
						count++;
					}
				}
				String lwvar = String.join("\n", finalNodeMap.get(ob.getId()));
				ob.setLWData(lwvar);
				if (len == count)
					ob.setApplies("N");
				else
					ob.setApplies("Y");

			}
		}
	}

	private void reconcileLWData() {
		finalNodeMap.put("3", new ArrayList<String>());
		finalNodeMap.put("4", new ArrayList<String>());
		finalNodeMap.put("5", new ArrayList<String>());
		finalNodeMap.put("6", new ArrayList<String>());
		finalNodeMap.put("7", new ArrayList<String>());
		finalNodeMap.put("11", new ArrayList<String>());
		finalNodeMap.put("14", new ArrayList<String>());
		finalNodeMap.put("16", new ArrayList<String>());
		finalNodeMap.put("20", new ArrayList<String>());
		finalNodeMap.put("21", new ArrayList<String>());
		finalNodeMap.put("22", new ArrayList<String>());
		for (String str : lwData) {
			str = str.trim();
			if (str.startsWith("Adv_AdvRent") || str.startsWith("Adv_DownPaymentAmount")) {
				// commented due to change in requirement in Blendid items
				// || str.startsWith("Adv_BlendedItemsName") ||
				// str.startsWith("Adv_BlendedItemsAmount")
				str = str.replace("Adv_", "");
				if (str.endsWith("=")) {
					str = str + "<null>";
				}
				finalNodeMap.get("3").add(str);
			}
			if (str.startsWith("servicemaint_PayableRemitTo") || str.startsWith("servicemaint_PayableAmount")
					|| str.startsWith("servicemaint_ReceivableCode")) {
				str = str.replace("servicemaint_", "");
				if (str.endsWith("=")) {
					str = str + "<null>";
				}
				finalNodeMap.get("4").add(str);

			}
			if (str.startsWith("TRICE_PayableRemitTo") || str.startsWith("TRICE_PayableAmount")
					|| str.startsWith("TRICE_ReceivableCode")) {
				str = str.replace("TRICE_", "");
				if (str.endsWith("=")) {
					str = str + "<null>";
				}
				finalNodeMap.get("5").add(str);

			}
			if (str.startsWith("ratebuydown_BlendedItemsName") || str.startsWith("ratebuydown_BlendedItemsFeesAmount")
					|| str.startsWith("ratebuydown_description")) {
				str = str.replace("ratebuydown_", "");
				if (str.endsWith("=")) {
					str = str + "<null>";
				}
				finalNodeMap.get("6").add(str);

			}
			if (str.startsWith("tradein_sequencenumber") || str.startsWith("tradein_tradeintreatmentcode")) {
				str = str.replace("tradein_", "");
				if (str.endsWith("=")) {
					str = str + "<null>";
				}
				finalNodeMap.get("7").add(str);

			}
			if (str.startsWith("othercomments_sequencenumber")
					|| str.startsWith("othercomments_tradeintreatmentcode")) {
				str = str.replace("othercomments_", "");
				if (str.endsWith("=")) {
					str = str + "<null>";
				}
				finalNodeMap.get("11").add(str);

			}
			if (str.startsWith("FLDocStamps_Docsignedinfl")) {
				str = str.replace("FLDocStamps_", "");
				if (str.endsWith("=")) {
					str = str + "<null>";
				}
				finalNodeMap.get("14").add(str);

			}
			if (str.startsWith("UpfrontSalesTax_LegalGoverningState")
					|| str.startsWith("UpfrontSalesTax_TotalUpfrontTaxAmount")) {
				str = str.replace("UpfrontSalesTax_", "");
				if (str.endsWith("=")) {
					str = str + "<null>";
				}
				finalNodeMap.get("16").add(str);

			}
			if (str.startsWith("welcomepackage_ContractContactFullName")
					|| str.startsWith("welcomepackage_ContractContactType")
					|| str.startsWith("welcomepackage_ContractContactPhoneNumber")
					|| str.startsWith("welcomepackage_ContractContactEmail")
					|| str.startsWith("welcomepackage_ParalegalName")) {
				str = str.replace("welcomepackage_", "");
				if (str.endsWith("=")) {
					str = str + "<null>";
				}
				finalNodeMap.get("20").add(str);

			}
			if (str.startsWith("paymentcredits_TotalProgressPaymentCreditForDocAmount")
					|| str.startsWith("paymentcredits_OriginalCapitalizedAmount")) {
				str = str.replace("paymentcredits_", "");
				if (str.endsWith("=")) {
					str = str + "<null>";
				}
				finalNodeMap.get("21").add(str);

			}
			if (str.startsWith("contractoptions_EBOOptionDate") || str.startsWith("contractoptions_EBOAmount")
					|| str.startsWith("contractoptions_EBOMonth") || str.startsWith("contractoptions_ContractOption")
					|| str.startsWith("contractoptions_ContractOptionTerms")
					|| str.startsWith("contractoptions_PurchaseFactor")) {
				str = str.replace("contractoptions_", "");
				if (str.endsWith("=")) {
					str = str + "<null>";
				}
				finalNodeMap.get("22").add(str);
			}

		}

	}

	private void readFromJson() {
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader(XML_FILE_PATH + "\\Data.json"));

			JSONObject jsonObject = (JSONObject) obj;
			JSONArray jsonArray = (JSONArray) jsonObject.get("Sheet1");

			for (int i = 0; i < jsonArray.size(); i++) {
				@SuppressWarnings("unchecked")
				HashMap<String, Object> yourHashMap = new Gson().fromJson(jsonArray.get(i).toString(), HashMap.class);
				JSONExcelObject objJson = new JSONExcelObject();
				objJson.setId((String) yourHashMap.get("#"));
				objJson.setItem((String) yourHashMap.get("Item"));
				objJson.setApplies((String) yourHashMap.get("Applies"));
				objJson.setInformation((String) yourHashMap.get("Information"));
				objJson.setAdditionalComments((String) yourHashMap.get("AdditionalComments"));
				excelData.add(objJson);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void parseXML(String sequenceNumber) {
		try {
			File file = new File(XML_FILE_PATH + "\\" + sequenceNumber + ".xml");
			// File file = new File("D:\\TECHM\\GJ557822\\Downloads\\691801124-98.xml");
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			if (doc.hasChildNodes()) {
				printNote(doc.getChildNodes());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

	private void printNote(NodeList nodeList) {

		for (int count = 0; count < nodeList.getLength(); count++) {
			Node tempNode = nodeList.item(count);
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
				lwData.add(tempNode.getNodeName() + "=" + tempNode.getTextContent());
				if (tempNode.hasChildNodes()) {
					printNote(tempNode.getChildNodes());
				}

			}

		}
	}
}
