package com.giulio.contentprovider;

import java.util.Comparator;

public class ContactComparator implements Comparator<Contact> {

	@Override
	public int compare(Contact arg0, Contact arg1) {
		// TODO Auto-generated method stub
		return arg0.name.compareToIgnoreCase(arg1.name);
	}

}
