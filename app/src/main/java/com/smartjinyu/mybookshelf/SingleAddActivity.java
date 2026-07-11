package com.smartjinyu.mybookshelf;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.actions.DialogActionExtKt;
import com.afollestad.materialdialogs.WhichButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.Result;

import java.lang.reflect.Type;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by smartjinyu on 2017/1/20.
 * Scan barcode of a single book
 */

public class SingleAddActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private static final String TAG = "SingleAddActivity";

    private static final int CAMERA_PERMISSION = 1;


    private static final String FLASH_STATE = "FLASH_STATE";
    private ZXingScannerView mScannerView;
    private boolean mFlash;
    private Toolbar mToolbar;

    private Integer[] selectedServices;
    private int indexOfServiceTested;
    // the index of service in selectedServices has been tested.
    // Initially is -1,when the 0st one is tested, it is 0.
    // Caution it is selectedServices[x], instead of the id of webServices itself.


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }

        if (savedInstanceState != null) {
            mFlash = savedInstanceState.getBoolean(FLASH_STATE, false);
        } else {
            mFlash = false;
        }
        setContentView(R.layout.activity_single_add_scan);

        mToolbar = (Toolbar) findViewById(R.id.singleScanToolbar);
        mToolbar.setTitle(R.string.single_scan_toolbar);
        setSupportActionBar(mToolbar);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.singleScanFrame);

        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);
        mScannerView.setResultHandler(this);
        mScannerView.setAutoFocus(true);
        mScannerView.setFlash(mFlash);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem;

        if (mFlash) {
            menuItem = menu.add(Menu.NONE, R.id.menu_simple_add_flash, 0, R.string.menu_single_add_flash_on);
            menuItem.setIcon(R.drawable.ic_flash_on);
        } else {
            menuItem = menu.add(Menu.NONE, R.id.menu_simple_add_flash, 0, R.string.menu_single_add_flash_off);
            menuItem.setIcon(R.drawable.ic_flash_off);
        }

        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menuItem = menu.add(Menu.NONE, R.id.menu_simple_add_manually, 0, R.string.menu_single_add_manually);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        menuItem = menu.add(Menu.NONE, R.id.menu_simple_add_totally_manual, 1, R.string.menu_single_add_totally_manually);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        // add a book without isbn directly

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FLASH_STATE, mFlash);
    }


    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.setAutoFocus(true);
        mScannerView.setFlash(mFlash);
        mScannerView.startCamera();

    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_simple_add_flash:
                mFlash = !mFlash;
                if (mFlash) {
                    item.setTitle(R.string.menu_single_add_flash_on);
                    item.setIcon(R.drawable.ic_flash_on);
                } else {
                    item.setTitle(R.string.menu_single_add_flash_off);
                    item.setIcon(R.drawable.ic_flash_off);
                }
                mScannerView.setFlash(mFlash);
                break;
            case R.id.menu_simple_add_manually:
                mScannerView.stopCamera();
                MaterialDialog isbnDialog = new MaterialDialog(this, com.afollestad.materialdialogs.ModalDialog.INSTANCE);
                isbnDialog.title(R.string.input_isbn_manually_title, null);
                isbnDialog.message(R.string.input_isbn_manually_content, null, null);
                isbnDialog.positiveButton(R.string.input_isbn_manually_positive, null, d -> {
                    addBook(com.afollestad.materialdialogs.input.DialogInputExtKt.getInputField(d).getText().toString());
                    return kotlin.Unit.INSTANCE;
                });
                isbnDialog.negativeButton(android.R.string.cancel, null, d -> {
                    d.dismiss();
                    resumeCamera();
                    return kotlin.Unit.INSTANCE;
                });
                com.afollestad.materialdialogs.input.DialogInputExtKt.input(isbnDialog,
                        null, R.string.input_isbn_manually_edit_text, null, null,
                        InputType.TYPE_CLASS_NUMBER, 0, false, false, (d, input) -> {
                            int length = input.length();
                            if (length == 10 || length == 13) {
                                DialogActionExtKt.getActionButton(d, WhichButton.POSITIVE).setEnabled(true);
                            } else {
                                DialogActionExtKt.getActionButton(d, WhichButton.POSITIVE).setEnabled(false);
                            }
                            return kotlin.Unit.INSTANCE;
                        });
                isbnDialog.noAutoDismiss();
                isbnDialog.show();
                // Initially disable "Add" button until valid ISBN entered
                DialogActionExtKt.getActionButton(isbnDialog, WhichButton.POSITIVE).setEnabled(false);
                break;

            case R.id.menu_simple_add_totally_manual:
                Book mBook = new Book();
                Intent i = new Intent(SingleAddActivity.this, BookEditActivity.class);
                i.putExtra(BookEditActivity.BOOK, mBook);
                i.putExtra(BookEditActivity.downloadCover, false);
                startActivity(i);
                finish();
                break;


        }
        return super.onOptionsItemSelected(item);
    }

    private void addBook(final String isbn) {
        mScannerView.stopCamera();
        BookLab bookLab = BookLab.get(this);
        List<Book> mBooks = bookLab.getBooks();
        boolean isExist = false;
        for (Book book : mBooks) {
            if (book.getIsbn().equals(isbn)) {
                isExist = true;
                break;
            }
        }

        if (isExist) {//The book is already in the list
            DialogHelper.show(this,
                    R.string.book_duplicate_dialog_title,
                    R.string.book_duplicate_dialog_content,
                    R.string.book_duplicate_dialog_positive,
                    android.R.string.cancel,
                    () -> beginFetcher(isbn),
                    () -> finish());
        } else {
            beginFetcher(isbn);
        }
    }

    private void beginFetcher(String isbn) {
        indexOfServiceTested = 0;
        String rawWS = PreferenceManager.getDefaultSharedPreferences(this).getString("webServices", null);
        if (rawWS != null) {
            Type type = new TypeToken<Integer[]>() {
            }.getType();
            Gson gson = new Gson();
            selectedServices = gson.fromJson(rawWS, type);
        } else {
            selectedServices = new Integer[]{0, 1, 2}; //three webServices
        }

        if (selectedServices[indexOfServiceTested] == 0) {
            DoubanFetcher fetcher = new DoubanFetcher();
            fetcher.getBookInfo(this, isbn, 0);
        } else if (selectedServices[indexOfServiceTested] == 1) {
            OpenLibraryFetcher fetcher = new OpenLibraryFetcher();
            fetcher.getBookInfo(this, isbn, 0);
        } else if (selectedServices[indexOfServiceTested] == 2) {
            GoogleBooksFetcher fetcher = new GoogleBooksFetcher();
            fetcher.getBookInfo(this, isbn, 0);
        }
    }


    @Override
    public void handleResult(Result rawResult) {
        Log.i(TAG, "ScanResult Contents = " + rawResult.getText() + ", Format = " + rawResult.getBarcodeFormat().toString());
        addBook(rawResult.getText());
    }

    public void resumeCamera() {
        mScannerView.setResultHandler(this);
        mScannerView.setAutoFocus(true);
        mScannerView.setFlash(mFlash);
        mScannerView.startCamera();
    }


    public void fetchSucceed(final Book mBook, final String imageURL) {
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {//on the main thread
            @Override
            public void run() {
                Intent i = new Intent(SingleAddActivity.this, BookEditActivity.class);
                i.putExtra(BookEditActivity.BOOK, mBook);
                if(imageURL!=null){
                    i.putExtra(BookEditActivity.downloadCover, true);
                    i.putExtra(BookEditActivity.imageURL, imageURL);
                }else{
                    i.putExtra(BookEditActivity.downloadCover, false);
                    mBook.setHasCover(false);
                }
                startActivity(i);
                finish();
            }
        });

    }

    public void fetchFailed(int fetcherID, int event, String isbn) {
        /**
         * event = 0, unexpected response code
         * event = 1, request failed
         */
        // Activity may have been destroyed while waiting for network response
        if (isFinishing() || isDestroyed()) return;
        indexOfServiceTested += 1;
        if (indexOfServiceTested < selectedServices.length) {
            // test next
            if (selectedServices[indexOfServiceTested] == 0) {
                DoubanFetcher fetcher = new DoubanFetcher();
                fetcher.getBookInfo(this, isbn, 0);
            } else if (selectedServices[indexOfServiceTested] == 1) {
                OpenLibraryFetcher fetcher = new OpenLibraryFetcher();
                fetcher.getBookInfo(this, isbn, 0);
            } else if (selectedServices[indexOfServiceTested] == 2) {
                GoogleBooksFetcher fetcher = new GoogleBooksFetcher();
                fetcher.getBookInfo(this, isbn, 0);
            }
        } else {
            if (event == 0) {
                event0Dialog(isbn);
            } else if (event == 1) {
                event1Dialog(isbn);
            }
        }


    }

    private void event0Dialog(final String isbn) {
        String dialogContent = String.format(getResources().getString(
                R.string.isbn_unmatched_dialog_content), isbn);
        com.afollestad.materialdialogs.callbacks.DialogCallbackExtKt.onDismiss(
                new MaterialDialog(this, com.afollestad.materialdialogs.ModalDialog.INSTANCE)
                        .title(R.string.isbn_unmatched_dialog_title, null)
                        .message(null, dialogContent, null)
                        .positiveButton(R.string.isbn_unmatched_dialog_positive, null, d -> {
                            Book mBook = new Book();
                            mBook.setIsbn(isbn);
                            Intent i = new Intent(SingleAddActivity.this, BookEditActivity.class);
                            i.putExtra(BookEditActivity.BOOK, mBook);
                            i.putExtra(BookEditActivity.downloadCover, false);
                            startActivity(i);
                            finish();
                            return kotlin.Unit.INSTANCE;
                        })
                        .negativeButton(R.string.isbn_unmatched_dialog_negative, null, d -> {
                            resumeCamera();
                            return kotlin.Unit.INSTANCE;
                        }),
                d -> {
                    resumeCamera();
                    return kotlin.Unit.INSTANCE;
                }).show();

    }

    private void event1Dialog(final String isbn) {
        String dialogContent = String.format(getResources().getString(
                R.string.request_failed_dialog_content), isbn);
        com.afollestad.materialdialogs.callbacks.DialogCallbackExtKt.onDismiss(
                new MaterialDialog(this, com.afollestad.materialdialogs.ModalDialog.INSTANCE)
                        .title(R.string.isbn_unmatched_dialog_title, null)
                        .message(null, dialogContent, null)
                        .positiveButton(R.string.isbn_unmatched_dialog_positive, null, d -> {
                            Book mBook = new Book();
                            mBook.setIsbn(isbn);
                            Intent i = new Intent(SingleAddActivity.this, BookEditActivity.class);
                            i.putExtra(BookEditActivity.BOOK, mBook);
                            i.putExtra(BookEditActivity.downloadCover, false);
                            startActivity(i);
                            finish();
                            return kotlin.Unit.INSTANCE;
                        })
                        .negativeButton(R.string.isbn_unmatched_dialog_negative, null, d -> {
                            resumeCamera();
                            return kotlin.Unit.INSTANCE;
                        }),
                d -> {
                    resumeCamera();
                    return kotlin.Unit.INSTANCE;
                }).show();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Camera Permission Denied");
                    finish();
                }
        }
    }


}
