package com.github.lahahana.csv.convertor;

public class DefaultDeserializationConvertor implements DeserializationConvertor<Object> {

	@Override
	public Object convert(String value) {
		return value;
	}

}
