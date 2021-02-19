package com.ge.capital.dms.fr.sle.controllers.api;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ge.capital.dms.fr.sle.config.DecodeSSO;

import org.apache.poi.common.usermodel.Hyperlink;

/**
 * @author GJ00557822
 */
@CrossOrigin(origins = "*", exposedHeaders = "fileName")
@RestController
@RequestMapping("/secure")
public class SearchResultExcel {

	private static final Logger log = Logger.getLogger(SearchResultExcel.class);

	@Autowired
	DecodeSSO decodeSSO;
	
	@Value("${download.path}")
	private String DIRECTORY;

	@Value("${deal.headers}")
	private String DEAL_HEADERS;

	@Value("${gec.headers}")
	private String GEC_HEADERS;

	@Value("${hfs.headers}")
	private String HFS_HEADERS;

	@SuppressWarnings("unchecked")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@RequestMapping(value = "/resultExcel")
	@ResponseBody
	public ResponseEntity<InputStreamResource> exportToExcel(HttpServletRequest httprequest,
			@RequestBody Object request) {
		InputStreamResource resource = null;
		decodeSSO.getDecodedSSO(httprequest.getHeader("loggedinuser"));
		String filename = DIRECTORY + "\\Search_Result.xls";
		try {
			// System.out.println(request.getClass());
			List<HashMap<String, String>> resultList = new ArrayList<HashMap<String, String>>();
			resultList = (List<HashMap<String, String>>) request;
			String[] excelHeaders = null;
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet sheet = workbook.createSheet("Search_Result");
			String docType = resultList.get(1).get("docType");
			if (docType.equalsIgnoreCase("dealDoc")) {
				excelHeaders = DEAL_HEADERS.split(",");
			} else if (docType.equalsIgnoreCase("gec") || docType.equalsIgnoreCase("gecDoc")) {
				excelHeaders = GEC_HEADERS.split(",");
			} else if (docType.equalsIgnoreCase("hfs") || docType.equalsIgnoreCase("hfsDocument")) {
				excelHeaders = HFS_HEADERS.split(",");
			}
			for (int row = 0; row <= resultList.size(); row++) {
				HSSFRow excelRow = sheet.createRow((short) row);
				if (row == 0) {
					int cellCount = 0;
					for (String head : excelHeaders) {
						if (head.equalsIgnoreCase("docId")) {
							excelRow.createCell(cellCount).setCellValue("Download");
						} else if (head.equalsIgnoreCase("documnetSubType")) {
							excelRow.createCell(cellCount).setCellValue("documentSubType");
						} else if (head.equalsIgnoreCase("legalEntityType")) {
							excelRow.createCell(cellCount).setCellValue("entityType");
						} else {
							excelRow.createCell(cellCount).setCellValue(head);
						}
						cellCount++;
					}
				} else if (row > 0) {
					int cellCount = 0;
					// System.out.println(excelHeaders);
					for (String head : excelHeaders) {
						if (head.equalsIgnoreCase("docId")) {
							Cell cell = excelRow.createCell(cellCount);
							cell.setCellValue("Click Here");
							String docId = String.valueOf(resultList.get(row - 1).get(head));
							String address = "https://hfsdms-uat16.cloud.health.ge.com/common-dms-app/#/downloadDocument?docId="
									+ docId;
							CreationHelper createHelper = workbook.getCreationHelper();
							HSSFHyperlink link = (HSSFHyperlink) createHelper.createHyperlink(Hyperlink.LINK_URL);
							link.setAddress(address);
							cell.setHyperlink((HSSFHyperlink) link);
							cell.setHyperlink(link);
						} else if (head.equalsIgnoreCase("syndicationPackage")) {
							if (resultList.get(row - 1).get(head) != null
									&& resultList.get(row - 1).get(head).equals("1"))
								excelRow.createCell(cellCount).setCellValue("True");
							else
								excelRow.createCell(cellCount).setCellValue("False");
						} else {
							if (resultList.get(row - 1).get(head) != null)
								excelRow.createCell(cellCount)
										.setCellValue(String.valueOf(resultList.get(row - 1).get(head)));
							else
								excelRow.createCell(cellCount).setCellValue("");
						}

						cellCount++;
					}
				}
			}
			FileOutputStream fileOut = new FileOutputStream(filename);
			workbook.write(fileOut);
			resource = new InputStreamResource(new FileInputStream(filename));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ResponseEntity.badRequest().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;")
					.body(new InputStreamResource(null, "File Not Found"));
		}
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;")
				.header("fileName", "Search_Result.xls").body(resource);

	}

}