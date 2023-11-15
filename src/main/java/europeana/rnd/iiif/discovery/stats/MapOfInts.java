/*
 * Created on Oct 12, 2004
 *
 */
package europeana.rnd.iiif.discovery.stats;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 * A general data structure for holding counts. Useful for calculation of statistics.
 */
public class MapOfInts<K> extends Hashtable<K, Integer> implements Serializable {

	public enum Sort {
		BY_KEY_ASCENDING, BY_KEY_DESCENDING, BY_VALUE_ASCENDING, BY_VALUE_DESCENDING, NONE
	};

	private static final long serialVersionUID = 1;

	/**
	 * Creates a new instance of this class.
	 */
	public MapOfInts() {
		super();
	}

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param initialCapacity
	 */
	public MapOfInts(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * @return sum of all ints
	 */
	public int total() {
		int total = 0;
		for (K key : keySet()) {
			total += get(key);
		}
		return total;
	}

	/**
	 * @param key
	 * @param value
	 */
	public void addTo(K key, Integer value) {
		Integer v = get(key);
		if (v != null) {
			put(key, value + v);
		} else {
			put(key, value);
		}
	}

	/**
	 * @param key
	 */
	public int incrementTo(K key) {
		Integer v = get(key);
		int newVal;
		if (v != null) {
			newVal = 1 + v;
		} else
			newVal = 1;
		put(key, newVal);
		return newVal;
	}

	/**
	 * @param key
	 * @param value
	 */
	public void subtractTo(K key, Integer value) {
		Integer v = get(key);
		if (v != null) {
			put(key, value - v);
		} else {
			put(key, -value);
		}
	}

	@Override
	public synchronized Integer get(Object key) {
		Integer ret = super.get(key);
		return ret == null ? 0 : ret;
	}

	/**
	 * @param key
	 */
	public void decrementTo(K key) {
		Integer v = get(key);
		if (v != null) {
			put(key, v - 1);
		} else {
			put(key, -1);
		}
	}

	Comparator<Entry<K, Integer>> valueAscendingComparator = new Comparator<Entry<K, Integer>>() {
		@Override
		public int compare(java.util.Map.Entry<K, Integer> o1, java.util.Map.Entry<K, Integer> o2) {
			return o2.getValue() - o1.getValue();
		}
	};

	Comparator<Entry<K, Integer>> valueDescendingComparator = new Comparator<Entry<K, Integer>>() {
		@Override
		public int compare(java.util.Map.Entry<K, Integer> o1, java.util.Map.Entry<K, Integer> o2) {
			return o2.getValue() - o1.getValue();
		}
	};

	Comparator<Entry<K, Integer>> keyDescendingComparator = new Comparator<Entry<K, Integer>>() {
		@Override
		public int compare(java.util.Map.Entry<K, Integer> o1, java.util.Map.Entry<K, Integer> o2) {
			return -((Comparable) o1.getKey()).compareTo(o2.getKey());
		}
	};

	Comparator<Entry<K, Integer>> keyAscendingComparator = new Comparator<Entry<K, Integer>>() {
		@Override
		public int compare(java.util.Map.Entry<K, Integer> o1, java.util.Map.Entry<K, Integer> o2) {
			return ((Comparable) o1.getKey()).compareTo(o2.getKey());
		}
	};

	public List<Entry<K, Integer>> getSortedEntries(Sort sort) {
		ArrayList<Entry<K, Integer>> ret = new ArrayList<>(entrySet());
		switch (sort) {
		case BY_KEY_ASCENDING:
			Collections.sort(ret, keyAscendingComparator);
			break;
		case BY_KEY_DESCENDING:
			Collections.sort(ret, keyDescendingComparator);
			break;
		case BY_VALUE_ASCENDING:
			Collections.sort(ret, valueAscendingComparator);
			break;
		case BY_VALUE_DESCENDING:
			Collections.sort(ret, valueAscendingComparator);
			break;
		case NONE:
			break;
		}
		return ret;
	}

	public List<K> getSortedKeysByInts() {
		List<K> ret = new ArrayList<K>(size());
		for (Entry<K, Integer> ns : getSortedEntries(Sort.BY_VALUE_ASCENDING)) {
			ret.add(ns.getKey());
		}
		return ret;
	}

	public List<K> getSortedKeys() {
		List<K> ret = new ArrayList<K>(keySet());
		Collections.sort((List<Comparable>) ret);
		return ret;
	}

	public void incrementToAll(Iterable<K> addToThese) {
		for (K k : addToThese)
			incrementTo(k);
	}

	public void writeCsv(Appendable csvWrite, Sort sort) throws IOException {
		CSVPrinter printer = new CSVPrinter(csvWrite, CSVFormat.DEFAULT);
		for (Entry<K, Integer> r : getSortedEntries(sort)) {
			printer.printRecord(r.getKey().toString(), r.getValue().toString());
		}
		printer.close();
	}

	public void writeCsv(Map<?, ?> labels, Appendable csvWrite) throws IOException {
		CSVPrinter printer = new CSVPrinter(csvWrite, CSVFormat.DEFAULT);
		for (Entry<K, Integer> r : entrySet()) {
			Object label = labels.get(r.getKey());
			printer.printRecord(r.getKey().toString(), r.getValue().toString(), label == null ? "" : label.toString());
		}
		printer.close();
	}

	public void addToAll(MapOfInts<K> otherMap) {
		for (java.util.Map.Entry<K, Integer> e : otherMap.entrySet())
			addTo(e.getKey(), e.getValue());
	}

	public int sizeTotal() {
		int size = 0;
		for (K key : keySet())
			size += get(key);
		return size;
	}

}
