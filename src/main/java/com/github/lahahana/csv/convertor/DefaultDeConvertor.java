package com.github.lahahana.csv.convertor;

public class DefaultDeConvertor implements DeConvertor<Object> {

	@Override
	public Object convert(String value) {
		return value;
	}

}
