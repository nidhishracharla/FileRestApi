package com.ge.capital.dms.fr.sle.controllers.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFile.Info;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.ge.capital.dms.fr.sle.config.DecodeSSO;
import com.ge.capital.dms.service.SelectService;
import com.ge.capital.dms.utility.DmsUtilityService;
import com.ge.capital.dms.utility.EmailNotificationUtil;
import com.itextpdf.text.Document;

/**
 * @author GJ00557822
 */
@CrossOrigin(origins = "*", exposedHeaders = "fileName")
@RestController
@RequestMapping("/secure")
public class RestructurePackageController {
	private static final Logger log = Logger.getLogger(RestructurePackageController.class);

	@Value("${download.path}")
	private String DIRECTORY;

	@Value("${box.upload.url}")
	private String boxUploadURL;

	@Value("${to.email.address}")
	private String toEmail;

	@Value("${logs.folder}")
	private String logFolderID;

	@Value("${welcome.email}")
	private String welcomeEmail;

	@Value("${final.email}")
	private String finalEmail;

	@Autowired
	SelectService selectService;

	@Autowired
	DmsUtilityService dmsUtilityService;

	@Autowired
	EmailPackageController emailPackageController;

	@Autowired
	EmailNotificationUtil emailNotificationUtil;

	@Autowired
	DecodeSSO decodeSSO;

	@Autowired
	DocumentController documentController;

//	Map<String, String> seqMap = new HashMap<String, String>();

	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@RequestMapping(value = "/restructurePackage")
	@ResponseBody
	public String restructurePackage(HttpServletRequest httprequest, @RequestBody String folderIds) {
		String[] boxFolderIds = folderIds.split(",");

		try {
			for (String boxId : boxFolderIds) {
				String boxToken = dmsUtilityService.requestAccessToken();
				BoxAPIConnection api = new BoxAPIConnection(boxToken);
				BoxFolder parentFolder = new BoxFolder(api, boxId);
				for (BoxItem.Info rootInfo : parentFolder.getChildren()) {
					if (rootInfo instanceof BoxFile.Info) {
						if (rootInfo.getName().equals("Prior to Renewal Lease.xlsx")) {
							log.info("Excel file is found");
							BoxFile exelFile = new BoxFile(api, rootInfo.getID());
							FileOutputStream stream;
							stream = new FileOutputStream(DIRECTORY + "\\Prior to Renewal Lease.xlsx");
							exelFile.download(stream);
							stream.close();
							// read from file
//							seqMap = readFromExcel("Prior to Renewal Lease.xlsx");Prior to Renewal Lease
//							log.info(seqMap); // add contents as map
						}
					}
				}
				for (BoxItem.Info rootInfo : parentFolder.getChildren()) {
					if (rootInfo instanceof BoxFolder.Info
							&& (rootInfo.getName().contains("Audited") || rootInfo.getName().contains("audited"))) {
						List<String> fileNamesWelcome = new ArrayList<String>();
						List<String> fileNamesFinal = new ArrayList<String>();
						List<BoxFile.Info> fileNames = new ArrayList<BoxFile.Info>();
						Boolean canUpload = true;
						BoxFolder rootFolder = new BoxFolder(api, rootInfo.getID());
						String contractNum = "";
						String newContractNum = "";
						if (rootInfo.getName().contains("-Audited")) {
							contractNum = rootInfo.getName().replace("-Audited", "");
						} else if (rootInfo.getName().contains("-audited")) {
							contractNum = rootInfo.getName().replace("-audited", "");
						} else if (rootInfo.getName().contains("Audited")) {
							contractNum = rootInfo.getName().replace("Audited", "");
						} else if (rootInfo.getName().contains("audited")) {
							contractNum = rootInfo.getName().replace("audited", "");
						}

						String logFileName = contractNum + "_DMS_Log_"
								+ new SimpleDateFormat("MM-dd-yyy-HH.mm.ss").format(new java.util.Date()) + ".log";
						FileWriter myWriter = new FileWriter(DIRECTORY + "\\" + logFileName);
//						myWriter.write(System.getProperty("line.separator"));
						myWriter.write(
								new Timestamp(new Date().getTime()) + " : " + "Root Folder ID : " + boxId + "\n");
						myWriter.write(
								" \n " + new Timestamp(new Date().getTime()) + " : " + "Processing started for : "
										+ rootInfo.getID() + " Contract Number :" + contractNum + "\n");
						Boolean isWelcomPack = false;
						Boolean isFinalPack = false;
						String packType1 = "";
						String packType2 = "";
						String creator = "";
						/*
						 * myWriter.write(" \n " + new Timestamp(new Date().getTime()) +
						 * " : The new Sequence Number Map :" + seqMap);
						 */
						newContractNum = readFromExcel("Prior to Renewal Lease.xlsx", contractNum);
						myWriter.write(" \n " + new Timestamp(new Date().getTime())
								+ " : List of Files Available in Folder : \n");
						for (BoxItem.Info itemInfo : rootFolder.getChildren()) {
							if (itemInfo instanceof BoxFile.Info) {
								if ((itemInfo.getName().endsWith(".pdf") || itemInfo.getName().endsWith(".prm"))) {
									fileNames.add((Info) itemInfo);
									log.info("FileName : " + itemInfo.getName());
									myWriter.write(" \n " + new Timestamp(new Date().getTime()) + " : File Name :"
											+ itemInfo.getName());
									if (itemInfo.getName().contains("FCL")) {
										isWelcomPack = true;
										BoxFile boxFile = new BoxFile(api, itemInfo.getID());
										packType1 = "PKG - Welcome Package";
										toEmail = boxFile.getInfo().getModifiedBy().getLogin();
										creator = selectService.getUserId(toEmail);
									}
									if (itemInfo.getName().contains("Coversheet")
											|| itemInfo.getName().contains("coversheet")
											|| itemInfo.getName().contains("cover")
											|| itemInfo.getName().contains("Cover")) {
										isFinalPack = true;
										packType2 = "PKG - TIAA Final Package";
									}
								}
							}
						}
						if (isWelcomPack) {
							myWriter.write(" \n " + new Timestamp(new Date().getTime()) + " : Processing Started :");
							HashMap<String, String> finalPackMetadata = new HashMap<String, String>();
							finalPackMetadata = selectService.getDocMetadata(contractNum); // fetching metadata
							if (finalPackMetadata.isEmpty()) {
								canUpload = false;
								emailNotificationUtil.emailWithoutAttachments(500, "Couldn't load Metadata for the Seq#"
										+ contractNum + " in DMS, Files Cannot be uploaded", toEmail);
							}

							if (newContractNum != null) {
								myWriter.write(" \n " + new Timestamp(new Date().getTime())
										+ " : The new Sequence Number :" + newContractNum);
								finalPackMetadata.put("lwContractSequenceNumber", newContractNum);// NewcontractNumber
							} else {
								myWriter.write(" \n " + new Timestamp(new Date().getTime())
										+ " : Unable to get the new Sequence number");
								finalPackMetadata.put("lwContractSequenceNumber", contractNum);
							}

							finalPackMetadata.put("sourceSystem", "DMS");
							finalPackMetadata.put("physicalStorageStatus", "0");
							finalPackMetadata.put("syndicationPackage", "0");
							finalPackMetadata.put("creator", creator);
							// adding new contract number
							/*
							 * for (BoxFile.Info itemInfo : fileNames) { if
							 * (itemInfo.getName().contains("renew") || itemInfo.getName().contains("new"))
							 * { String[] fileNameArray = itemInfo.getName().split("_"); newContractNum =
							 * fileNameArray[0]; myWriter.write(" \n " + new Timestamp(new Date().getTime())
							 * + " : New Sequence Number :" + newContractNum);
							 * 
							 * finalPackMetadata.put("lwContractSequenceNumber", newContractNum); } }
							 */
							for (BoxFile.Info fileInfo : fileNames) {
								String fileName = fileInfo.getName();
								finalPackMetadata.put("fileName", fileName);
								if (fileName.endsWith(".prm")) {
									finalPackMetadata.put("docSubType", "Pricing and SuperTrump");
									finalPackMetadata.put("finalPackage", "0");
									finalPackMetadata.put("welcomePackage", "0");
								} else if (fileName.endsWith(".pdf")) {
									if (fileName.contains("Signed") || fileName.contains("signed")) {
										fileNamesWelcome.add(fileName);
										fileNamesFinal.add(fileName);
										finalPackMetadata.put("docSubType", "Contract Executed Amendment");
										finalPackMetadata.put("welcomePackage", "1");
										finalPackMetadata.put("finalPackage", "1");
									} else {
										finalPackMetadata.remove("docSubType");
										fileNamesWelcome.add(fileName);
										fileNamesFinal.add(fileName);
									}
								}
								boxToken = dmsUtilityService.requestAccessToken();
								api = new BoxAPIConnection(boxToken);
								BoxFile file = new BoxFile(api, fileInfo.getID());
								FileOutputStream stream;
								stream = new FileOutputStream(DIRECTORY + "\\" + fileName);
								file.download(stream);
								stream.close();
								if (canUpload && (finalPackMetadata.get("docSubType") != null)) {
									try {
										String resp = emailPackageController.uploadDocument(httprequest, fileName,
												finalPackMetadata, finalPackMetadata.get("docSubType"));
										myWriter.write(" \n " + new Timestamp(new Date().getTime()) + " : " + fileName
												+ ", Response :" + resp + "\n");
									} catch (Exception e) {
										log.info(e.getMessage(), e);
									}
								} else {
									myWriter.write(" \n " + new Timestamp(new Date().getTime()) + " : " + fileName
											+ ", Cannot Upload file to DMS, unable to fetch metadata/Check your file name : "
											+ finalPackMetadata + "\n");
								}
							}

							String welComePackName = newContractNum + "_" + packType1 + ".pdf";
							String welComdoIdExists = selectService.isFileExists(welComePackName);
							if (welComdoIdExists != null && !(welComdoIdExists.equals(""))) {
								JSONObject json = new JSONObject();
								json.put("docType", "dealDoc");
								json.put("docId", welComdoIdExists);
								String[] finalArray = new String[1];
								finalArray[0] = json.toString();
								log.info("Delete Request sent to delete ::" + welComdoIdExists);
								documentController.deleteDoc(httprequest, finalArray);
							}
							myWriter.write(" \n " + new Timestamp(new Date().getTime()) + " : WelcomePackageName :"
									+ welComePackName + "\n");

							finalPackMetadata.put("docSubType", packType1);
							finalPackMetadata.put("fileName", welComePackName);
							finalPackMetadata.put("welcomePackage", "0");
							finalPackMetadata.put("finalPackage", "0");
							log.info(welComePackName);
							List<File> pdfWelcomeFiles = getPDFFiles(DIRECTORY, fileNamesWelcome, packType1);
							Document finalWelComePDF = emailPackageController.mergePDF(pdfWelcomeFiles, DIRECTORY,
									welComePackName);

							myWriter.write(" \n " + new Timestamp(new Date().getTime()) + " : " + welComePackName
									+ ": Email Sent to :" + toEmail + "\n");
							emailPackageController.emailDocument(finalWelComePDF, welComePackName,
									toEmail + welcomeEmail);

							String respWelcome = emailPackageController.uploadDocument(httprequest, welComePackName,
									finalPackMetadata, packType1);
							myWriter.write(" \n " + new Timestamp(new Date().getTime()) + " : " + welComePackName
									+ ", Response :" + respWelcome);

							if (isFinalPack) {
								String finalPackName = newContractNum + "_" + packType2 + ".pdf";
								myWriter.write(" \n " + new Timestamp(new Date().getTime()) + " : finalPackageName :"
										+ finalPackName + "\n");
								String doIdExists = selectService.isFileExists(finalPackName);
								if (doIdExists != null && !(doIdExists.equals(""))) {
									JSONObject json = new JSONObject();
									json.put("docType", "dealDoc");
									json.put("docId", doIdExists);
									String[] finalArray = new String[1];
									finalArray[0] = json.toString();
									log.info("Delete Request sent to delete ::" + doIdExists);
									documentController.deleteDoc(httprequest, finalArray);
								}
								finalPackMetadata.put("docSubType", packType2);
								finalPackMetadata.put("fileName", finalPackName);
								finalPackMetadata.put("welcomePackage", "0");
								finalPackMetadata.put("finalPackage", "0");
								log.info(finalPackName);
								List<File> pdfFiles = getPDFFiles(DIRECTORY, fileNamesFinal, packType2);
								Document finalPDF = emailPackageController.mergePDF(pdfFiles, DIRECTORY, finalPackName);

								myWriter.write(" \n " + new Timestamp(new Date().getTime()) + " : " + finalPackName
										+ ": Email Sent to :" + toEmail + "\n");
								emailPackageController.emailDocument(finalPDF, finalPackName, toEmail + finalEmail);
								String resp = emailPackageController.uploadDocument(httprequest, finalPackName,
										finalPackMetadata, packType2);
								myWriter.write(" \n " + new Timestamp(new Date().getTime()) + " : " + finalPackName
										+ ", Response :" + resp);
							}
							rootInfo.setName(contractNum + "-Complete");
							rootFolder.updateInfo((BoxFolder.Info) rootInfo);
							myWriter.write(" \n " + new Timestamp(new Date().getTime()) + " : Processing completed \n");
							myWriter.close();
							BoxFolder rootLogFolder = new BoxFolder(api, logFolderID);

							String todayLogFolder = new SimpleDateFormat("MM-dd-yyyy").format(new java.util.Date());
							String subLogId = "";
							for (BoxItem.Info logInfo : rootLogFolder.getChildren()) {
								if (logInfo instanceof BoxFolder.Info) {
									String actualFolderName = logInfo.getName();
									if (actualFolderName.equals(todayLogFolder)) {
										subLogId = logInfo.getID();
									}
								}
							}
							if (subLogId.equals("")) {
								subLogId = rootLogFolder.createFolder(todayLogFolder).getID();
							}
							BoxFolder subLogFolder = new BoxFolder(api, subLogId);
							FileInputStream stream = new FileInputStream(DIRECTORY + "\\" + logFileName);
							subLogFolder.uploadFile(stream, logFileName);
							stream.close();
							fileNamesWelcome.clear();
							fileNamesFinal.clear();
							fileNames.clear();
						} else {
							rootInfo.setName(contractNum + "-ERROR");
							rootFolder.updateInfo((BoxFolder.Info) rootInfo);
							emailNotificationUtil.emailWithoutAttachments(500,
									"CoverLetter Doesn't exist" + contractNum + " in Folder", toEmail);
							myWriter.write(
									" \n " + new Timestamp(new Date().getTime()) + " : Coverletter doesnot exist \n");
							myWriter.close();
							BoxFolder rootLogFolder = new BoxFolder(api, logFolderID);

							String todayLogFolder = new SimpleDateFormat("MM-dd-yyyy").format(new java.util.Date());
							String subLogId = "";
							for (BoxItem.Info logInfo : rootLogFolder.getChildren()) {
								if (logInfo instanceof BoxFolder.Info) {
									String actualFolderName = logInfo.getName();
									if (actualFolderName.equals(todayLogFolder)) {
										subLogId = logInfo.getID();
									}
								}
							}
							if (subLogId.equals("")) {
								subLogId = rootLogFolder.createFolder(todayLogFolder).getID();
							}
							BoxFolder subLogFolder = new BoxFolder(api, subLogId);
							FileInputStream stream = new FileInputStream(DIRECTORY + "\\" + logFileName);
							subLogFolder.uploadFile(stream, logFileName);
							stream.close();
							fileNamesWelcome.clear();
							fileNamesFinal.clear();
							fileNames.clear();

						}
					}

				}
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return "Processing Completed";
	}

	private String readFromExcel(String name, String contractNum) throws IOException {
		Map<String, String> seqMap = new HashMap<String, String>();
		FileInputStream file = new FileInputStream(new File(DIRECTORY + "\\" + name));
		XSSFWorkbook workbook = new XSSFWorkbook(file);
		XSSFSheet sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			Iterator<Cell> cellIterator = row.cellIterator();
			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();
				Cell nextCell = cellIterator.next();
				cell.setCellType(Cell.CELL_TYPE_STRING);
				nextCell.setCellType(Cell.CELL_TYPE_STRING);
//				if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
				seqMap.put(String.valueOf(cell.getStringCellValue().trim()),
						String.valueOf(nextCell.getStringCellValue()).trim());
				log.info(String.valueOf(cell.getStringCellValue()) + " : "
						+ String.valueOf(nextCell.getStringCellValue()));
				/*
				 * } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
				 * seqMap.put(String.valueOf(Math.round(cell.getNumericCellValue())).trim(),
				 * String.valueOf(Math.round(nextCell.getNumericCellValue())).trim());
				 * log.info(String.valueOf(Math.round(cell.getNumericCellValue())) + " : " +
				 * String.valueOf(Math.round(nextCell.getNumericCellValue()))); }
				 */
			}
		}
		log.info(seqMap);
		file.close();
		return seqMap.get(contractNum).trim();
	}

	private List<File> getPDFFiles(String dIRECTORY2, List<String> fileNames, String packType) {
		List<File> pdfFiles = new ArrayList<File>();
		if (packType.equals("PKG - Welcome Package")) {
			for (int i = 0; i < fileNames.size(); i++) {
				String temp = fileNames.get(i);
				if (temp.endsWith(".pdf") && temp.contains("FCL")) {
					pdfFiles.add(new File(dIRECTORY2 + "\\" + temp));
				}
			}
			for (int i = 0; i < fileNames.size(); i++) {
				String temp = fileNames.get(i);
				if (temp.endsWith(".pdf") && (temp.contains("Signed") || temp.contains("signed"))) {
					pdfFiles.add(new File(dIRECTORY2 + "\\" + temp));
				}
			}
			for (int i = 0; i < fileNames.size(); i++) {
				String temp = fileNames.get(i);
				if (temp.endsWith(".pdf")
						&& (temp.contains("StipLoss") || temp.contains("Stip") || temp.contains("stip"))) {
					pdfFiles.add(new File(dIRECTORY2 + "\\" + temp));
				}
			}
		} else if (packType.equals("PKG - TIAA Final Package")) {
			for (int i = 0; i < fileNames.size(); i++) {
				String temp = fileNames.get(i);
				if (temp.endsWith(".pdf") && (temp.contains("Coversheet") || temp.contains("coversheet")
						|| temp.contains("cover") || temp.contains("Cover"))) {
					pdfFiles.add(new File(dIRECTORY2 + "\\" + temp));
				}
			}
			for (int i = 0; i < fileNames.size(); i++) {
				String temp = fileNames.get(i);
				if (temp.endsWith(".pdf") && (temp.contains("Signed") || temp.contains("signed"))) {
					pdfFiles.add(new File(dIRECTORY2 + "\\" + temp));
				}
			}
			for (int i = 0; i < fileNames.size(); i++) {
				String temp = fileNames.get(i);
				if (temp.endsWith(".pdf")
						&& (temp.contains("StipLoss") || temp.contains("Stip") || temp.contains("stip"))) {
					pdfFiles.add(new File(dIRECTORY2 + "\\" + temp));
				}
			}
			for (int i = 0; i < fileNames.size(); i++) {
				String temp = fileNames.get(i);
				if (temp.endsWith(".pdf") && (temp.contains("ST") || temp.contains("ST Summary")
						|| temp.contains("STSummary") || temp.contains("ST summary"))) {
					pdfFiles.add(new File(dIRECTORY2 + "\\" + temp));
				}
			}
			for (int i = 0; i < fileNames.size(); i++) {
				String temp = fileNames.get(i);
				if (temp.endsWith(".pdf")
						&& (temp.contains("payoff") || temp.contains("Payoff") || temp.contains("PAyoff"))) {
					pdfFiles.add(new File(dIRECTORY2 + "\\" + temp));
				}
			}
			for (int i = 0; i < fileNames.size(); i++) {
				String temp = fileNames.get(i);
				if (temp.endsWith(".pdf")
						&& (temp.contains("Prior Contract") || temp.contains("prior contract")
								|| temp.contains("priorcontract") || temp.contains("PriorContract"))
						&& !(temp.contains("payoff") || temp.contains("Payoff") || temp.contains("PAyoff"))) {
					pdfFiles.add(new File(dIRECTORY2 + "\\" + temp));
				}
			}
			for (int i = 0; i < fileNames.size(); i++) {
				String temp = fileNames.get(i);
				if (temp.endsWith(".pdf") && (temp.contains("renew") || temp.contains("new"))
						&& !(temp.contains("Cover") || temp.contains("cover"))) {
					pdfFiles.add(new File(dIRECTORY2 + "\\" + temp));
				}
			}

		}

		/*
		 * not used for (int i = 0; i < fileNames.size(); i++) { String temp =
		 * fileNames.get(i); File fileEntry = new File(dIRECTORY2 + "\\" + temp); if
		 * (temp.endsWith(".pdf")) { if (packType.equals("PKG - Welcome Package")) { if
		 * (i >= 0 && (temp.contains("FCL"))) { pdfFiles.add(0, fileEntry); } else if (i
		 * >= 1 && (temp.contains("Amendment") || temp.contains("amendment"))) {
		 * pdfFiles.add(1, fileEntry); } } else if
		 * (packType.equals("PKG - TIAA Final Package")) { if (i >= 0 &&
		 * (temp.contains("Coversheet") || temp.contains("coversheet"))) {
		 * pdfFiles.add(0, fileEntry); } else if (i >= 1 && (temp.contains("Amendment")
		 * || temp.contains("amendment"))) { pdfFiles.add(1, fileEntry); } else if (i >=
		 * 2 && (temp.contains("payoff") || temp.contains("Payoff"))) { pdfFiles.add(2,
		 * fileEntry); } else if (i >= 3 && (temp.contains("old") ||
		 * temp.contains("Old"))) { pdfFiles.add(3, fileEntry); } else if (i >= 4 &&
		 * (temp.contains("renew") || temp.contains("new"))) { pdfFiles.add(4,
		 * fileEntry); } } } }
		 */

		return pdfFiles;
	}
}
