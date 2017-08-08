package com.github.lahahana.csv.serialize;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.github.lahahana.csv.base.CsvMetaNode;
import com.github.lahahana.csv.exceptions.CsvException;

public class StringCsvSerializer {
	
	private InnerStringCsvSerializer delegate;
	
	private StringBuilder proxyOut = new StringBuilder();
	
	public StringCsvSerializer() throws IOException {
		this.delegate = new InnerStringCsvSerializer(proxyOut);
	}

	public StringCsvSerializer(CSVFormat csvFormat) throws IOException {
		this.delegate = new InnerStringCsvSerializer(proxyOut, csvFormat);
	}

	public <T> String serialize(final T object) throws CsvException, IOException {
		delegate.serialize0(object);
		return delegate.getResult();
	}

	public <T> String serialize(final T[] array) throws CsvException, IOException {
		delegate.serialize0(array);
		return delegate.getResult();
	}

	public <T> String serialize(final Collection<T> iterable, final Class<T> clazz) throws CsvException, IOException {
		delegate.serialize0(iterable, clazz);
		return delegate.getResult();
	}
	
	private class InnerStringCsvSerializer extends CsvSerializer {
		
		public InnerStringCsvSerializer(StringBuilder out) throws IOException {
			super(out);
		}
		
		public InnerStringCsvSerializer(StringBuilder out, CSVFormat csvFormat) throws IOException {
			super(out, csvFormat);
		}

		private static final int THRESHOLD = 10000;

		@Override
		protected <T> void printObjects(CsvMetaNode[] csvMetaNodes, Iterable<T> iterable)
				throws CsvException, IOException {
			int count = 0;
			int totalLength = ((Collection<T>)iterable).size();
			for (Object obj : iterable) {
				if(count++ == THRESHOLD) {
					int predictLength = ((StringBuilder)out).length() / count * totalLength;
					StringBuilder out2 = new StringBuilder(predictLength);
					out2.append(out.toString());
					out = out2;
					try {
						Field outField = InnerStringCsvSerializer.class.getField("out");
						outField.set(this, out2);
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				} 
				printObject(csvMetaNodes, obj);
			}
		}

		@Override
		protected void printObjects(CsvMetaNode[] csvMetaNodes, Object[] objects)
				throws CsvException, IOException {
			for (int i = 0; i < objects.length; i++) {
				if(i == THRESHOLD) {
					int prevTotalLength = ((StringBuilder)out).length();
					int predictLength = (prevTotalLength / i + 1) * objects.length;
					StringBuilder out2 = new StringBuilder(predictLength);
					out2.append(out.toString());
					out = out2;
					try {
						Field outField = CSVPrinter.class.getDeclaredField("out");
						outField.setAccessible(true);
						outField.set(this.csvPrinter, out2);
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				printObject(csvMetaNodes, objects[i]);
			}
		}
		
		String getResult() {
			return ((StringBuilder)out).toString();
		}
	}

}
