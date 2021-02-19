/*
package com.ge.capital.dms.fr.sle.controllers.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

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
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.ge.capital.dms.fr.sle.config.DecodeSSO;
import com.ge.capital.dms.service.SelectService;
import com.ge.capital.dms.utility.DmsUtilityService;
import com.ge.capital.dms.utility.EmailNotificationUtil;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

*//**
 * @author GJ00557822
 *//*
@CrossOrigin(origins = "*", exposedHeaders = "fileName")
@RestController
@RequestMapping("/secure")
public class RestructurePackageController2 {
	private static final Logger log = Logger.getLogger(RestructurePackageController2.class);

	@Value("${download.path}")
	private String DIRECTORY;

	@Value("${box.upload.url}")
	private String boxUploadURL;

	@Value("${to.email.address}")
	private String toEmail;

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

	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@RequestMapping(value = "/restructurePackage")
	@ResponseBody
	public String restructurePackage(HttpServletRequest httprequest, @RequestBody String folderIds) {
		String[] boxFolderIds = folderIds.split(",");
		log.info(folderIds);
//		toEmail = toEmail + "," + decodeSSO.getDecodedSSO(httprequest.getHeader("loggedinuser")) + "@ge.com";

		try {

			for (String boxId : boxFolderIds) {
				String boxToken = dmsUtilityService.requestAccessToken();
				List<String> fileNames = new ArrayList<String>();
				BoxAPIConnection api = new BoxAPIConnection(boxToken);
				java.net.Proxy proxy = new java.net.Proxy(Type.HTTP,
						new InetSocketAddress("PITC-Zscaler-Americas-Cincinnati3PR.proxy.corporate.ge.com", 80));
				api.setProxy(proxy);
				BoxFolder parentFolder = new BoxFolder(api, boxId);

				for (BoxItem.Info rootInfo : parentFolder.getChildren()) {
					if (rootInfo instanceof BoxFolder.Info && rootInfo.getName().contains("-Processed")) {
						BoxFolder rootFolder = new BoxFolder(api, rootInfo.getID());
						String contractNum = rootInfo.getName().replace("-Processed", "");
						String packType = "";
						for (BoxItem.Info itemInfo : rootFolder.getChildren()) {
							if (itemInfo instanceof BoxFile.Info) {
								BoxFile boxFile = new BoxFile(api, itemInfo.getID());
								System.out.println(boxFile.getInfo().getOwnedBy().getLogin());
								// && itemInfo.getName().endsWith(".pdf")
								String docId = itemInfo.getID();
								String docName = itemInfo.getName();
								fileNames.add(docName);
								if (docName.contains("FCL") || docName.contains("Coversheet")
										|| docName.contains("coversheet")) {
									if (docName.contains("FCL"))
										packType = "PKG - Welcome Package";
									else
										packType = "PKG - TIAA Final Package";
								}
								boxToken = dmsUtilityService.requestAccessToken();
								api = new BoxAPIConnection(boxToken);
								api.setProxy(proxy);
								log.info(itemInfo.getID() + " : " + itemInfo.getName());
								BoxFile file = new BoxFile(api, docId);
								FileOutputStream stream;
								stream = new FileOutputStream(DIRECTORY + "\\" + docName);
								file.download(stream);
								stream.close();
							}

						}
						for (String temp : fileNames) {
							if (temp.contains("Covid-19") || temp.contains("COVID-19") || temp.contains("COVID_19")
									|| temp.contains("Covid") || temp.contains("COVID") || temp.contains("CV19")
									|| temp.contains("CV-19") || temp.contains("CV_19") || temp.contains("cv19")
									|| temp.contains("cv-19") || temp.contains("cv_19")) {
								if (packType.equals("PKG - Welcome Package")) {
									packType = "PKG - CV19 Welcome Package";
								}
								if (packType.equals("PKG - TIAA Final Package")) {
									packType = "PKG - CV19 TIAA Final Package";
								}
							}
						}
						
						String finalPackName = contractNum + "_" + packType + ".pdf";
						log.info(finalPackName);
						List<File> pdfFiles = getPDFFiles(DIRECTORY, fileNames, packType);
						Document finalPDF = emailPackageController.mergePDF(pdfFiles, DIRECTORY, finalPackName);
						HashMap<String, String> finalPackMetadata = new HashMap<String, String>();
						finalPackMetadata = selectService.getDocMetadata(contractNum);
						finalPackMetadata.put("fileName", finalPackName);
						finalPackMetadata.put("sourceSystem", "DMS");
						finalPackMetadata.put("docSubType", packType);
						finalPackMetadata.put("finalPackage", "0");
						finalPackMetadata.put("welcomePackage", "0");
						finalPackMetadata.put("physicalStorageStatus", "0");
						finalPackMetadata.put("syndicationPackage", "0");
						finalPackMetadata.put("lwSeqNumber", contractNum);
						
						//get the metadata from the DB, filename from file, and document subtype--??
						toEmail = "gayatri.jaladi@ge.com" + "," + finalPackMetadata.get("creator") + "@ge.com";
						emailPackageController.emailDocument(finalPDF, finalPackName, toEmail);
						emailPackageController.uploadDocument(httprequest, finalPackName, finalPDF, finalPackMetadata,
								packType);
						
						rootInfo.setName(contractNum + "-Complete");
						rootFolder.updateInfo((BoxFolder.Info) rootInfo);
					}

				}

			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return "Email has been sent Successfully";
	}

	private List<File> getPDFFiles(String dIRECTORY2, List<String> fileNames, String packType) {
		List<File> pdfFiles = new ArrayList<File>();
		for (int i = 0; i < fileNames.size(); i++) {
			String temp = fileNames.get(i);
			File fileEntry = new File(dIRECTORY2 + "\\" + temp);
			if (temp.endsWith(".pdf")) {
//				pdfFiles.add(new File(dIRECTORY2 + "\\" + temp));
				if (packType.equals("PKG - Welcome Package") || packType.equals("PKG - CV19 Welcome Package")) {
					if (i >= 0 && (temp.contains("FCL"))) {
						pdfFiles.add(0, fileEntry);
					} else if (i >= 1 && (temp.contains("StipLoss") || temp.contains("-stiploss")
							|| temp.contains("Amort") || temp.contains("amort"))) {
						pdfFiles.add(1, fileEntry);
					} else if (i >= 2 && (temp.contains("-Amendment") || temp.contains("-amendment"))) {
						pdfFiles.add(2, fileEntry);
					} else {
						pdfFiles.add(fileEntry);
					}
				} else if (packType.equals("PKG - TIAA Final Package")
						|| packType.equals("PKG - CV19 TIAA Final Package")) {
					if (i >= 0 && (temp.contains("Coversheet") || temp.contains("coversheet"))) {
						pdfFiles.add(0, fileEntry);
					} else if (i >= 1 && (temp.contains("Booking") || temp.contains("booking"))) {
						pdfFiles.add(1, fileEntry);
					} else if (i >= 2 && (temp.contains("Amendment") || temp.contains("amendment"))) {
						pdfFiles.add(2, fileEntry);
					} else if (i >= 3 && (temp.contains("ST Summary") || temp.contains("ST summary"))) {
						pdfFiles.add(3, fileEntry);
					} else if (i >= 4 && (temp.contains("Risk Approval") || temp.contains("risk approval"))) {
						pdfFiles.add(4, fileEntry);
					} else {
						pdfFiles.add(fileEntry);
					}
				}
			}
		}
		return pdfFiles;
	}
}
*/