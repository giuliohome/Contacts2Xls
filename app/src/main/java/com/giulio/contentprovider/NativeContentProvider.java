package com.giulio.contentprovider;

import java.util.ArrayList;



import android.Manifest;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class NativeContentProvider extends Activity implements showMsg {
	
    private static NativeContentProvider thisClass;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nativecontentprovider);
        SystemBars.applyInsets(this);
        thisClass = this;

            Button view = (Button)findViewById(R.id.viewButton);
            Button select = (Button)findViewById(R.id.selectButton);
            Button add = (Button)findViewById(R.id.createButton);
            Button openXls = (Button)findViewById(R.id.openXlsButton);
            Button merge = (Button)findViewById(R.id.mergeButton);
            final EditText xlsName = (EditText)findViewById(R.id.xls_name_open);
            
            view.setOnClickListener(new OnClickListener() {
             	public void onClick(View v){
             		if (ContextCompat.checkSelfPermission(thisClass,
             		        Manifest.permission.READ_CONTACTS)
             		        != PackageManager.PERMISSION_GRANTED) {
             			ActivityCompat.requestPermissions(thisClass,
             	                new String[]{Manifest.permission.READ_CONTACTS,
             						Manifest.permission.WRITE_CONTACTS},
             	                MY_PERMISSIONS_REQUEST_ExportContacts);
             		} else {
                		askExportLocation();
             		}
            	}
            });
            
            select.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (ContextCompat.checkSelfPermission(thisClass,
             		        Manifest.permission.READ_CONTACTS)
             		        != PackageManager.PERMISSION_GRANTED ||
             		    ContextCompat.checkSelfPermission(thisClass,
                    		 Manifest.permission.WRITE_CONTACTS)
                    		 != PackageManager.PERMISSION_GRANTED ) {
             			ActivityCompat.requestPermissions(thisClass,
             	                new String[]{Manifest.permission.READ_CONTACTS,
             						Manifest.permission.WRITE_CONTACTS},
             	                MY_PERMISSIONS_REQUEST_SelectXlsContacts);
             		} else {
						ContactsListActivity.arrayOfContacts.clear();
             			selectXlsContacts(xlsName.getText().toString());
             		}
					
				}
			});

            add.setOnClickListener(new OnClickListener() {
             	public void onClick(View v){
					if (ContextCompat.checkSelfPermission(thisClass,
             		        Manifest.permission.READ_CONTACTS)
             		        != PackageManager.PERMISSION_GRANTED ||
                 		    ContextCompat.checkSelfPermission(thisClass,
                           		 Manifest.permission.WRITE_CONTACTS)
                           		 != PackageManager.PERMISSION_GRANTED)  {
             			ActivityCompat.requestPermissions(thisClass,
             	                new String[]{Manifest.permission.READ_CONTACTS,
             						Manifest.permission.WRITE_CONTACTS},
             	                MY_PERMISSIONS_REQUEST_CreateContacts);
             		} else {
                		pickXls(REQ_OPEN_IMPORT);
                		//toast already inside above call
                		//showMsg("Completed Importing Contact numbers");
             		}
             		
            	}
            });

            openXls.setOnClickListener(new OnClickListener() {
             	public void onClick(View v){

					if (ContextCompat.checkSelfPermission(thisClass,
             		        Manifest.permission.READ_CONTACTS)
             		        != PackageManager.PERMISSION_GRANTED ||
                 		    ContextCompat.checkSelfPermission(thisClass,
                           		 Manifest.permission.WRITE_CONTACTS)
                           		 != PackageManager.PERMISSION_GRANTED ) {
             			ActivityCompat.requestPermissions(thisClass,
             	                new String[]{Manifest.permission.READ_CONTACTS,
             						Manifest.permission.WRITE_CONTACTS},
             	                MY_PERMISSIONS_REQUEST_OpenXlsContacts);
             		} else {
                		pickXls(REQ_OPEN_LIST);
             		}
            	}
            });

            merge.setOnClickListener(new OnClickListener() {
             	public void onClick(View v){

					if (ContextCompat.checkSelfPermission(thisClass,
             		        Manifest.permission.READ_CONTACTS)
             		        != PackageManager.PERMISSION_GRANTED ||
                 		    ContextCompat.checkSelfPermission(thisClass,
                           		 Manifest.permission.WRITE_CONTACTS)
                           		 != PackageManager.PERMISSION_GRANTED ) {
             			ActivityCompat.requestPermissions(thisClass,
             	                new String[]{Manifest.permission.READ_CONTACTS,
             						Manifest.permission.WRITE_CONTACTS},
             	                MY_PERMISSIONS_REQUEST_MergeContacts);
             		} else {
                		pickXls(REQ_OPEN_MERGE);
             		}
            	}
            });
    }
    
    private static final int MY_PERMISSIONS_REQUEST_ExportContacts = 1;
    private static final int MY_PERMISSIONS_REQUEST_SelectXlsContacts = 2;
    private static final int MY_PERMISSIONS_REQUEST_CreateContacts = 3;
    private static final int MY_PERMISSIONS_REQUEST_OpenXlsContacts = 4;
    private static final int MY_PERMISSIONS_REQUEST_MergeContacts = 5;
    
    @Override
    public void onRequestPermissionsResult(int requestCode,
            String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ExportContacts: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 2
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                	EditText xlsName = (EditText)findViewById(R.id.xls_name_open);
            		askExportLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
            		showMsg("You didn't authorize the app");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
            
            case MY_PERMISSIONS_REQUEST_SelectXlsContacts: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 2
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                	EditText xlsName = (EditText)findViewById(R.id.xls_name_open);
                	selectXlsContacts(xlsName.getText().toString());
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
            		showMsg("You didn't authorize the app");
                }
                return;
            }

            
            case MY_PERMISSIONS_REQUEST_CreateContacts: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 2
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                	EditText xlsName = (EditText)findViewById(R.id.xls_name_open);
            		pickXls(REQ_OPEN_IMPORT);
            		//toast already inside above call
            		//showMsg("Completed Importing Contact numbers");
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
            		showMsg("You didn't authorize the app");
                }
                return;
            }
            
            case MY_PERMISSIONS_REQUEST_OpenXlsContacts: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 2
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                	EditText xlsName = (EditText)findViewById(R.id.xls_name_open);
            		pickXls(REQ_OPEN_LIST);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
            		showMsg("You didn't authorize the app");
                }
                return;
            }   
            
            case MY_PERMISSIONS_REQUEST_MergeContacts: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 2
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                	EditText xlsName = (EditText)findViewById(R.id.xls_name_open);
            		pickXls(REQ_OPEN_MERGE);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
            		showMsg("You didn't authorize the app");
                }
                return;
            }   
                        
        }
    }
    
    protected static void showMsg(String msg) {
    	Toast.makeText(thisClass.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

	@Override
	public void showText(String msg) {
		showMsg(msg);
	}
    // Requests to the system file picker (Storage Access Framework).
    private static final int REQ_SAVE_EXPORT = 101;
    private static final int REQ_OPEN_LIST = 102;
    private static final int REQ_OPEN_MERGE = 103;
    private static final int REQ_OPEN_IMPORT = 104;

    /** Export All: the user picks folder and name, the contacts are written in onActivityResult. */
    private void askExportLocation() {
    	EditText xlsName = (EditText)findViewById(R.id.xls_name_open);
    	XlsFiles.pickSaveLocation(this, xlsName.getText().toString(), null, REQ_SAVE_EXPORT);
    }

    private void pickXls(final int requestCode) {
    	XlsFiles.pickFileToOpen(this, requestCode, new XlsFiles.OnPicked() {
    		@Override
    		public void onPicked(Uri uri) {
    			onXlsPicked(requestCode, uri);
    		}
    	});
    }

    private void onXlsPicked(int requestCode, Uri uri) {
    	switch (requestCode) {
    		case REQ_OPEN_LIST: openXlsContacts(uri); break;
    		case REQ_OPEN_MERGE: mergeContacts(uri); break;
    		case REQ_OPEN_IMPORT: createContacts(uri); break;
    	}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
    	if (requestCode == REQ_SAVE_EXPORT) {
    		ArrayList<Contact> all = Contact.fromAndroid(getContentResolver());
    		XlsFiles.showResult(this, XlsFiles.save(this, data.getData(), all));
    	} else {
    		onXlsPicked(requestCode, data.getData());
    	}
    }
    
    protected static void createContact(String name, String phone, String type, String account) {
    	ContentResolver cr = thisClass.getContentResolver();
    	
    	Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
    	
    	if (cur.getCount() > 0) {
        	while (cur.moveToNext()) {
        		String existName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        		if (existName !=  null && existName.equalsIgnoreCase(name)) {
                	Toast.makeText(thisClass,"The contact name: " + name + " already exists", Toast.LENGTH_SHORT).show();
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
    
    private void openXlsContacts(Uri uri) {
    	try {
        	Intent contactsListIntent = new Intent(thisClass, ContactsListActivity.class);
    		contactsListIntent.putExtra(XlsFiles.EXTRA_URI, uri);
    		contactsListIntent.putExtra(XlsFiles.EXTRA_NAME, XlsFiles.displayName(this, uri));
        	startActivity(contactsListIntent);
    		
    	} catch(Exception e) {
    		showMsg(e.toString());
    	}
    }
    
    private void mergeContacts(Uri uri) {
    	try {
        	Intent mergeIntent = new Intent(thisClass, MergeContactactivity.class);
        	mergeIntent.putExtra(XlsFiles.EXTRA_URI, uri);
        	mergeIntent.putExtra(XlsFiles.EXTRA_NAME, XlsFiles.displayName(this, uri));
        	startActivity(mergeIntent);
    		
    	} catch(Exception e) {
    		showMsg(e.toString());
    	}
    }
    
    private void selectXlsContacts(String xlsname) {
    	try {
			Intent selectContactIntent = new Intent(thisClass, SelectContactActivity.class);
			selectContactIntent.putExtra(XlsFiles.EXTRA_NAME, xlsname);
			startActivity(selectContactIntent);
		} catch (Exception e) {
    		showMsg(e.toString());
		}
    }
    
    private void createContacts(Uri source) {
    	ArrayList<Contact> contacts;
    	try {
    		contacts = XlsFiles.read(this, source);
    	} catch (Exception e) {
    		XlsFiles.showResult(this, "Import failed: " + XlsFiles.errorText(e));
    		return;
    	}
    	for (Contact c : contacts) {
    		createContact(c.name, c.number, c.type, c.account);
    	}
    	Toast.makeText(NativeContentProvider.this, "Imported contacts from: " + XlsFiles.displayName(this, source), Toast.LENGTH_LONG).show();
    }

    

}