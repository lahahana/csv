package com.github.lahahana.csv.serialize;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.github.lahahana.csv.base.CsvMetaNode;
import com.github.lahahana.csv.exceptions.CsvException;
import com.github.lahahana.csv.serialize.CsvSerializer.Builder;

public class StringCsvSerializer {
	
	private InnerStringCsvSerializer delegate;
	
	public StringCsvSerializer(Builder builder) throws IOException {
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
	
	private class InnerStringCsvSerializer extends CsvSerializer {
		
		private static final int THRESHOLD = 10000;
		
		InnerStringCsvSerializer(StringCsvSerializer.Builder builder) throws IOException {
			super(new Builder(builder.out).csvFormat(builder.csvFormat));
		}
		
		@Override
		protected <T> void printObjects(CsvMetaNode<?>[] csvMetaNodes, Iterable<T> iterable)
				throws CsvException, IOException {
			int count = 0;
			int totalLength = ((Collection<T>)iterable).size();
			for (Object obj : iterable) {
				if(count++ == THRESHOLD) {
					int predictLength = ((StringBuilder)out).length() / count * totalLength;
					StringBuilder out2 = new StringBuilder(predictLength);
					out2.append(out.toString());
					out = out2;
					updateFieldOfCSVPrinter();
				} 
				printObject(csvMetaNodes, obj);
			}
		}

		@Override
		protected <T> void printObjects(CsvMetaNode<?>[] csvMetaNodes, T[] objects)
				throws CsvException, IOException {
			for (int i = 0; i < objects.length; i++) {
				if(i == THRESHOLD) {
					int prevTotalLength = ((StringBuilder)out).length();
					int predictLength = (prevTotalLength / i + 1) * objects.length;
					StringBuilder out2 = new StringBuilder(predictLength);
					out2.append(out.toString());
					out = out2;
					updateFieldOfCSVPrinter();
				}
				printObject(csvMetaNodes, objects[i]);
			}
		}
		
		String getResult() {
			return ((StringBuilder)out).toString();
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
