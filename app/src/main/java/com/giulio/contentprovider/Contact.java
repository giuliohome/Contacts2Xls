package com.giulio.contentprovider;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import android.os.Environment;
import android.provider.ContactsContract;
import android.widget.ListAdapter;
import android.widget.Toast;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;



public class Contact {
    public String name;
    public String number;
    public String type;
    public String account;
    public Boolean export = false;

    public Contact(String name, String number, String type, String account, Boolean export) {
       this.name = name;
       this.number = number;
       this.type = type;
       this.account = account;
       this.export = export;
    }
    
    public static ArrayList<Contact> fromStub(String path, showMsg delegate) {
    	ArrayList<Contact> contacts = new ArrayList<Contact>();
    	contacts.add(new Contact("giulio", "123","whatsapp","giulio8", false));
    	contacts.add(new Contact("the best", "007","gmail","giuliohome", false));
    	return contacts;
    }

    public static ArrayList<Contact> fromAndroid(ContentResolver cr ) {

		ArrayList<Contact> arrayOfContacts = new ArrayList<Contact>();
    	
    	Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
    	arrayOfContacts.clear();
    	if (cur.getCount() > 0) {
    		while (cur.moveToNext()) {
    			String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
        		String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        		if (Integer.parseInt(cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                     Cursor pCur = cr.query(
                    		 ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
                    		 null, 
                    		 ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
                    		 new String[]{id}, null);
                     while (pCur.moveToNext()) {
                    	 String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    	 //Toast.makeText(NativeContentProvider.this, "Name: " + name + ", Phone No: " + phoneNo, Toast.LENGTH_SHORT).show();
                    	 String type = pCur.getString(pCur.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
                    	 String account = pCur.getString(pCur.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));
                    	 
                    	 if (name == null) continue;
                    	 Contact currContact = new Contact(name,  phoneNo, type, account, true);
                    	 arrayOfContacts.add(currContact);
                     }
                     pCur.close();
        		}
    		}
    	}
    	cur.close();

		return arrayOfContacts;
    }
    
	public static ArrayList<Contact> fromXls(Context context, Uri source, showMsg delegate) {
		if (source == null) {
			delegate.showText("No file was chosen.");
			return new ArrayList<Contact>();
		}
		try {
			return XlsFiles.read(context, source);
		} catch (Exception e) {
			delegate.showText(XlsFiles.errorText(e));
			return new ArrayList<Contact>();
		}
	}
}
