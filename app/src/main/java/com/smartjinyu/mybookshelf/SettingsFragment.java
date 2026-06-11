package com.smartjinyu.mybookshelf;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;

import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.list.DialogMultiChoiceExtKt;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVWriter;
import com.smartjinyu.mybookshelf.database.BookBaseHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * settings fragment
 * Created by smartjinyu on 2017/2/8.
 */

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = "SettingsFragment";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Preference backupPreference;
    private Preference restorePreference;
    private Preference webServicesPreference;
    private Preference exportCSVPreference;

    private SharedPreferences sharedPreferences;

    private List<Integer> exportCSVList = null;

    private final ActivityResultLauncher<Intent> createBackupLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Log.i(TAG, "Create backup file, uri = " + result.getData().getData());
                    executeBackupTask(result.getData().getData());
                }
            });

    private final ActivityResultLauncher<Intent> openBackupLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Log.i(TAG, "Restore backup file, uri = " + result.getData().getData());
                    DialogHelper.show(getActivity(),
                            R.string.restore_confirm_dialog_title,
                            R.string.restore_confirm_dialog_content,
                            android.R.string.ok,
                            android.R.string.cancel,
                            () -> executeRestoreTask(result.getData().getData()),
                            null);
                }
            });

    private final ActivityResultLauncher<Intent> exportCsvLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Log.i(TAG, "Export CSV file, uri = " + result.getData().getData());
                    executeExportCsvTask(result.getData().getData());
                }
            });

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        setBackupCategory();
        setWebServicesPreference();
        setExportCSVPreference();
    }

    private void setExportCSVPreference() {
        exportCSVPreference = findPreference("settings_pref_export_to_csv");
        exportCSVPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                exportToCSV();
                return false;
            }
        });
    }

    private void exportToCSV() {
        CharSequence[] csvItems = getResources().getTextArray(R.array.export_csv_dialog_list);
        int[] initialSelected = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        final int[] currentSelection = initialSelected.clone();
        java.util.List<CharSequence> csvItemList = new java.util.ArrayList<>();
        for (CharSequence cs : csvItems) csvItemList.add(cs);

        MaterialDialog csvDialog = new MaterialDialog(getActivity(), null);
        csvDialog.title(R.string.export_csv_dialog_title, null);
        DialogMultiChoiceExtKt.listItemsMultiChoice(csvDialog, null, csvItemList, null, initialSelected, true, false, (dialog, indices, texts) -> {
                    if (indices.length < 2) {
                        Toast.makeText(getActivity(), R.string.export_csv_dialog_at_least_toast, Toast.LENGTH_SHORT).show();
                    } else {
                        System.arraycopy(indices, 0, currentSelection, 0, Math.min(indices.length, currentSelection.length));
                    }
                    return kotlin.Unit.INSTANCE;
                });
        csvDialog.positiveButton(android.R.string.ok, null, d -> {
                    final int[] selectedIndices = currentSelection.clone();
                    MaterialDialog cautionDialog = new MaterialDialog(getActivity(), null);
                    cautionDialog.title(R.string.export_csv_caution_dialog_title, null);
                    cautionDialog.message(R.string.export_csv_caution_dialog_content, null, null);
                    cautionDialog.positiveButton(android.R.string.ok, null, d2 -> {
                                java.util.List<Integer> exportList = new java.util.ArrayList<>();
                                for (int idx : selectedIndices) exportList.add(idx);
                                exportCSVList = exportList;
                                String filename = "Bookshelf_CSV_" + BuildConfig.VERSION_CODE + "_"
                                        + Calendar.getInstance().getTimeInMillis() + ".csv";
                                Intent backupFileIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                                backupFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                                backupFileIntent.setType("text/csv");
                                backupFileIntent.putExtra(Intent.EXTRA_TITLE, filename);
                                try {
                                    exportCsvLauncher.launch(backupFileIntent);
                                } catch (ActivityNotFoundException e) {
                                    Log.e(TAG, "No Document Provider Available");
                                    Toast.makeText(getActivity(), R.string.settings_no_document_provider_toast, Toast.LENGTH_LONG).show();
                                }
                                d.dismiss();
                                return kotlin.Unit.INSTANCE;
                            });
                    cautionDialog.negativeButton(android.R.string.cancel, null, d2 -> {
                                d2.dismiss();
                                d.dismiss();
                                return kotlin.Unit.INSTANCE;
                            });
                    cautionDialog.show();
                    return kotlin.Unit.INSTANCE;
                });
        csvDialog.negativeButton(android.R.string.cancel, null, d -> {
                    d.dismiss();
                    return kotlin.Unit.INSTANCE;
                });
        csvDialog.noAutoDismiss();
        csvDialog.show();
    }


    private void setWebServicesPreference() {
        webServicesPreference = findPreference("settings_pref_web_services");
        String rawWS = sharedPreferences.getString("webServices", null);
        final Integer[] initialSelected;
        if (rawWS != null) {
            Type type = new TypeToken<Integer[]>() {
            }.getType();
            Gson gson = new Gson();
            initialSelected = gson.fromJson(rawWS, type);
        } else {
            initialSelected = new Integer[]{0, 1, 2}; //three webServices
        }

        webServicesPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                CharSequence[] wsItems = getResources().getTextArray(R.array.settings_web_services_entries);
                int[] initialSel = new int[initialSelected.length];
                for (int i = 0; i < initialSelected.length; i++) initialSel[i] = initialSelected[i];
                final int[] currentSel = initialSel.clone();
                java.util.List<CharSequence> wsItemList = new java.util.ArrayList<>();
                for (CharSequence cs : wsItems) wsItemList.add(cs);

                MaterialDialog wsDialog = new MaterialDialog(getActivity(), null);
                wsDialog.title(R.string.settings_web_services_title, null);
                DialogMultiChoiceExtKt.listItemsMultiChoice(wsDialog, null, wsItemList, null, initialSel, true, false, (dialog, indices, texts) -> {
                            if (indices.length >= 1) {
                                System.arraycopy(indices, 0, currentSel, 0, Math.min(indices.length, currentSel.length));
                            } else {
                                Toast.makeText(getActivity(), R.string.settings_web_services_min_toast, Toast.LENGTH_SHORT).show();
                            }
                            return kotlin.Unit.INSTANCE;
                        });
                wsDialog.positiveButton(android.R.string.ok, null, d -> {
                            Gson gson = new Gson();
                            String toSave = gson.toJson(currentSel);
                            sharedPreferences.edit().putString("webServices", toSave).apply();
                            setWebServicesPreference();
                            return kotlin.Unit.INSTANCE;
                        });
                wsDialog.cancelOnTouchOutside(false);
                wsDialog.show();

                return false;
            }
        });

    }

    private void setBackupCategory() {
        backupPreference = findPreference("settings_pref_backup");
        backupPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String filename = "Bookshelf_backup_" + BuildConfig.VERSION_CODE + "_"
                        + Calendar.getInstance().getTimeInMillis() + ".zip";
                Intent backupFileIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                backupFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                backupFileIntent.setType("application/zip");
                backupFileIntent.putExtra(Intent.EXTRA_TITLE, filename);
                try {
                    createBackupLauncher.launch(backupFileIntent);
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "No Document Provider Available");

                    Toast.makeText(getActivity(), R.string.settings_no_document_provider_toast, Toast.LENGTH_LONG)
                            .show();
                }
                return false;
            }
        });

        restorePreference = findPreference("settings_pref_restore");
        restorePreference.setOnPreferenceClickListener(preference -> {
            Intent restoreFileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            restoreFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
            restoreFileIntent.setType("application/zip");
            // restoreFileIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri); // requires >= API 26
            try {
                openBackupLauncher.launch(restoreFileIntent);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "No Document Provider Available");

                Toast.makeText(getActivity(), R.string.settings_no_document_provider_toast, Toast.LENGTH_LONG)
                        .show();
            }
            return false;
        });

    }

    private void executeBackupTask(Uri uri) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setTitle(R.string.backup_progress_dialog_title);
        dialog.setMessage(getString(R.string.backup_progress_dialog_content));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        executorService.execute(() -> {
            boolean isSucceed = doBackupWork(uri);
            getActivity().runOnUiThread(() -> {
                dialog.dismiss();

                if (isSucceed) {
                    Toast.makeText(getActivity(), getString(R.string.backup_succeed_toast), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.backup_fail_toast), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private boolean doBackupWork(Uri zipFileUri) {
        List<String> fileName = new ArrayList<>();
        File covers = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String coverZipFileName = getActivity().getCacheDir() + "/Covers.zip";
        if (covers == null) {
            return false;
        }
        try {
            zipFiles(covers.listFiles(), Uri.fromFile(new File(coverZipFileName)));
            fileName.add(coverZipFileName);
            Log.i(TAG, "Cover temp zip created " + coverZipFileName);
        } catch (IOException e) {
            Log.e(TAG, "IOException when zipCovers = " + e.toString());
            return false;
        }
        fileName.add(getActivity().getDatabasePath(BookBaseHelper.DATABASE_NAME).getAbsolutePath());
        fileName.add(getActivity().getFilesDir().getParent() + "/shared_prefs/" + BookShelfLab.PreferenceName + ".xml");
        fileName.add(getActivity().getFilesDir().getParent() + "/shared_prefs/" + LabelLab.PreferenceName + ".xml");
        fileName.add(getActivity().getFilesDir().getParent() + "/shared_prefs/" + getActivity().getPackageName() + "_preferences.xml");
        try {
            zipFiles(fileName.toArray(new String[0]), zipFileUri);
            Log.i(TAG, "Backup created " + zipFileUri);
        } catch (IOException e) {
            Log.e(TAG, "IOException when zipFiles = " + e.toString());
            return false;
        }
        File coverZipFile = new File(coverZipFileName);
        boolean deleted = coverZipFile.delete();
        Log.i(TAG, "Cover.zip name = " + coverZipFileName + ", delete result = " + deleted);
        return true;
    }

    private void zipFiles(File[] files, Uri zipFileUri) throws IOException {
        if (files.length != 0) {
            int BUFFER_SIZE = 2048;
            try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(getActivity().getContentResolver().openOutputStream(zipFileUri)))) {
                byte[] data = new byte[BUFFER_SIZE];
                for (File file : files) {
                    if (file.exists()) {
                        FileInputStream fi = new FileInputStream(file);
                        try (BufferedInputStream origin = new BufferedInputStream(fi, BUFFER_SIZE)) {
                            String fileName = file.getAbsolutePath();
                            ZipEntry entry = new ZipEntry(fileName.substring(fileName.lastIndexOf("/") + 1));
                            out.putNextEntry(entry);
                            int count;
                            while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                                out.write(data, 0, count);
                            }
                        }
                    }
                }
            }
        }
    }

    private void zipFiles(String[] fileNames, Uri zipFileUri) throws IOException {
        File[] files = new File[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            files[i] = new File(fileNames[i]);
        }
        zipFiles(files, zipFileUri);
    }

    private void executeRestoreTask(Uri uri) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setTitle(R.string.restore_progress_dialog_title);
        dialog.setMessage(getString(R.string.backup_progress_dialog_content));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        executorService.execute(() -> {
            boolean isSucceed = doRestoreWork(uri);
            getActivity().runOnUiThread(() -> {
                dialog.dismiss();

                if (isSucceed) {
                    Toast.makeText(getActivity(), getString(R.string.restore_succeed_toast), Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(this::restartApp, 2000);
                    Log.i(TAG, "Restore successfully!");
                } else {
                    Toast.makeText(getActivity(), getString(R.string.restore_fail_toast), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private boolean doRestoreWork(Uri backupFileUri) {
        String unZipDir = getActivity().getCacheDir() + "/restoreTemp";
        try {
            unzip(backupFileUri, unZipDir);
        } catch (IOException e) {
            Log.e(TAG, "Unzip failed 1, ioe = " + e.toString());
            return false;
        }
        File unzipDirectory = new File(unZipDir);
        String[] fileNames = unzipDirectory.list();
        for (String file : fileNames) {
            if (file.equals("Covers.zip")) {
                try {
                    unzip(Uri.fromFile(new File(unZipDir + "/Covers.zip")), unZipDir + "/Covers");
                    File srcCoverFolder = new File(unZipDir + "/Covers");
                    File[] covers = srcCoverFolder.listFiles();
                    if (getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) != null) {
                        String destCoverFolder = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
                        File destCover = new File(destCoverFolder);
                        destCover.delete();
                        for (File cover : covers) {
                            copyFile(cover, new File(destCoverFolder + "/" + cover.getName()));
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Unzip failed 2, ioe = " + e.toString());
                    return false;
                }
                continue;
            }
            if (file.equals(BookShelfLab.PreferenceName + ".xml")) {
                String src = unZipDir + "/" + BookShelfLab.PreferenceName + ".xml";
                String dest = getActivity().getFilesDir().getParent() + "/shared_prefs/" + BookShelfLab.PreferenceName + ".xml";
                copyFile(new File(src), new File(dest));
                continue;
            }
            if (file.equals(LabelLab.PreferenceName + ".xml")) {
                String src = unZipDir + "/" + LabelLab.PreferenceName + ".xml";
                String dest = getActivity().getFilesDir().getParent() + "/shared_prefs/" + LabelLab.PreferenceName + ".xml";
                copyFile(new File(src), new File(dest));
                continue;
            }
            if (file.equals(getActivity().getPackageName() + "_preferences.xml")) {
                String src = unZipDir + "/" + getActivity().getPackageName() + "_preferences.xml";
                String dest = getActivity().getFilesDir().getParent() + "/shared_prefs/" + getActivity().getPackageName() + "_preferences.xml";
                copyFile(new File(src), new File(dest));
                continue;
            }
            if (file.equals(BookBaseHelper.DATABASE_NAME)) {
                String src = unZipDir + "/" + BookBaseHelper.DATABASE_NAME;
                String dest = getActivity().getDatabasePath(BookBaseHelper.DATABASE_NAME).getAbsolutePath();
                File tempFile = new File(dest + "-shm");
                if (tempFile.exists()) tempFile.delete();
                tempFile = new File(dest + "-wal");
                if (tempFile.exists()) tempFile.delete();
                copyFile(new File(src), new File(dest));
            }
        }
        if (unzipDirectory.exists()) {
            deleteRecursive(unzipDirectory);
        }
        return true;
    }

    private void unzip(Uri zipFileUri, String location) throws IOException {
        int size;
        int BUFFER_SIZE = 2048;
        byte[] buffer = new byte[BUFFER_SIZE];
        if (!location.endsWith("/")) {
            location += "/";
        }
        File file = new File(location);
        if (!file.isDirectory()) {
            file.mkdirs();
        }
        try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(getActivity().getContentResolver().openInputStream(zipFileUri)))) {
            ZipEntry ze;
            while ((ze = zipInputStream.getNextEntry()) != null) {
                String path = location + ze.getName();
                File unzipFile = new File(path);
                if (ze.isDirectory()) {
                    if (!unzipFile.isDirectory()) {
                        unzipFile.mkdirs();
                    }
                } else {
                    File parentDir = unzipFile.getParentFile();
                    if (parentDir != null && !parentDir.isDirectory()) {
                        parentDir.mkdirs();
                    }
                    FileOutputStream out = new FileOutputStream(unzipFile, false);
                    BufferedOutputStream fout = new BufferedOutputStream(out, BUFFER_SIZE);
                    try {
                        while ((size = zipInputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
                            fout.write(buffer, 0, size);
                        }
                        zipInputStream.closeEntry();
                    } finally {
                        fout.flush();
                        fout.close();
                    }
                }
            }
        }
    }

    private boolean copyFile(File src, File dst) {
        if (!dst.getParentFile().exists()) {
            dst.getParentFile().mkdirs();
        }
        if (dst.exists()) {
            dst.delete();
        }
        try (FileInputStream inStream = new FileInputStream(src);
             FileOutputStream outStream = new FileOutputStream(dst)) {
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            Log.i(TAG, "Copy file succeed, src = " + src.getAbsolutePath() + ", dest = " + dst.getAbsolutePath());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Copy file IOException = " + e.toString() + ", src = " + src.getAbsolutePath() + ", dest = " + dst.getAbsolutePath());
            return false;
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        try {
            if (fileOrDirectory.isDirectory()) {
                for (File child : fileOrDirectory.listFiles()) {
                    deleteRecursive(child);
                }
            }
            fileOrDirectory.delete();
        } catch (Exception e) {
            Log.e(TAG, "Exception when deleting restore unzip folder: " + e);
        }
    }

    private void executeExportCsvTask(Uri uri) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setTitle(R.string.export_progress_dialog_title);
        dialog.setMessage(getString(R.string.export_progress_dialog_content));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        executorService.execute(() -> {
            boolean isSucceed = doExportCsvWork(uri);
            getActivity().runOnUiThread(() -> {
                dialog.dismiss();

                if (isSucceed) {
                    Toast.makeText(getActivity(), getString(R.string.export_csv_export_succeed_toast), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.export_csv_export_fail_toast), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private boolean doExportCsvWork(Uri csvFileUri) {
        Calendar mCalendar = Calendar.getInstance();
        try (OutputStream outputStream = getActivity().getContentResolver().openOutputStream(csvFileUri)) {
            List<Book> mBooks = BookLab.get(getActivity()).getBooks();
            int sortMethod = sharedPreferences.getInt("SORT_METHOD", 0);
            Comparator<Book> comparator;
            switch (sortMethod) {
                case 0:
                    comparator = new Book.titleComparator();
                    break;
                case 1:
                    comparator = new Book.authorComparator();
                    break;
                case 2:
                    comparator = new Book.publisherComparator();
                    break;
                case 3:
                    comparator = new Book.pubtimeComparator();
                    break;
                default:
                    comparator = new Book.titleComparator();
            }
            Collections.sort(mBooks, comparator);

            int[] items = new int[11];
            if (exportCSVList != null) {
                for (int i : exportCSVList) {
                    items[i] = 1;
                }
            }
            outputStream.write(0xef);
            outputStream.write(0xbb);
            outputStream.write(0xbf);
            CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(outputStream));
            List<String> titleBar = new ArrayList<>();
            titleBar.add(getString(R.string.export_csv_file_order));
            for (int i = 0; i < 11; i++) {
                if (items[i] == 1) {
                    titleBar.add(getResources().getStringArray(R.array.export_csv_dialog_list)[i]);
                }
            }
            csvWriter.writeNext(titleBar.toArray(new String[0]));

            for (int i = 1; i <= mBooks.size(); i++) {
                List<String> entry = new ArrayList<>();
                Book mBook = mBooks.get(i - 1);
                entry.add(Integer.toString(i));
                if (items[0] == 1) {
                    entry.add(mBook.getTitle());
                }
                if (items[1] == 1) {
                    String authors = mBook.getFormatAuthor();
                    entry.add(authors != null ? authors : "");
                }
                if (items[2] == 1) {
                    String translators = mBook.getFormatTranslator();
                    entry.add(translators != null ? translators : "");
                }
                if (items[3] == 1) {
                    entry.add(mBook.getPublisher());
                }
                if (items[4] == 1) {
                    Calendar calendar = mBook.getPubTime();
                    int year = calendar.get(Calendar.YEAR);
                    if (year == 9999) {
                        entry.add("");
                    } else {
                        int month = calendar.get(Calendar.MONTH) + 1;
                        entry.add(year + " - " + month);
                    }
                }
                if (items[5] == 1) {
                    entry.add(mBook.getIsbn());
                }
                if (items[6] == 1) {
                    String[] readingStatus = getResources().getStringArray(R.array.reading_status_array);
                    entry.add(readingStatus[mBook.getReadingStatus()]);
                }
                if (items[7] == 1) {
                    BookShelf bookShelf = BookShelfLab.get(getActivity()).getBookShelf(mBook.getBookshelfID());
                    entry.add(bookShelf.getTitle());
                }
                if (items[8] == 1) {
                    List<UUID> labelID = mBook.getLabelID();
                    if (labelID.size() != 0) {
                        StringBuilder labelsTitle = new StringBuilder();
                        for (UUID id : labelID) {
                            labelsTitle.append(LabelLab.get(getActivity()).getLabel(id).getTitle());
                            labelsTitle.append(",");
                        }
                        labelsTitle.deleteCharAt(labelsTitle.length() - 1);
                        entry.add(labelsTitle.toString());
                    } else {
                        entry.add("");
                    }
                }
                if (items[9] == 1) {
                    entry.add(mBook.getNotes());
                }
                if (items[10] == 1) {
                    entry.add(mBook.getWebsite());
                }
                csvWriter.writeNext(entry.toArray(new String[0]));
            }
            csvWriter.writeNext(new String[]{""});
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss EEE z");
            String exportTime = format.format(mCalendar.getTime());
            csvWriter.writeNext(new String[]{String.format(getString(R.string.export_csv_file_time), exportTime)});
            csvWriter.writeNext(new String[]{getString(R.string.export_csv_file_end)});
            csvWriter.writeNext(new String[]{getString(R.string.export_csv_file_copyright)});
            csvWriter.close();
        } catch (Exception e) {
            Log.e(TAG, "csvFileUri = " + csvFileUri + ", exception = " + e);
            return false;
        }
        return true;
    }

    private void restartApp() {
        Intent i = getActivity().getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        Runtime.getRuntime().exit(0);
    }
}
