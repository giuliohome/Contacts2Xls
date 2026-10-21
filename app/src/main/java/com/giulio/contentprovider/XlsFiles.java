package com.giulio.contentprovider;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;

/**
 * Reads and writes the contacts .xls through the system file picker (Storage Access Framework):
 * the user chooses the folder and the name, and the app needs no storage permission.
 */
final class XlsFiles {

	static final String XLS_MIME = "application/vnd.ms-excel";
	static final String DEFAULT_NAME = "MyExportedContacts.xls";
	/** Extras used to hand a picked file to the list screens. */
	static final String EXTRA_URI = "xls_uri";
	static final String EXTRA_NAME = "open_xls_name";

	interface OnPicked {
		void onPicked(Uri uri);
	}

	private XlsFiles() {}

	/** Opens the system "Save as" screen; the chosen document comes back to onActivityResult(requestCode). */
	static void pickSaveLocation(Activity activity, String suggestedName, Uri near, int requestCode) {
		String name = suggestedName == null ? "" : suggestedName.trim();
		if (name.length() == 0) name = DEFAULT_NAME;
		if (!name.toLowerCase(Locale.ROOT).endsWith(".xls")) name += ".xls";

		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType(XLS_MIME);
		intent.putExtra(Intent.EXTRA_TITLE, name);
		// Start in the folder of the file that was opened, when there is one.
		if (near != null && Build.VERSION.SDK_INT >= 26
				&& ContentResolver.SCHEME_CONTENT.equals(near.getScheme())) {
			intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, near);
		}
		startPicker(activity, intent, requestCode);
	}

	/**
	 * Lets the user choose an .xls to open. Files saved by versions up to 1.20 live in the app's
	 * private folder, which the system picker cannot show, so those are offered first.
	 * A file from the picker comes back to onActivityResult(requestCode), an old one to onOldFile.
	 */
	static void pickFileToOpen(final Activity activity, final int requestCode, final OnPicked onOldFile) {
		final File[] old = filesInAppFolder(activity);
		if (old.length == 0) {
			browse(activity, requestCode);
			return;
		}
		String[] items = new String[old.length + 1];
		for (int i = 0; i < old.length; i++) {
			items[i] = old[i].getName() + "  (saved in the app)";
		}
		items[old.length] = "Browse phone folders…";
		new AlertDialog.Builder(activity)
				.setTitle("Open which file?")
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which < old.length) {
							onOldFile.onPicked(Uri.fromFile(old[which]));
						} else {
							browse(activity, requestCode);
						}
					}
				})
				.show();
	}

	private static void browse(Activity activity, int requestCode) {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {
				XLS_MIME, "application/x-msexcel", "application/excel", "application/octet-stream" });
		startPicker(activity, intent, requestCode);
	}

	private static void startPicker(Activity activity, Intent intent, int requestCode) {
		try {
			activity.startActivityForResult(intent, requestCode);
		} catch (ActivityNotFoundException e) {
			showResult(activity, "No file picker is available on this device.");
		}
	}

	private static File[] filesInAppFolder(Context ctx) {
		File dir = ctx.getExternalFilesDir(null);
		File[] files = dir == null ? null : dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isFile() && f.getName().toLowerCase(Locale.ROOT).endsWith(".xls");
			}
		});
		if (files == null) return new File[0];
		Arrays.sort(files);
		return files;
	}

	/** Writes the contacts to the document the user picked and returns a message for the user. */
	static String save(Context ctx, Uri target, ArrayList<Contact> contacts) {
		int saved = 0;
		OutputStream out = null;
		try {
			out = openForWriting(ctx, target);
			WritableWorkbook wb = Workbook.createWorkbook(out);
			WritableSheet sheet = wb.createSheet("Contacts", 1);
			int row = 1;
			for (Contact contact : contacts) {
				if (contact.name == null) continue;
				sheet.addCell(new Label(1, row, contact.name));
				sheet.addCell(new Label(2, row, contact.number));
				sheet.addCell(new Label(3, row, contact.type));
				sheet.addCell(new Label(4, row, contact.account));
				row++;
				saved++;
			}
			wb.write();
			wb.close();
		} catch (Exception e) {
			return "Save failed: " + errorText(e);
		} finally {
			closeQuietly(out);
		}
		return "Saved " + saved + " contacts to " + displayName(ctx, target);
	}

	private static OutputStream openForWriting(Context ctx, Uri uri) throws FileNotFoundException {
		ContentResolver cr = ctx.getContentResolver();
		try {
			// "wt" truncates when the user picked an existing file to replace.
			OutputStream out = cr.openOutputStream(uri, "wt");
			if (out != null) return out;
		} catch (Exception e) {
			// Some providers do not support "wt": fall back to plain "w" below.
		}
		OutputStream out = cr.openOutputStream(uri, "w");
		if (out == null) throw new FileNotFoundException("Cannot write to " + displayName(ctx, uri));
		return out;
	}

	/** Reads the "Contacts" sheet written by this app: name, number, type, account in columns B to E. */
	static ArrayList<Contact> read(Context ctx, Uri source) throws Exception {
		ArrayList<Contact> contacts = new ArrayList<Contact>();
		Workbook wb;
		InputStream in = null;
		try {
			in = ctx.getContentResolver().openInputStream(source);
			if (in == null) throw new FileNotFoundException("Cannot open " + displayName(ctx, source));
			wb = Workbook.getWorkbook(in);
		} finally {
			closeQuietly(in);
		}
		try {
			Sheet sheet = wb.getSheet("Contacts");
			if (sheet == null) {
				throw new Exception(displayName(ctx, source) + " has no \"Contacts\" sheet.");
			}
			int rows = sheet.getRows();
			for (int row = 1; row < rows && sheet.getColumns() > 1; row++) {
				String name = cell(sheet, 1, row);
				if (name.length() == 0) break;
				contacts.add(new Contact(name, cell(sheet, 2, row), cell(sheet, 3, row), cell(sheet, 4, row), false));
			}
		} finally {
			wb.close();
		}
		Collections.sort(contacts, new ContactComparator());
		return contacts;
	}

	private static String cell(Sheet sheet, int col, int row) {
		if (col >= sheet.getColumns()) return "";
		String s = sheet.getCell(col, row).getContents();
		return s == null ? "" : s;
	}

	static String displayName(Context ctx, Uri uri) {
		if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
			return uri.getLastPathSegment();
		}
		Cursor c = null;
		try {
			c = ctx.getContentResolver().query(uri, new String[] { OpenableColumns.DISPLAY_NAME }, null, null, null);
			if (c != null && c.moveToFirst() && c.getString(0) != null) {
				return c.getString(0);
			}
		} catch (Exception e) {
			// Fall back to the last part of the URI below.
		} finally {
			if (c != null) c.close();
		}
		return uri.getLastPathSegment();
	}

	/** Shows an outcome in a dialog, so longer messages are readable in full. */
	static void showResult(Activity activity, String msg) {
		new AlertDialog.Builder(activity)
				.setMessage(msg)
				.setPositiveButton(android.R.string.ok, null)
				.show();
	}

	static String errorText(Exception e) {
		return e.getMessage() != null ? e.getMessage() : e.toString();
	}

	private static void closeQuietly(Closeable c) {
		if (c == null) return;
		try {
			c.close();
		} catch (Exception ignored) {
		}
	}
}
