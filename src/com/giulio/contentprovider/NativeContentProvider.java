package com.giulio.contentprovider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.giulio.contentprovider.R;

import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;



public class NativeContentProvider extends Activity {
	

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nativecontentprovider);

            Button view = (Button)findViewById(R.id.viewButton);
            Button add = (Button)findViewById(R.id.createButton);
            
            
            view.setOnClickListener(new OnClickListener() {
             	public void onClick(View v){
            		exportContacts("/Contacts2Xls");
            		Log.i("Contacts2Xls", "Completed Exporting Contact numbers");
            	}
            });

            add.setOnClickListener(new OnClickListener() {
             	public void onClick(View v){
            		createContacts("/Contacts2Xls");
            		Log.i("Contacts2Xls", "Completed Exporting Contact numbers");
            	}
            });
            
            
    }
    
    private void exportContacts(String path) {
    	WritableWorkbook wb;
    	WritableSheet sheet;
    	int row=1;
    	try {
    		File sdCard = Environment.getExternalStorageDirectory();
    		File dir = new File(sdCard.getAbsolutePath() + path);
    		dir.mkdirs();
    		File wbfile = new File(dir,"MyExportedContacts.xls");
			 wb = jxl.Workbook.createWorkbook(wbfile);
			 sheet = wb.createSheet("Contacts",1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(NativeContentProvider.this,e.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}
    	ContentResolver cr = getContentResolver();
    	
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
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
                    	 Label label1 = new Label(1,row,name);
                    	 Label label2 = new Label(2,row,phoneNo);
                    	 Label label3 = new Label(3,row,type);
                    	 Label label4 = new Label(4,row,account);
                    	 
                    	 
                    	 row++;
                    	 try {
							sheet.addCell(label1);
							sheet.addCell(label2);
							sheet.addCell(label3);
							sheet.addCell(label4);
						} catch (RowsExceededException e) {
							Toast.makeText(NativeContentProvider.this,e.getMessage(), Toast.LENGTH_SHORT).show();
							return;
						} catch (WriteException e) {
							Toast.makeText(NativeContentProvider.this,e.getMessage(), Toast.LENGTH_SHORT).show();
							return;
						}
                     } 
                     
      	        pCur.close();
      	    }
        	}
        }
        try {
        	wb.write();
			wb.close();
		} catch (WriteException e) {
			Toast.makeText(NativeContentProvider.this,e.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		} catch (IOException e) {
			Toast.makeText(NativeContentProvider.this,e.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}
    }
    
    private void createContact(String name, String phone, String type, String account) {
    	ContentResolver cr = getContentResolver();
    	
    	Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
    	
    	if (cur.getCount() > 0) {
        	while (cur.moveToNext()) {
        		String existName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        		if (existName !=  null && existName.contains(name)) {
                	Toast.makeText(NativeContentProvider.this,"The contact name: " + name + " already exists", Toast.LENGTH_SHORT).show();
                	return;        			
        		}
        	}
    	}
    	
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, type)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, account)
                .build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
                .build());

        
        try {
			cr.applyBatch(ContactsContract.AUTHORITY, ops);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
       
    private void createContacts(String path) {

        
    	Workbook wb;
    	Sheet sheet;
    	int row=1;
    	try {
    		File sdCard = Environment.getExternalStorageDirectory();
    		File dir = new File(sdCard.getAbsolutePath() + path);
    		dir.mkdirs();
    		File wbfile = new File(dir,"MyExportedContacts.xls");
			 wb = jxl.Workbook.getWorkbook(wbfile);
			 sheet = wb.getSheet("Contacts");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(NativeContentProvider.this,e.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}
    	
    	
    	row = 1;
    	String name=sheet.getCell(1, row).getContents();
    	String phone=sheet.getCell(2, row).getContents();
    	String type=sheet.getCell(3, row).getContents();
    	String account=sheet.getCell(4, row).getContents();
    	int rows = sheet.getRows();
    	while (name != null && name.length()>0)
    	{
    		createContact(name,phone, type, account);
    		row++;
    		if (row == rows) break;
    		name=sheet.getCell(1, row).getContents();
    		phone=sheet.getCell(2, row).getContents();
    		type=sheet.getCell(3, row).getContents();
    		account=sheet.getCell(4, row).getContents();
    	}
    	
    	
    	Toast.makeText(NativeContentProvider.this, "Imported contacts from folder: " + path, Toast.LENGTH_SHORT).show();
    	
    }
    

}