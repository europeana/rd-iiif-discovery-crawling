package europeana.rnd.iiif.discovery.demo.syncdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
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

/**
 * A simple implementation of a TimestampTracker for demonstration purposes.
 * This implementation works with the timestamps in memory and on commit, it
 * persists them in csv files .
 *
 */
public class InMemoryTimestampStore implements TimestampTracker {

	Map<String, Instant> dbDataset = new HashMap<>();
	Map<String, Instant> dbDatasetError = new HashMap<>();
	Map<String, Map<String, Instant>> db = new HashMap<>();
	Map<String, Set<String>> dbDeleted = new HashMap<>();

	File persistanceCsvFolder;

	public InMemoryTimestampStore(String persistanceCsvFolder) throws IOException {
		this.persistanceCsvFolder = new File(persistanceCsvFolder);
		open();
	}

	@Override
	public void clear(String dataset) {
		db.remove(dataset);
		dbDeleted.remove(dataset);
		dbDataset.remove(dataset);
		dbDatasetError.remove(dataset);
		File resourcesFile;
		resourcesFile = getResourcesCsvFile(dataset);
		if (resourcesFile.exists())
			resourcesFile.delete();
	}

	public void setDatasetTimestamp(String dataset, Instant timestamp) {
		dbDataset.put(dataset, timestamp);
	}

	public void setObjectTimestamp(String collection, String object, Instant timestamp) {
		setObjectTimestamp(collection, object, timestamp, false);
	}

	public ResourceStatus getObjectStatus(String collection, String object) {
		Map<String, Instant> ret = db.get(collection);
		if (ret == null)
			return null;
		Instant tmst = ret.get(object);
		if (tmst == null)
			return null;
		ResourceStatus s = new ResourceStatus(object, tmst, dbDeleted.get(collection).contains(object));
		return s;
	}

	public void setObjectTimestamp(String collection, String object, Instant timestamp, boolean deleted) {
		Map<String, Instant> colMap = db.get(collection);
		if (colMap == null) {
			colMap = new HashMap<>();
			db.put(collection, colMap);
			dbDeleted.put(collection, new HashSet<>());
		}
		colMap.put(object, timestamp);
		if (deleted)
			dbDeleted.get(collection).add(object);
		else
			dbDeleted.get(collection).remove(object);
	}

	@Override
	public synchronized void open() throws IOException {
		File datasetsFile = getDatasetsCsvFile();
		if (datasetsFile.exists()) {
			FileInputStream fosDatasets = new FileInputStream(datasetsFile);
			InputStreamReader datasetsCsvReader = new InputStreamReader(fosDatasets, "UTF-8");
			CSVParser csvParserDatasets = new CSVParser(datasetsCsvReader, CSVFormat.DEFAULT);
			for (Iterator<CSVRecord> it = csvParserDatasets.iterator(); it.hasNext();) {
				CSVRecord rec = it.next();
				Instant timestamp = Instant.ofEpochMilli(Long.parseLong(rec.get(1)));
				dbDataset.put(rec.get(0), timestamp);
				if (rec.size() >= 3) {
					timestamp = Instant.ofEpochMilli(Long.parseLong(rec.get(2)));
					dbDatasetError.put(rec.get(0), timestamp);
				}
				dbDeleted.put(rec.get(0), new HashSet<>());
				db.put(rec.get(0), new HashMap<>());
			}
			csvParserDatasets.close();

			for (Entry<String, Instant> dataset : dbDataset.entrySet()) {
				File file = getResourcesCsvFile(dataset.getKey());
				if (!file.exists())
					return;
				FileInputStream fosResources = new FileInputStream(file);
				InputStreamReader resourcesCsvWriter = new InputStreamReader(fosResources, "UTF-8");
				CSVParser csvParserResources = new CSVParser(resourcesCsvWriter, CSVFormat.DEFAULT);
				Set<String> deletedOfDataset = dbDeleted.get(dataset.getKey());
				Map<String, Instant> dbDatasetMap = db.get(dataset.getKey());
				for (Iterator<CSVRecord> it = csvParserResources.iterator(); it.hasNext();) {
					CSVRecord rec = it.next();
					boolean deleted = rec.get(2) == null || !rec.get(2).equals("D") ? false : true;
					if (deleted)
						deletedOfDataset.add(rec.get(0));
					Instant timestamp = Instant.ofEpochMilli(Long.parseLong(rec.get(1)));
					dbDatasetMap.put(rec.get(0), timestamp);
				}
				System.out.println("Timestamps database loaded dataset: " + dataset.getKey() + " ("
						+ dbDatasetMap.size() + " resources)");
				csvParserResources.close();
			}
		}
		System.out.println("Timestamps database loaded from " + datasetsFile.getPath() + "(" + dbDataset.size()
				+ " existing datasets)");
	}

	@Override
	public synchronized void commit() throws IOException {
		if (!persistanceCsvFolder.exists())
			persistanceCsvFolder.mkdirs();
		FileOutputStream fosDatasets = new FileOutputStream(getDatasetsCsvFile(), false);
		OutputStreamWriter datasetsCsvWriter = new OutputStreamWriter(fosDatasets, "UTF-8");
		CSVPrinter csvPrinterDatasets = new CSVPrinter(datasetsCsvWriter, CSVFormat.DEFAULT);
		for (Entry<String, Instant> dataset : dbDataset.entrySet()) {
			csvPrinterDatasets.print(dataset.getKey());
			csvPrinterDatasets.print(dataset.getValue().toEpochMilli());
			Instant lastError = dbDatasetError.get(dataset.getKey());
			if (lastError != null)
				csvPrinterDatasets.print(lastError.toEpochMilli());
			csvPrinterDatasets.println();

			if (db.containsKey(dataset.getKey())) {
				FileOutputStream fosResources = new FileOutputStream(getResourcesCsvFile(dataset.getKey()), false);
				OutputStreamWriter resourcesCsvWriter = new OutputStreamWriter(fosResources, "UTF-8");
				CSVPrinter csvPrinterResources = new CSVPrinter(resourcesCsvWriter, CSVFormat.DEFAULT);
				Set<String> deletedOfDataset = dbDeleted.get(dataset.getKey());
				for (Entry<String, Instant> resource : db.get(dataset.getKey()).entrySet()) {
					csvPrinterResources.print(resource.getKey());
					csvPrinterResources.print(resource.getValue() == null ? "" : resource.getValue().toEpochMilli());
					csvPrinterResources.print(deletedOfDataset.contains(resource.getKey()) ? "D" : "");
					csvPrinterResources.println();
				}
				csvPrinterResources.close();
			}
		}
		csvPrinterDatasets.close();
	}

	@Override
	public Instant getDatasetTimestamp(String dataset) {
		return dbDataset.get(dataset);
	}

	@Override
	public Instant getObjectTimestamp(String dataset, String object) {
		Map<String, Instant> ret = db.get(dataset);
		if (ret == null)
			return null;
		return ret.get(object);
	}

	@Override
	public void rollback() throws IOException {
		open();
	}

	@Override
	public void close() throws Exception {
		commit();
	}

	@Override
	public Iterable<ResourceStatus> getIterableOfObjects(String dataset, Instant since, Instant until) {
		Map<String, Instant> allRss = db.get(dataset);
		if (allRss == null)
			return Collections.emptyList();
		Set<String> deletedRss = dbDeleted.get(dataset);
		ArrayList<ResourceStatus> ret = new ArrayList<ResourceStatus>();
		for (String objId : allRss.keySet()) {
			Instant timeStp = allRss.get(objId);
			if ((since == null || !since.isAfter(timeStp)) && (until == null || !until.isBefore(timeStp))) {
				ret.add(new ResourceStatus(objId, timeStp, deletedRss.contains(objId)));
			}
		}
		return ret;
	}

	@Override
	public int getDatasetSize(String dataset, Deleted deletedOption) {
		Map<String, Instant> datasetDbMap = db.get(dataset);
		if (datasetDbMap == null)
			return 0;
		if (deletedOption == Deleted.EXCLUDE)
			return datasetDbMap.size() - dbDeleted.get(dataset).size();
		return datasetDbMap.size();
	}

	@Override
	public void setDatasetLastError(String dataset, Instant timestamp) {
		dbDatasetError.put(dataset, timestamp);
	}

	@Override
	public Instant getDatasetLastError(String dataset) {
		return dbDatasetError.get(dataset);
	}

	private File getResourcesCsvFile(String dataset) {
		try {
			return new File(persistanceCsvFolder, URLEncoder.encode(dataset, "UTF-8") + ".syncdb.csv");
		} catch (UnsupportedEncodingException e) {
			// Ignore, UTF 8 is always supported
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private File getDatasetsCsvFile() {
		return new File(persistanceCsvFolder, "syncdb-datasets.syncdb.csv");
	}

}
