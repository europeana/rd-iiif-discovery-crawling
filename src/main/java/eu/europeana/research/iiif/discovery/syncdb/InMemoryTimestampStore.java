package eu.europeana.research.iiif.discovery.syncdb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;


public class InMemoryTimestampStore implements TimestampTracker {
	Map<String, Map<String, Calendar>> db=new HashMap<>();
	Map<String, Calendar> dbDataset=new HashMap<>();
//	Set<String> dbDeletedDataset=new HashSet<>();
	Map<String, Set<String>> dbDeleted=new HashMap<>();
	
	File persistanceCsvFolder;
	
	public InMemoryTimestampStore(String persistanceCsvFolder) {
		this.persistanceCsvFolder = new File(persistanceCsvFolder);
	}

	public void setDatasetTimestamp(String dataset, Calendar timestamp) {
		dbDataset.put(dataset, timestamp);
	}

	public void setObjectTimestamp(String collection, String object, Calendar timestamp) {
		setObjectTimestamp(collection, object, timestamp, false);
	}

	public Status getObjectStatus(String collection, String object) {
		Map<String, Calendar> ret = db.get(collection);
		if(ret==null)
			return null;
		Calendar tmst = ret.get(object);
		if(tmst==null)
			return null;
		Status s=new Status(tmst, dbDeleted.get(collection).contains(object));
		return s;
	}

	public void setObjectTimestamp(String collection, String object, Calendar timestamp, boolean deleted) {
		Map<String, Calendar> colMap = db.get(collection);
		if(colMap==null) {
			colMap=new HashMap<>();
			db.put(collection, colMap);
			dbDeleted.put(collection, new HashSet<>());
			
		}
		colMap.put(object, timestamp);
		if(deleted)
			dbDeleted.get(collection).add(object);
		else
			dbDeleted.get(collection).remove(object);
	}

	@Override
	public void open() {
//		throw new RuntimeException("TODO");
	}

	@Override
	public void commit() throws IOException {
		FileOutputStream fosDatasets=new FileOutputStream(new File(persistanceCsvFolder, "syncdb-datasets.syncdb.csv"), false);
		OutputStreamWriter datasetsCsvWriter=new OutputStreamWriter(fosDatasets, "UTF-8");
		CSVPrinter csvPrinterDatasets=new CSVPrinter(datasetsCsvWriter, CSVFormat.DEFAULT);
		for(Entry<String, Calendar> dataset: dbDataset.entrySet()) {
			csvPrinterDatasets.print(dataset.getKey());
			csvPrinterDatasets.print(dataset.getValue().getTimeInMillis());
			csvPrinterDatasets.println();
			
			FileOutputStream fosResources=new FileOutputStream(new File(persistanceCsvFolder, URLEncoder.encode(dataset.getKey(), "UTF-8")+".syncdb.csv"), false);
			OutputStreamWriter resourcesCsvWriter=new OutputStreamWriter(fosResources, "UTF-8");
			CSVPrinter csvPrinterResources=new CSVPrinter(resourcesCsvWriter, CSVFormat.DEFAULT);
			Set<String> deletedOfDataset = dbDeleted.get(dataset.getKey());
			for(Entry<String, Calendar> resource: db.get(dataset.getKey()).entrySet()) {
				csvPrinterResources.print(resource.getKey());
				csvPrinterResources.print(resource.getValue()==null? "" : resource.getValue());
//				csvPrinterResources.print(resource.getValue()==null? "" : resource.getValue().getTimeInMillis());
				csvPrinterResources.print(deletedOfDataset.contains(resource.getKey()) ? "D" : "");
				csvPrinterResources.println();
			}
			csvPrinterResources.close();
		}
		csvPrinterDatasets.close();
	}

	@Override
	public Calendar getDatasetStatus(String dataset) {
		return dbDataset.get(dataset);
	}

	@Override
	public Calendar getObjectTimestamp(String dataset, String object) {
		Map<String, Calendar> ret = db.get(dataset);
		if(ret==null)
			return null;
		return ret.get(object);
	}

	@Override
	public void close() throws Exception {
		commit();
	}
}
