package eu.europeana.research.iiif.discovery;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import eu.europeana.research.iiif.discovery.model.Activity;
import eu.europeana.research.iiif.discovery.model.JsonObject;
import eu.europeana.research.iiif.discovery.model.OrderedCollection;
import eu.europeana.research.iiif.discovery.model.OrderedCollectionPage;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker.Deleted;

public class ProcesssingAlgorithm {
	TimestampTracker timestampTracker;
	CrawlingHandler crawler;
	
	Calendar lastHarvest;
	Calendar currentHarvestStartTimestamp=null;
	Calendar currentHarvestLattestTimestamp=null;
	HashSet<String> processedItems;
	HashSet<String> processedCollections;
	String dataset;
	
	Integer sampleSize;

	
	public ProcesssingAlgorithm(TimestampTracker timestampTracker, CrawlingHandler crawler) throws Exception {
		this.timestampTracker = timestampTracker;
		this.crawler = crawler;
	}
	
	public void setSampleSize(Integer sampleSize) {
		this.sampleSize = sampleSize;
	}
	
	public Calendar startProcess(String dataset, boolean updateDatasetTimestamp) throws Exception{
		this.dataset = dataset;
		lastHarvest=timestampTracker.getDatasetTimestamp(dataset);
		processedItems=new HashSet<>(1000);
		processedCollections=new HashSet<>();
		currentHarvestStartTimestamp=new GregorianCalendar();
		{
			int datasetSize = timestampTracker.getDatasetSize(dataset, Deleted.INCLUDE);
			int datasetSizeDeleted = datasetSize - timestampTracker.getDatasetSize(dataset, Deleted.EXCLUDE);
			crawler.log("Starting to process dataset: "+dataset+ " ("+datasetSize+" resources, "+ datasetSizeDeleted +" deleted)");
		}
		process(dataset);
		if(currentHarvestLattestTimestamp!=null)
			currentHarvestStartTimestamp=currentHarvestLattestTimestamp;
		if(updateDatasetTimestamp) 
			timestampTracker.setDatasetTimestamp(dataset, currentHarvestStartTimestamp);
		timestampTracker.commit();
		{
			int datasetSize = timestampTracker.getDatasetSize(dataset, Deleted.INCLUDE);
			int datasetSizeDeleted = datasetSize - timestampTracker.getDatasetSize(dataset, Deleted.EXCLUDE);
			crawler.log("Finished processing dataset: "+dataset+ " ("+datasetSize+" resources, "+ datasetSizeDeleted +" deleted)");
		}
		return currentHarvestLattestTimestamp;
	}
	@SuppressWarnings("incomplete-switch")
	public void process(String collectionOrPageUrl) throws IOException{
		if(processedCollections.contains(collectionOrPageUrl)) {
			return;
		}
		processedCollections.add(collectionOrPageUrl);
		crawler.log("Processing resource "+collectionOrPageUrl);
		InputStream inStream;
		Reader reader;
		if(collectionOrPageUrl.startsWith("file:")) {
			inStream = new FileInputStream(collectionOrPageUrl.substring("file:".length()));
			reader = new InputStreamReader(inStream, "UTF-8");
		} else {
			String urlContent = crawler.httpGet(collectionOrPageUrl);
			reader=new StringReader(urlContent);
		}
		JsonReader jr=new JsonReader(reader);
		jr.beginObject();
		
//		HashMap<String, Object> jsonObj=new HashMap<>();
		JsonObject jsonObj=new JsonObject();
		while(jr.peek()!=JsonToken.END_OBJECT){
			String field = jr.nextName();
			if(field.equals("@context")) {
				jr.skipValue();
			}else if(field.equals("type")) {
				jsonObj.setType(IiifObjectType.valueOf(jr.nextString()));
				jsonObj=jsonObj.toTypedInstance();
				if(jsonObj.getType()==IiifObjectType.OrderedCollection) {
					((OrderedCollection)jsonObj).setProcessingAlgorithm(this);
				}else if(jsonObj.getType()==IiifObjectType.OrderedCollectionPage) {
					((OrderedCollectionPage)jsonObj).setProcessingAlgorithm(this);
				}
			}else if(field.equals("id")) {
				jsonObj.setId(jr.nextString());				
			} else {
				JsonToken valueToken=jr.peek();
				switch (valueToken) {
				case BEGIN_OBJECT:
					jsonObj.processObject(field, jr);
					break;
				case BEGIN_ARRAY:
					jsonObj.processArray(field, jr);
					break;
				case BOOLEAN:
					jsonObj.processField(field, jr.nextBoolean());
					break;
				case NULL:
					jsonObj.processField(field, null);
					break;
				case NUMBER:
					try {
						jsonObj.processField(field, jr.nextInt());
					} catch (NumberFormatException e) {
						try {
							jsonObj.processField(field, jr.nextLong());
						} catch (NumberFormatException e2) {
//							try {
								jsonObj.processField(field, jr.nextDouble());
//							} catch (NumberFormatException e3) { }
						}
					}
					break;
				case STRING:
					jsonObj.processField(field, jr.nextString());
					break;
				}
			}
		}
		jr.endObject();
		jr.close();
		reader.close();
		{
			int datasetSize = timestampTracker.getDatasetSize(dataset, Deleted.INCLUDE);
			int datasetSizeDeleted = datasetSize - timestampTracker.getDatasetSize(dataset, Deleted.EXCLUDE);
			crawler.log("Progress: ("+datasetSize+" resources, "+ datasetSizeDeleted +" deleted)");
		}
		jsonObj.finalize();
	}
	

//	public void processCollection(String iiifSourceUrl) throws IOException{
//
//	}
	
//	public void processCollectionPage(String iiifSourceUrl) throws IOException{
////		Retrieve the representation of page via HTTP(S)
////		Minimally validate that it conforms to the specification
////		Find the set of updates of the page at page.orderedItems (items)
////		In reverse order, iterate through the activities (activity) in items:
////		For each activity, if activity.endTime is before lastCrawl, then terminate ;
////		If the updated resource's uri at activity.target.id is in processedItems, then continue ;
////		Otherwise, if activity.type is Update or Create, then find the URI of the updated resource at activity.target.id (target) and process the target resource ;
////		Otherwise, if activity.type is Delete, then find the URI of the deleted resource at activity.target.id and process its removal.
////		Add the processed resource's URI to processedItems
////		Finally, find the URI of the previous page at collection.prev.id (pageN1)
////		If there is a previous page, apply the results of the page algorithm to pageN1
//		
//		
//		InputStream inStream;
//		Reader reader;
//		String urlContent = crawler.httpGet(iiifSourceUrl);
//		reader=new StringReader(urlContent);
//		JsonReader jr=new JsonReader(reader);
//		jr.beginObject();
//		
////		HashMap<String, Object> jsonObj=new HashMap<>();
//		OrderedCollectionPage jsonObj=new OrderedCollectionPage();
//		while(jr.peek()!=JsonToken.END_OBJECT){
//			String field = jr.nextName();
//			System.out.println(field);
//			if(field.equals("@context")) {
//				jr.skipValue();
//			}else if(field.equals("type")) {
//				jsonObj.setType(IiifObjectType.valueOf(jr.nextString()));
//				jsonObj=jsonObj.toTypedInstance();
//				if(jsonObj.getType()==IiifObjectType.OrderedCollection) {
//					((OrderedCollection)jsonObj).setProcessingAlgorithm(this);
//				}else if(jsonObj.getType()==IiifObjectType.OrderedCollectionPage) {
//					((OrderedCollectionPage)jsonObj).setProcessingAlgorithm(this);
//				}
//			}else if(field.equals("id")) {
//				jsonObj.setId(jr.nextString());				
//			} else {
//				JsonToken valueToken=jr.peek();
//				switch (valueToken) {
//				case BEGIN_OBJECT:
//					jsonObj.processObject(field, jr);
//					break;
//				case BEGIN_ARRAY:
//					jsonObj.processArray(field, jr);
//					break;
//				case BOOLEAN:
//					jsonObj.processField(field, jr.nextBoolean());
//					break;
//				case NULL:
//					jsonObj.processField(field, null);
//					break;
//				case NUMBER:
//					try {
//						jsonObj.processField(field, jr.nextInt());
//					} catch (NumberFormatException e) {
//						try {
//							jsonObj.processField(field, jr.nextLong());
//						} catch (NumberFormatException e2) {
////							try {
//								jsonObj.processField(field, jr.nextDouble());
////							} catch (NumberFormatException e3) { }
//						}
//					}
//					break;
//				case STRING:
//					jsonObj.processField(field, jr.nextString());
//					break;
//				}
//			}
//		}
//		jr.close();
//		reader.close();
//		jsonObj.finalize();
//	}

	/**
	 * @param activity
	 * @return true to continue processing Activities in the Page, false if timestamp is before lastIngest
	 */
	public boolean processActivity(Activity activity) {
		if (lastHarvest!=null && activity.getEndTime().before(lastHarvest))
			return false;
		if(processedItems.contains(activity.getObjectId()))
			return true;

		if(sampleSize!=null && processedItems.size()>=sampleSize)
			return false;
		
		crawler.processManifest(activity);
		timestampTracker.setObjectTimestamp(this.dataset, activity.getObjectId(), activity.getEndTime());
		if(currentHarvestLattestTimestamp==null)
			currentHarvestLattestTimestamp=activity.getEndTime();
		processedItems.add(activity.getObjectId());
		return true;
	}
}
