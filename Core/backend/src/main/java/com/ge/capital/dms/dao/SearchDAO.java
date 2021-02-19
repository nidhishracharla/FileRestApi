package com.ge.capital.dms.dao;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.ge.capital.dms.utility.DmsUtilityConstants;
import com.ge.capital.dms.utility.DmsUtilityService;

@Component
@Configuration
@EnableTransactionManagement
public class SearchDAO {

	@PersistenceUnit
	private EntityManagerFactory emf;

	@Autowired
	private DmsUtilityService dmsUtilityService;
	private final Logger log = Logger.getLogger(this.getClass());

	@Transactional
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<HashMap<String, String>> searchMetadata(String docType, Map<String, String> inputMetadataMap) {
		boolean flag = false;
		EntityManager em = emf.createEntityManager();
		HashMap<String, String> resultMap = null;
		List<HashMap<String, String>> resultMetadataList = null;

		try {
			Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docTypeMappingResource);
			// Set<String> inputKeys = inputMetadataMap.keySet();
			String documentType = docType;
			String selectKey = documentType + ".select.clause";
			String fromKey = documentType + ".from.clause";
			String whereKey = documentType + ".where.clause";

			String orderByKey = documentType + ".orderBy.clause";
			String searchQuery = props.getProperty(selectKey) + " " + props.getProperty(fromKey) + " "
					+ props.getProperty(whereKey);
			String fromDate = "";
			String toDate = "";
			for (Map.Entry<String, String> entry : inputMetadataMap.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (key.contains("minYear"))
					searchQuery = searchQuery + " AND " + props.getProperty(documentType + "." + key) + ">=:" + key;
				else if (key.contains("maxYear"))
					searchQuery = searchQuery + " AND " + props.getProperty(documentType + "." + key) + "<=:" + key;
				else if (key.contains("minAmount"))
					searchQuery = searchQuery + " AND " + props.getProperty(documentType + "." + key) + ">=:" + key;
				else if (key.contains("maxAmount"))
					searchQuery = searchQuery + " AND " + props.getProperty(documentType + "." + key) + "<=:" + key;
				else if (key.contains("FromDate") || key.contains("DateFrom")) {
					fromDate = value;
					if (!toDate.isEmpty()) {
						if (!toDate.equals(value)) {
							searchQuery = searchQuery + " AND " + props.getProperty(documentType + "." + key) + ">=:"
									+ key;
						} else {
							flag = true;
							searchQuery = searchQuery + " AND " + props.getProperty(documentType + "." + key)
									+ " >= cast(:" + key + " as date) AND "
									+ props.getProperty(documentType + "." + key) + "< cast(:nextDate as date)";
						}
					} else
						searchQuery = searchQuery + " AND " + props.getProperty(documentType + "." + key) + ">=:" + key;
				} else if (key.contains("ToDate") || key.contains("DateTo")) {
					toDate = value;
					if (!fromDate.isEmpty()) {
						if (!fromDate.equals(value)) {
							searchQuery = searchQuery + " AND " + props.getProperty(documentType + "." + key) + "<=:"
									+ key;
						} else {
							flag = true;
							searchQuery = searchQuery + " AND " + props.getProperty(documentType + "." + key)
									+ " >= cast(:" + key + " as date) AND "
									+ props.getProperty(documentType + "." + key) + "< cast(:nextDate as date)";
						}
					} else
						searchQuery = searchQuery + " AND " + props.getProperty(documentType + "." + key) + "<=:" + key;
				} else if ((key.toLowerCase().contains("date") || key.equals("interestYear"))
						&& !(key.toLowerCase().contains("from") || key.toLowerCase().contains("to"))) {
					flag = true;
					searchQuery = searchQuery + " AND " + props.getProperty(documentType + "." + key) + " >= cast(:"
							+ key + " as date) AND " + props.getProperty(documentType + "." + key)
							+ "< cast(:nextDate as date)";
				} else if (value.contains("*") || key.contains("accountSchedulable") || key.contains("invoicenumber")
						|| key.contains("transactionId") || key.contains("customerId") || key.contains("hfsCustomerId")
						|| key.contains("hfsTransactionId")) {
					inputMetadataMap.put(key, value.replace("*", ""));
					searchQuery = searchQuery + " AND " + props.getProperty(documentType + "." + key)
							+ " like concat('%',:" + key + ",'%')";
				} else if (key.contains("checkamount")) {// || key.contains("totalAmount") for invoices
					float checkAmount = Float.parseFloat(value);
					searchQuery = searchQuery + " AND (" + props.getProperty(documentType + "." + key) + " = "
							+ checkAmount + " OR " + props.getProperty(documentType + "." + key) + " like "
							+ checkAmount + ")";
				} else if (key.equalsIgnoreCase("welcomePackage") || key.equalsIgnoreCase("syndicatePackage")
						|| key.equalsIgnoreCase("finalPackage") || key.equalsIgnoreCase("physicalStorageStatus")) {
					Integer pack = Integer.parseInt(value);
					searchQuery = searchQuery + " AND " + props.getProperty(documentType + "." + key) + "=" + pack;
					System.out.println(searchQuery);
				} else {
					searchQuery = searchQuery + " AND " + props.getProperty(documentType + "." + key) + "=:" + key;
				}
			}

			if (props.getProperty(orderByKey) != null) {
				searchQuery = searchQuery + " " + props.getProperty(orderByKey);
			}
			Query invoicesearchQuery = em.createQuery(searchQuery);
			log.info("Query : " + searchQuery);
			// set the named query values for the metadata inputs
			for (final Iterator iter = inputMetadataMap.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry = (Entry) iter.next();
				final String key = (String) entry.getKey();

				if (key.toLowerCase().contains("date") || key.equalsIgnoreCase("interestYear")) {
					// if(key.equalsIgnoreCase("DateLoaded")) {
					final String ISO_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
					final SimpleDateFormat sdf = new SimpleDateFormat(ISO_FORMAT);
					final String input_format = "yyyy/MM/dd";
					final SimpleDateFormat ipformat = new SimpleDateFormat(input_format);
					java.util.Date date = (java.util.Date) ipformat.parse(inputMetadataMap.get(key));
					String convertedDate = sdf.format(date);
					invoicesearchQuery.setParameter(key, Timestamp.valueOf(convertedDate));
					if (flag) {
						Calendar c = Calendar.getInstance();
						c.setTime(ipformat.parse(ipformat.format(date)));
						c.add(Calendar.DATE, 1);
						invoicesearchQuery.setParameter("nextDate", Timestamp.valueOf(sdf.format(c.getTime())));
					}
					// } else {
					// String oldDate = inputMetadataMap.get(key);
					// java.util.Date date1 = new
					// SimpleDateFormat("yyyy/MM/dd").parse(convertedDate);
					// System.out.println("some random format "+ date1);
					// invoicesearchQuery.setParameter(key, date, TemporalType.DATE);
					// }
				} else if (key.equalsIgnoreCase("welcomePackage") || key.equalsIgnoreCase("finalPackage")
						|| key.equalsIgnoreCase("syndicatePackage") || key.equalsIgnoreCase("physicalStorageStatus")) {
					// do nothing
				} else if (!key.contains("checkamount")) {
					invoicesearchQuery.setParameter(key, inputMetadataMap.get(key));
				}

			}
			invoicesearchQuery.setFirstResult(0);
			// invoicesearchQuery.setMaxResults(Integer.parseInt(props.getProperty("max-result-set")));
			invoicesearchQuery.setMaxResults(10000);
			List<Object[]> invSrchResLst = invoicesearchQuery.getResultList();

			resultMetadataList = new ArrayList<HashMap<String, String>>();

			String respMtdAttrString = props.getProperty(documentType + ".metadata.respAttr");
			if (respMtdAttrString != null) {
				String respMtdAttrArry[] = respMtdAttrString.split(",");
				int respMtdAttrSize = respMtdAttrArry.length;

				// setting the search result
				for (Object[] obj : invSrchResLst) {
					resultMap = new HashMap<String, String>();
					for (int index = 0; index < respMtdAttrSize; index++) {
						if (obj[index] instanceof Date)
							resultMap.put(respMtdAttrArry[index],
									new SimpleDateFormat("MM/dd/yyyy").format((java.util.Date) obj[index]));
						else if (obj[index] instanceof Timestamp) {
							resultMap.put(respMtdAttrArry[index],
									new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format((java.util.Date) obj[index]));
						} else if (obj[index] instanceof Float) {
							resultMap.put(respMtdAttrArry[index], String.valueOf(obj[index]));
						} else if (obj[index] instanceof Integer) {
							resultMap.put(respMtdAttrArry[index], String.valueOf(obj[index]));
						} else
							resultMap.put(respMtdAttrArry[index], (String) obj[index]);
					}
					resultMetadataList.add(resultMap);
				}
			}

			return resultMetadataList;
		} catch (NullPointerException e) {
			log.error(e.getMessage(), e);

		} catch (Exception e) {
			log.error(e.getMessage(), e);

		} finally {
			em.close();
		}
		return resultMetadataList;
	}

	@Transactional
	public List<HashMap<String, String>> searchRecentMetadata(String userId) {
		List<HashMap<String, String>> resultMetadataList = new ArrayList<HashMap<String, String>>();
		EntityManager em = emf.createEntityManager();
		HashMap<String, String> resultMap = null;
		try {
			String searchQuery = "SELECT docId,docName,docType,modifyDate from CommonDoc WHERE modifier=:userId ORDER BY modifyDate desc";
			Query invoicesearchQuery = em.createQuery(searchQuery);
			invoicesearchQuery.setParameter("userId", userId);
			invoicesearchQuery.setFirstResult(0);
			// invoicesearchQuery.setMaxResults(Integer.parseInt(props.getProperty("max-result-set")));
			invoicesearchQuery.setMaxResults(5);
			List<Object[]> invSrchResLst = invoicesearchQuery.getResultList();
			String respMtdAttrArry[] = "docId,docName,docType,modifyDate".split(",");
			int respMtdAttrSize = respMtdAttrArry.length;
			for (Object[] obj : invSrchResLst) {
				resultMap = new HashMap<String, String>();
				for (int index = 0; index < respMtdAttrSize; index++) {
					if (obj[index] instanceof Date)
						resultMap.put(respMtdAttrArry[index],
								new SimpleDateFormat("MM/dd/yyyy").format((java.util.Date) obj[index]));
					else if (obj[index] instanceof Timestamp) {
						resultMap.put(respMtdAttrArry[index],
								new SimpleDateFormat("MM/dd/yyyy").format((java.util.Date) obj[index]));
					} else if (obj[index] instanceof Float) {
						resultMap.put(respMtdAttrArry[index], String.valueOf(obj[index]));
					} else if (obj[index] instanceof Integer) {
						resultMap.put(respMtdAttrArry[index], String.valueOf(obj[index]));
					} else
						resultMap.put(respMtdAttrArry[index], (String) obj[index]);
				}
				resultMetadataList.add(resultMap);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return resultMetadataList;
	}

	@SuppressWarnings("deprecation")
	public List<String> getSequenceNumbers(String partyNumber) {
		List<String> resultMetadataList = new ArrayList<String>();
		EntityManager em = emf.createEntityManager();
		try {
			String searchQuery = "SELECT DISTINCT lw_sequence_num from DealDoc where party_number=:partyNumber";
			Query invoicesearchQuery = em.createQuery(searchQuery);
			invoicesearchQuery.setParameter("partyNumber", partyNumber);
			resultMetadataList = invoicesearchQuery.getResultList();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return resultMetadataList;
	}

}
