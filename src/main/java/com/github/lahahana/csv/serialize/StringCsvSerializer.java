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

	private StringCsvSerializer(Builder builder) throws IOException {
		this.delegate = new InnerStringCsvSerializer(builder);
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
	
	public void close() throws IOException {
		delegate.close();
	}

	private class InnerStringCsvSerializer extends CsvSerializer {

		private static final int THRESHOLD = 10000;
		
		private int adjustiveThreshold = THRESHOLD;

		InnerStringCsvSerializer(StringCsvSerializer.Builder builder) throws IOException {
			super(new Builder(builder.out).csvFormat(builder.csvFormat));
		}

		@Override
		protected <T> void printObjects(CsvMetaNode<?>[] csvMetaNodes, Iterable<T> iterable)
				throws CsvException, IOException {
			int count = 0;
			int totalSize = ((Collection<T>) iterable).size();
			for (Object obj : iterable) {
				if (count++ == adjustiveThreshold) {
					int currentLength = ((StringBuilder) out).length();
					int currentCapacity = ((StringBuilder) out).capacity();
					int predictLength = calcPredictLength(count, totalSize, currentLength);
					if(currentCapacity < predictLength) {
						StringBuilder out2 = new StringBuilder(predictLength);
						out2.append(out);
						out = out2;
						updateFieldOfCSVPrinter();
					}
				}
				printObject(csvMetaNodes, obj);
			}
		}

		@Override
		protected <T> void printObjects(CsvMetaNode<?>[] csvMetaNodes, T[] objects) throws CsvException, IOException {
			final int totalSize = objects.length;
			for (int i = 0; i < objects.length; i++) {
				if (i == adjustiveThreshold) {
					adjustiveThreshold *= 2;
					int currentLength = ((StringBuilder) out).length();
					int currentCapacity = ((StringBuilder) out).capacity();
					int predictLength = calcPredictLength(i, totalSize, currentLength);
					if(currentCapacity < predictLength) {
						StringBuilder out2 = new StringBuilder(predictLength);
						out2.append(out);
						out = out2;
						updateFieldOfCSVPrinter();
					}
				}
				printObject(csvMetaNodes, objects[i]);
			}
		}

		String getResult() {
			return ((StringBuilder) out).toString();
		}

		private void updateFieldOfCSVPrinter() {
			try {
				Field outField = CSVPrinter.class.getDeclaredField("out");
				outField.setAccessible(true);
				outField.set(this.csvPrinter, out);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		private int calcPredictLength(int count, int totalSize, int currentlength ) {
			return (currentlength/count + 1) * (totalSize - count) + currentlength;
		}
	}

	public static final class Builder {
		Appendable out = new StringBuilder();
		CSVFormat csvFormat = CSVFormat.DEFAULT;

		public Builder() {
			super();
		}

		public StringCsvSerializer build() throws IOException {
			return new StringCsvSerializer(this);
		}
	}

}
