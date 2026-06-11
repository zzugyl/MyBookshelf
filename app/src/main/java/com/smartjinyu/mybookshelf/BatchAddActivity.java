package com.smartjinyu.mybookshelf;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.list.DialogListExtKt;
import com.afollestad.materialdialogs.list.DialogMultiChoiceExtKt;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


/**
 * Batch add books activity
 * Created by smartjinyu on 2017/2/8.
 */

public class BatchAddActivity extends AppCompatActivity {
    private static final String TAG = "BatchAddActivity";
    private static final int CAMERA_PERMISSION = 1;
    public static TabLayout tabLayout;

    FragmentStateAdapter adapter;

    public static Integer[] selectedServices;
    public static int indexOfServiceTested;
    // the index of service in selectedServices has been tested.
    // Initially is -1,when the 0st one is tested, it is 0.
    // Caution it is selectedServices[x], instead of the id of webServices itself.


    public static List<Book> mBooks;// books added

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_add);

        mBooks = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }


        ViewPager2 viewPager = findViewById(R.id.batch_add_view_pager);
        adapter = new PagerAdapter();
        viewPager.setAdapter(adapter);
        tabLayout = findViewById(R.id.batch_add_tab_layout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(getString(R.string.batch_add_tab_title_0));
            } else {
                tab.setText(String.format(getString(R.string.batch_add_tab_title_1), mBooks.size()));
            }
        }).attach();

        Toolbar mToolbar = (Toolbar) findViewById(R.id.batch_add_toolbar);
        mToolbar.setTitle(R.string.batch_add_title);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_close);
        mToolbar.setNavigationContentDescription(R.string.batch_add_navigation_close);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBeforeDiscard();
            }
        });

        String rawWS = PreferenceManager.getDefaultSharedPreferences(this).getString("webServices", null);
        if (rawWS != null) {
            Type type = new TypeToken<Integer[]>() {
            }.getType();
            Gson gson = new Gson();
            selectedServices = gson.fromJson(rawWS, type);
        } else {
            selectedServices = new Integer[]{0, 1, 2}; //three webServices
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                dialogBeforeDiscard();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_batchadd, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.batch_add_menu_item_save:
                // choose bookshelf
                if (mBooks.size() != 0) {
                    chooseBookshelf();
                } else {
                    finish();
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void chooseBookshelf() {

        final BookShelfLab bookShelfLab = BookShelfLab.get(BatchAddActivity.this);
        final List<BookShelf> bookShelves = bookShelfLab.getBookShelves();
        final java.util.ArrayList<CharSequence> bookShelfNames = new java.util.ArrayList<>();
        for (BookShelf bs : bookShelves) bookShelfNames.add(bs.toString());

        MaterialDialog bsDialog = new MaterialDialog(BatchAddActivity.this, com.afollestad.materialdialogs.ModalDialog.INSTANCE);
        bsDialog.title(R.string.move_to_dialog_title, null);
        DialogListExtKt.listItems(bsDialog, null, bookShelfNames, null, false, (dialog, position, text) -> {
                    List<BookShelf> allShelves = bookShelfLab.getBookShelves();
                    for (BookShelf bookShelf : allShelves) {
                        if (bookShelf.toString().equals(text.toString())) {
                            for (Book book : mBooks) {
                                book.setBookshelfID(bookShelf.getId());
                            }
                            break;
                        }
                    }
                    dialog.dismiss();
                    addLabel();
                    return kotlin.Unit.INSTANCE;
                });
        bsDialog.neutralButton(R.string.move_to_dialog_neutral, null, listdialog -> {
                    DialogHelper.showInput(BatchAddActivity.this,
                            R.string.custom_book_shelf_dialog_title,
                            R.string.custom_book_shelf_dialog_edit_text, null,
                            getResources().getInteger(R.integer.bookshelf_name_max_length),
                            android.R.string.ok,
                            android.R.string.cancel,
                            (inputDialog, input) -> {
                                BookShelf bookShelfToAdd = new BookShelf();
                                bookShelfToAdd.setTitle(input);
                                bookShelfLab.addBookShelf(bookShelfToAdd);
                                Log.i(TAG, "New bookshelf created " + bookShelfToAdd.getTitle());
                                bookShelfNames.add(bookShelfToAdd.toString());
                                DialogListExtKt.updateListItems(listdialog, null, bookShelfNames, null, null);
                            },
                            null);
                    return kotlin.Unit.INSTANCE;
                });
        bsDialog.noAutoDismiss();
        bsDialog.show();

    }

    private void addLabel() {
        final LabelLab labelLab = LabelLab.get(BatchAddActivity.this);
        final List<Label> labels = labelLab.getLabels();
        final java.util.ArrayList<CharSequence> labelNames = new java.util.ArrayList<>();
        for (Label lb : labels) labelNames.add(lb.getTitle());

        MaterialDialog addLabelDialog = new MaterialDialog(BatchAddActivity.this, com.afollestad.materialdialogs.ModalDialog.INSTANCE);
        addLabelDialog.title(R.string.add_label_dialog_title, null);
        DialogMultiChoiceExtKt.listItemsMultiChoice(addLabelDialog, null, labelNames, null, null, true, true, (dialog, indices, texts) -> {
                    List<Label> allLabels = labelLab.getLabels();
                    for (int idx : indices) {
                        for (Label label : allLabels) {
                            if (label.getTitle().equals(labelNames.get(idx).toString())) {
                                for (Book book : mBooks) {
                                    book.addLabel(label);
                                }
                                break;
                            }
                        }
                    }
                    dialog.dismiss();
                    BookLab.get(BatchAddActivity.this).addBooks(mBooks);
                    finish();
                    return kotlin.Unit.INSTANCE;
                });
        addLabelDialog.positiveButton(android.R.string.ok, null, null);
        addLabelDialog.neutralButton(R.string.label_choice_dialog_neutral, null, listDialog -> {
                    DialogHelper.showInput(BatchAddActivity.this,
                            R.string.label_add_new_dialog_title,
                            R.string.label_add_new_dialog_edit_text, null,
                            getResources().getInteger(R.integer.label_name_max_length),
                            android.R.string.ok,
                            android.R.string.cancel,
                            (inputDialog, input) -> {
                                Label labelToAdd = new Label();
                                labelToAdd.setTitle(input);
                                labelLab.addLabel(labelToAdd);
                                Log.i(TAG, "New label created " + labelToAdd.getTitle());
                                labelNames.add(labelToAdd.getTitle());
                                DialogListExtKt.updateListItems(listDialog, null, labelNames, null, null);
                            },
                            null);
                    return kotlin.Unit.INSTANCE;
                });
        addLabelDialog.noAutoDismiss();
        addLabelDialog.show();
    }

    private void setTabTitle() {
        if (tabLayout != null) {
            tabLayout.getTabAt(1).
                    setText(String.format(getString(R.string.batch_add_tab_title_1), mBooks.size()));

        }
    }


    public class PagerAdapter extends FragmentStateAdapter {
        public PagerAdapter() {
            super(BatchAddActivity.this);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new BatchScanFragment();
            } else {
                return new BatchListFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }


    public void fetchSucceed(final Book mBook, final String imageURL) {
        mBooks.add(mBook);
        if (mBook.getWebsite() == null) {
            mBook.setWebsite("");
        }
        if (mBook.getNotes() == null) {
            mBook.setNotes("");
        }
        setTabTitle();
        Snackbar.make(
                findViewById(R.id.batch_add_linear_layout),
                String.format(getString(R.string.batch_add_added_snack_bar), mBook.getTitle()),
                Snackbar.LENGTH_SHORT).show();
        if(imageURL!=null){
            CoverDownloader coverDownloader = new CoverDownloader(this, mBook, 1);
            String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + mBook.getCoverPhotoFileName();
            coverDownloader.downloadAndSaveImg(imageURL, path);
        }else{
            mBook.setHasCover(false);
        }
    }

    public void fetchFailed(int fetcherID, int event, String isbn) {
        /**
         * event = 0, unexpected response code
         * event = 1, request failed
         */

        indexOfServiceTested += 1;
        if (indexOfServiceTested < selectedServices.length) {
            // test next
            if (selectedServices[indexOfServiceTested] == 0) {
                DoubanFetcher fetcher = new DoubanFetcher();
                fetcher.getBookInfo(this, isbn, 1);
            } else if (selectedServices[indexOfServiceTested] == 1) {
                OpenLibraryFetcher fetcher = new OpenLibraryFetcher();
                fetcher.getBookInfo(this, isbn, 1);
            } else if (selectedServices[indexOfServiceTested] == 2) {
                GoogleBooksFetcher fetcher = new GoogleBooksFetcher();
                fetcher.getBookInfo(this, isbn, 1);
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
                R.string.isbn_unmatched_dialog_batch_content), isbn);
        DialogHelper.show(this,
                getString(R.string.isbn_unmatched_dialog_title),
                dialogContent,
                getString(R.string.isbn_unmatched_dialog_negative),
                getString(android.R.string.cancel),
                null, null);
    }

    private void event1Dialog(final String isbn) {
        String dialogContent = String.format(getResources().getString(
                R.string.request_failed_dialog_batch_content), isbn);
        DialogHelper.show(this,
                getString(R.string.isbn_unmatched_dialog_title),
                dialogContent,
                getString(R.string.isbn_unmatched_dialog_negative),
                getString(android.R.string.cancel),
                null, null);
    }

    private void dialogBeforeDiscard() {
        if (mBooks.size() != 0) {
            DialogHelper.show(this,
                    R.string.batch_add_activity_discard_dialog_title,
                    R.string.batch_add_activity_discard_dialog_content,
                    R.string.batch_add_activity_discard_dialog_positive,
                    android.R.string.cancel,
                    () -> finish(),
                    null);
        } else {
            finish();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] results) {
        switch (requestCode) {
            case CAMERA_PERMISSION:
                if (!(results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Camera Permission Denied");
                    finish();
                }
        }
    }


}

