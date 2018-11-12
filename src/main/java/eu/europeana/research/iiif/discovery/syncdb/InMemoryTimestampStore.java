package eu.europeana.research.iiif.discovery.syncdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;


public class InMemoryTimestampStore implements TimestampTracker {
	Map<String, Map<String, Calendar>> db=new HashMap<>();
	Map<String, Calendar> dbDataset=new HashMap<>();
	Map<String, Calendar> dbDatasetError=new HashMap<>();
//	Set<String> dbDeletedDataset=new HashSet<>();
	Map<String, Set<String>> dbDeleted=new HashMap<>();
	
	File persistanceCsvFolder;
	
	public InMemoryTimestampStore(String persistanceCsvFolder) {
		this.persistanceCsvFolder = new File(persistanceCsvFolder);
	}

	@Override
	public void clear(String dataset) {
		db.remove(dataset);
		dbDeleted.remove(dataset);
		dbDataset.remove(dataset);
		dbDatasetError.remove(dataset);
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
	public synchronized void open() throws IOException {
		File datasetsFile = new File(persistanceCsvFolder, "syncdb-datasets.syncdb.csv");
		if(datasetsFile.exists()) {
			FileInputStream fosDatasets=new FileInputStream(datasetsFile);
			InputStreamReader datasetsCsvReader=new InputStreamReader(fosDatasets, "UTF-8");
			CSVParser csvParserDatasets=new CSVParser(datasetsCsvReader, CSVFormat.DEFAULT);
			for(Iterator<CSVRecord> it=csvParserDatasets.iterator() ; it.hasNext() ; ) {
				CSVRecord rec=it.next();
				GregorianCalendar timestamp = new GregorianCalendar();
				timestamp.setTimeInMillis(Long.parseLong(rec.get(1)));
				dbDataset.put(rec.get(0), timestamp);
				if(rec.size()>=3) {
					timestamp = new GregorianCalendar();
					timestamp.setTimeInMillis(Long.parseLong(rec.get(2)));
					dbDatasetError.put(rec.get(0), timestamp);
				}
				dbDeleted.put(rec.get(0), new HashSet<>());
				db.put(rec.get(0), new HashMap<>());
			}
			csvParserDatasets.close();
			
			for(Entry<String, Calendar> dataset: dbDataset.entrySet()) {	
				File file = new File(persistanceCsvFolder, URLEncoder.encode(dataset.getKey(), "UTF-8")+".syncdb.csv");
				if(!file.exists())
					return;
				FileInputStream fosResources=new FileInputStream(file);
				InputStreamReader resourcesCsvWriter=new InputStreamReader(fosResources, "UTF-8");
				CSVParser csvParserResources=new CSVParser(resourcesCsvWriter, CSVFormat.DEFAULT);
				Set<String> deletedOfDataset = dbDeleted.get(dataset.getKey());
				Map<String, Calendar> dbDatasetMap = db.get(dataset.getKey());
				for(Iterator<CSVRecord> it=csvParserResources.iterator() ; it.hasNext() ; ) {
					CSVRecord rec=it.next();
					boolean deleted=rec.get(2) == null || !rec.get(2).equals("D") ? false : true;
					if(deleted)
						deletedOfDataset.add(rec.get(0));
					GregorianCalendar timestamp = new GregorianCalendar();
					timestamp.setTimeInMillis(Long.parseLong(rec.get(1)));
					dbDatasetMap.put(rec.get(0), timestamp);
				}
				System.out.println("syncdb loaded dataset: "+dataset.getKey()+" ("+dbDatasetMap.size()+" resources)");
				csvParserResources.close();
			}
		}
		System.out.println("syncdb loaded ("+dbDataset.size()+" datasets)");
	}

	@Override
	public synchronized  void commit() throws IOException {
		if(!persistanceCsvFolder.exists())
			persistanceCsvFolder.mkdirs();
		FileOutputStream fosDatasets=new FileOutputStream(new File(persistanceCsvFolder, "syncdb-datasets.syncdb.csv"), false);
		OutputStreamWriter datasetsCsvWriter=new OutputStreamWriter(fosDatasets, "UTF-8");
		CSVPrinter csvPrinterDatasets=new CSVPrinter(datasetsCsvWriter, CSVFormat.DEFAULT);
		for(Entry<String, Calendar> dataset: dbDataset.entrySet()) {
			csvPrinterDatasets.print(dataset.getKey());
			csvPrinterDatasets.print(dataset.getValue().getTimeInMillis());
			Calendar lastError = dbDatasetError.get(dataset.getKey());
			if(lastError!=null)
				csvPrinterDatasets.print(lastError.getTimeInMillis());			
			csvPrinterDatasets.println();
			
			if(db.containsKey(dataset.getKey())) {
				FileOutputStream fosResources=new FileOutputStream(new File(persistanceCsvFolder, URLEncoder.encode(dataset.getKey(), "UTF-8")+".syncdb.csv"), false);
				OutputStreamWriter resourcesCsvWriter=new OutputStreamWriter(fosResources, "UTF-8");
				CSVPrinter csvPrinterResources=new CSVPrinter(resourcesCsvWriter, CSVFormat.DEFAULT);
				Set<String> deletedOfDataset = dbDeleted.get(dataset.getKey());
				for(Entry<String, Calendar> resource: db.get(dataset.getKey()).entrySet()) {
					csvPrinterResources.print(resource.getKey());
					csvPrinterResources.print(resource.getValue()==null? "" : resource.getValue().getTimeInMillis());
					csvPrinterResources.print(deletedOfDataset.contains(resource.getKey()) ? "D" : "");
					csvPrinterResources.println();
				}
				csvPrinterResources.close();
			}
		}
		csvPrinterDatasets.close();
	}

	@Override
	public Calendar getDatasetTimestamp(String dataset) {
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

	@Override
	public Iterable<String> getIterableOfObjects(String dataset, Deleted deletedOption) {
		Map<String, Calendar> ret = db.get(dataset);
		if(ret==null)
			return Collections.emptyList();
		Set<String> uris = ret.keySet();
		if(deletedOption==Deleted.EXCLUDE)
			uris.removeAll(dbDeleted.get(dataset));
		return uris;
	}

	@Override
	public int getDatasetSize(String dataset, Deleted deletedOption) {
		Map<String, Calendar> datasetDbMap = db.get(dataset);
		if(datasetDbMap==null)
			return 0;
		if(deletedOption==Deleted.EXCLUDE)
			return datasetDbMap.size()-dbDeleted.get(dataset).size();
		return datasetDbMap.size();
	}
	
	@Override
	public void setDatasetLastError(String dataset, Calendar timestamp) {
		dbDatasetError.put(dataset, timestamp);
	}
	
	@Override
	public Calendar getDatasetLastError(String dataset) {
		return dbDatasetError.get(dataset);
	}
	
}
