package com.smartjinyu.mybookshelf;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.widget.AccountHeaderView;
import com.mikepenz.materialdrawer.util.MaterialDrawerSliderViewExtensionsKt;
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String drawerSelected = "drawerSelected";
    private static final String SORT_METHOD = "SORT_METHOD";

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private MaterialDrawerSliderView mSlider;
    private AccountHeaderView mAccountHeader;
    private Spinner mSpinner;
    private RecyclerView mRecyclerView;
    private FloatingActionMenu mActionAddButton;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private MenuItem searchItem;
    private SearchView searchView;
    private CoordinatorLayout mCoordinatorLayout;

    private BookAdapter mRecyclerViewAdapter;
    private ActionMode mActionMode;

    private boolean isMultiSelect = false;
    private List<Book> multiSelectList = new ArrayList<>();
    private List<Book> UndoBooks = new ArrayList<>();// used to undo deleting
    private List<Book> mBooks;
    private boolean showBookshelfMenuItem = false;
    private boolean showLabelMenuItem = false;
    private int sortMethod;
    private SharedPreferences defaultSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sortMethod = defaultSharedPreferences.getInt(SORT_METHOD, 0);

        mCoordinatorLayout = findViewById(R.id.book_list_fragment_coordinator_layout);
        checkTermOfService();
        setRecyclerView();
        setFloatingActionButton();
        setToolbar();
        setBookShelfSpinner(0);
        long drawerSelection = -1;
        if (savedInstanceState != null) {
            drawerSelection = savedInstanceState.getLong(drawerSelected, -1);
        }
        setDrawer(drawerSelection);

        if(defaultSharedPreferences.getBoolean("settings_pref_check_update",true)){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new UpdateCheck(MainActivity.this);
                }
            }, 3000);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    if (defaultSharedPreferences != null) {
                        int startTimes = defaultSharedPreferences.getInt("launchTimes", 1);
                        Log.i(TAG, "startTimes = " + startTimes);
                        defaultSharedPreferences.edit().putInt("launchTimes", startTimes + 1).apply();
                        boolean muteRatings = defaultSharedPreferences.getBoolean("muteRatings", false);
                        boolean isRated = defaultSharedPreferences.getBoolean("isRated", false);
                        Log.i(TAG, "rating info muteRatings = " + muteRatings + ", isRated = " + isRated);
                        if (!muteRatings &&
                                !isRated &&
                                startTimes % getResources().getInteger(R.integer.rating_after_start_times) == 0 &&
                                mBooks.size() > getResources().getInteger(R.integer.rating_if_books_more_than)) {
                            showRatingDialog();
                        } else {
                            finish();
                        }
                    } else {
                        finish();
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu()");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_main_search);
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Log.d(TAG, "searchItem expand");
                if (mActionAddButton != null) {
                    Log.d(TAG, "Hide FAM 2");
                    mActionAddButton.setVisibility(View.GONE);
                    mActionAddButton.hideMenuButton(true);
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Log.d(TAG, "searchItem collapse");
                if (mActionAddButton != null) {
                    Log.d(TAG, "Show FAM 2");
                    mActionAddButton.setVisibility(View.VISIBLE);
                    mActionAddButton.showMenuButton(true);
                }
                updateUI(true, null);
                return true;
            }
        });
        searchView = (SearchView) searchItem.getActionView();
        searchView.setIconifiedByDefault(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG, "Search " + query);
                updateUI(true, query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                updateUI(true, newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.i(TAG, "onPrepareOptionsMenu showLabelItem = " + showLabelMenuItem + ", showBookshelfItem = " + showBookshelfMenuItem);
        MenuItem renameLabelItem = menu.findItem(R.id.menu_main_rename_label);
        MenuItem deleteLabelItem = menu.findItem(R.id.menu_main_delete_label);
        MenuItem renameBookshelfItem = menu.findItem(R.id.menu_main_rename_bookshelf);
        MenuItem deleteBookshelfItem = menu.findItem(R.id.menu_main_delete_bookshelf);
        searchItem = menu.findItem(R.id.menu_main_search);


        renameLabelItem.setVisible(showLabelMenuItem);
        deleteLabelItem.setVisible(showLabelMenuItem);
        searchItem.setVisible(!showLabelMenuItem);// no search icon in label page

        renameBookshelfItem.setVisible(showBookshelfMenuItem);
        deleteBookshelfItem.setVisible(showBookshelfMenuItem);
        if (showLabelMenuItem) {
            Log.d(TAG, "Hide FAM 1");
            mActionAddButton.setVisibility(View.GONE);
            mActionAddButton.hideMenuButton(true);
        } else {
            Log.d(TAG, "Show FAM 1");
            mActionAddButton.setVisibility(View.VISIBLE);
            mActionAddButton.showMenuButton(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_rename_bookshelf:
                if (mSpinner != null) {
                    final BookShelf selectedBS = (BookShelf) mSpinner.getSelectedItem();
                    if (!selectedBS.getTitle().equals(getString(R.string.spinner_all_bookshelf))) {
                        // make sure the bookshelf to rename is valid
                        new MaterialDialog.Builder(this)
                                .title(R.string.rename_bookshelf_dialog_title)
                                .input(
                                        getString(R.string.rename_bookshelf_dialog_edit_text),
                                        selectedBS.getTitle(),
                                        new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                // nothing to do here
                                            }
                                        })
                                .positiveText(android.R.string.ok)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        String newName = dialog.getInputEditText().getText().toString();
                                        BookShelfLab.get(MainActivity.this).renameBookShelf(selectedBS.getId(), newName);
                                        setBookShelfSpinner(mSpinner.getSelectedItemPosition());
                                    }
                                })
                                .negativeText(android.R.string.cancel)
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }
                }
                break;
            case R.id.menu_main_delete_bookshelf:
                if (mSpinner != null) {
                    final BookShelf selectedBS = (BookShelf) mSpinner.getSelectedItem();
                    if (!selectedBS.getTitle().equals(getString(R.string.spinner_all_bookshelf))) {
                        // make sure the bookshelf to rename is valid
                        if (mBooks.size() == 0) {
                            // no books here, no need to popup a dialog
                            BookShelfLab.get(MainActivity.this).deleteBookShelf(selectedBS.getId(), false);
                            setBookShelfSpinner(0);
                        } else {
                            new MaterialDialog.Builder(this)
                                    .title(R.string.delete_bookshelf_dialog_title)
                                    .content(R.string.delete_bookshelf_dialog_content)
                                    .positiveText(R.string.delete_bookshelf_dialog_positive)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            BookShelfLab.get(MainActivity.this).deleteBookShelf(selectedBS.getId(), true);
                                            setBookShelfSpinner(0);
                                            updateUI(true, null);
                                            setBookShelfSpinner(0);
                                        }
                                    })
                                    .negativeText(android.R.string.cancel)
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        }
                    }
                }
                break;
            case R.id.menu_main_rename_label:
                if (mSlider != null) {
                    long drawerSelection = getDrawerSelection();
                    List<Label> labels = LabelLab.get(MainActivity.this).getLabels();
                    if (drawerSelection >= 10 && drawerSelection < 10 + labels.size()) {
                        // make sure the selection label is valid
                        final Label selectedLB = labels.get((int) drawerSelection - 10);
                        new MaterialDialog.Builder(this)
                                .title(R.string.rename_label_dialog_title)
                                .input(
                                        getString(R.string.rename_label_dialog_edit_text),
                                        selectedLB.getTitle(),
                                        new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                // nothing to do here
                                            }
                                        })
                                .positiveText(android.R.string.ok)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        String newName = dialog.getInputEditText().getText().toString();
                                        LabelLab.get(MainActivity.this).renameLabel(selectedLB.getId(), newName);
                                        setDrawer(getDrawerSelection());
                                    }
                                })
                                .negativeText(android.R.string.cancel)
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();

                    }
                }
                break;
            case R.id.menu_main_delete_label:
                if (mSlider != null) {
                    long drawerSelection = getDrawerSelection();
                    List<Label> labels = LabelLab.get(MainActivity.this).getLabels();
                    if (drawerSelection >= 10 && drawerSelection < 10 + labels.size()) {
                        // make sure the selection label is valid
                        final Label selectedLB = labels.get((int) drawerSelection - 10);
                        if (mBooks.size() == 0) {
                            // no need to popup a dialog
                            LabelLab.get(MainActivity.this).deleteLabel(selectedLB.getId(), false);
                            setDrawer(1);

                        } else {
                            new MaterialDialog.Builder(this)
                                    .title(R.string.delete_label_dialog_title)
                                    .content(R.string.delete_label_dialog_content)
                                    .positiveText(R.string.delete_label_dialog_positive)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            LabelLab.get(MainActivity.this).deleteLabel(selectedLB.getId(), true);
                                            setDrawer(1);
                                        }
                                    })
                                    .negativeText(android.R.string.cancel)
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        }
                    }
                }
                break;
            case R.id.menu_main_sort:
                new MaterialDialog.Builder(this)
                        .title(R.string.sort_choice_dialog_title)
                        .items(R.array.main_sort_dialog)
                        .itemsCallbackSingleChoice(sortMethod, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                sortMethod = which;
                                return true; // return true allow select
                            }
                        })
                        .positiveText(R.string.sort_choice_dialog_positive)
                        .alwaysCallSingleChoiceCallback()
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                updateUI(false, null);
                            }
                        })
                        .show();
                break;
            case R.id.menu_main_search:
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private long getDrawerSelection() {
        if (mSlider == null) return -1;
        FastAdapter<IDrawerItem<?>> adapter = mSlider._adapter;
        for (int i = 0; i < adapter.getItemCount(); i++) {
            IDrawerItem<?> item = adapter.getItem(i);
            if (item != null && item.isSelected()) {
                return item.getIdentifier();
            }
        }
        return -1;
    }

    private void setDrawerSelection(long identifier) {
        if (mSlider == null) return;
        FastAdapter<IDrawerItem<?>> adapter = mSlider._adapter;
        for (int i = 0; i < adapter.getItemCount(); i++) {
            IDrawerItem<?> item = adapter.getItem(i);
            if (item != null && item.getIdentifier() == identifier) {
                item.setSelected(true);
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void setDrawer(long selectionIdentifier) {
        final List<Label> labels = LabelLab.get(this).getLabels();

        // Initialize DrawerLayout reference
        if (mDrawerLayout == null) {
            mDrawerLayout = findViewById(R.id.drawer_layout);
        }

        // Initialize slider reference
        if (mSlider == null) {
            mSlider = findViewById(R.id.slider);
        }

        // Build account header
        final IProfile profile = new ProfileDrawerItem();
        profile.setName(new StringHolder(getResources().getString(R.string.app_name)));
        profile.setIcon(new ImageHolder(R.mipmap.ic_launcher_circle));
        profile.setDescription(new StringHolder(getResources().getString(R.string.drawer_header_email)));

        mAccountHeader = new AccountHeaderView(this, null, 0, false);
        mAccountHeader.addProfiles(profile);
        mAccountHeader.setHeaderBackground(new ImageHolder(R.drawable.header));
        mAccountHeader.setSelectionListEnabledForSingleProfile(false);
        mSlider.setAccountHeader(mAccountHeader);

        // Build drawer items
        List<IDrawerItem<?>> items = new ArrayList<>();

        PrimaryDrawerItem booksItem = new PrimaryDrawerItem();
        booksItem.setName(new StringHolder(getString(R.string.drawer_item_books)));
        booksItem.setIcon(new ImageHolder(R.drawable.ic_bookshelf));
        booksItem.setIdentifier(1);
        booksItem.setSelectable(true);
        items.add(booksItem);

        SectionDrawerItem sectionItem = new SectionDrawerItem();
        sectionItem.setName(new StringHolder(getString(R.string.drawer_section_label)));
        items.add(sectionItem);

        PrimaryDrawerItem createLabelItem = new PrimaryDrawerItem();
        createLabelItem.setName(new StringHolder(getString(R.string.drawer_item_create_new_label)));
        createLabelItem.setIcon(new ImageHolder(R.drawable.ic_add));
        createLabelItem.setIdentifier(3);
        createLabelItem.setSelectable(false);
        items.add(createLabelItem);

        items.add(new DividerDrawerItem());

        PrimaryDrawerItem settingsItem = new PrimaryDrawerItem();
        settingsItem.setName(new StringHolder(getString(R.string.drawer_item_settings)));
        settingsItem.setIcon(new ImageHolder(R.drawable.ic_settings));
        settingsItem.setIdentifier(4);
        settingsItem.setSelectable(false);
        items.add(settingsItem);

        PrimaryDrawerItem aboutItem = new PrimaryDrawerItem();
        aboutItem.setName(new StringHolder(getString(R.string.drawer_item_about)));
        aboutItem.setIcon(new ImageHolder(R.drawable.ic_about));
        aboutItem.setIdentifier(5);
        aboutItem.setSelectable(false);
        items.add(aboutItem);

        // Add label items
        for (int i = 0; i < labels.size(); i++) {
            PrimaryDrawerItem labelItem = new PrimaryDrawerItem();
            labelItem.setName(new StringHolder(labels.get(i).getTitle()));
            labelItem.setIcon(new ImageHolder(R.drawable.ic_label));
            labelItem.setIdentifier(i + 10);
            labelItem.setSelectable(true);
            items.add(labelItem);
        }

        MaterialDrawerSliderViewExtensionsKt.setItems(mSlider, items.toArray(new IDrawerItem[0]));

        // Set click listener
        mSlider.setOnDrawerItemClickListener(new kotlin.jvm.functions.Function3<View, IDrawerItem<?>, Integer, Boolean>() {
            @Override
            public Boolean invoke(View view, IDrawerItem<?> drawerItem, Integer position) {
                        if (drawerItem != null) {
                            Log.i(TAG, "Select drawer item at position " + position);
                            if (mActionMode != null) {
                                mActionMode.finish();
                            }
                            long id = drawerItem.getIdentifier();
                            if (id == 1) {
                                updateUI(true, null);
                                if (mSpinner != null) {
                                    setBookShelfSpinner(mSpinner.getSelectedItemPosition());
                                }
                            } else if (id == 3) {
                                new MaterialDialog.Builder(MainActivity.this)
                                        .title(R.string.label_add_new_dialog_title)
                                        .inputRange(1, getResources().getInteger(R.integer.label_name_max_length))
                                        .input(
                                                R.string.label_add_new_dialog_edit_text,
                                                0,
                                                new MaterialDialog.InputCallback() {
                                                    @Override
                                                    public void onInput(@NonNull MaterialDialog dialog1, CharSequence input) {
                                                        // nothing to do here
                                                    }
                                                })
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog inputDialog, @NonNull DialogAction which) {
                                                Label labelToAdd = new Label();
                                                labelToAdd.setTitle(inputDialog.getInputEditText().getText().toString());
                                                LabelLab.get(MainActivity.this).addLabel(labelToAdd);
                                                Log.i(TAG, "New label created " + labelToAdd.getTitle());
                                                setDrawer(getDrawerSelection());
                                            }
                                        })
                                        .negativeText(android.R.string.cancel)
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog inputDialog, @NonNull DialogAction which) {
                                                inputDialog.dismiss();
                                            }
                                        })
                                        .show();
                            } else if (id >= 10 && id < 10 + labels.size()) {
                                if (searchView != null && !searchView.isIconified()) {
                                    searchView.setIconified(true);
                                }
                                updateUI(true, null);
                                if (mSpinner != null) {
                                    setBookShelfSpinner(mSpinner.getSelectedItemPosition());
                                }
                            } else if (id == 2) {
                                if (searchItem != null) {
                                    searchItem.expandActionView();
                                }
                                setDrawerSelection(1);
                                updateUI(true, null);
                            } else if (id == 4) {
                                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(i);
                            } else if (id == 5) {
                                Intent i = new Intent(MainActivity.this, AboutActivity.class);
                                startActivity(i);
                            }
                        }
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    }
                });

        if (selectionIdentifier != -1) {
            setDrawerSelection(selectionIdentifier);
        }
    }

    private void setToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setFloatingActionButton() {
        mActionAddButton = (FloatingActionMenu) findViewById(R.id.fab_menu_add);
        fab1 = (FloatingActionButton) findViewById(R.id.fab_menu_item_1);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "fab menu item 1 clicked");
                Intent i = new Intent(MainActivity.this, SingleAddActivity.class);
                startActivity(i);
                mActionAddButton.close(true);

            }
        });
        fab2 = (FloatingActionButton) findViewById(R.id.fab_menu_item_2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "fab menu item 2 clicked");
                Intent i = new Intent(MainActivity.this, BatchAddActivity.class);
                startActivity(i);
                mActionAddButton.close(true);
            }
        });
        mActionAddButton.setMenuButtonShowAnimation(AnimationUtils.loadAnimation(this, R.anim.show_from_bottom));
        mActionAddButton.setMenuButtonHideAnimation(AnimationUtils.loadAnimation(this, R.anim.hide_to_bottom));
    }

    private void setRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.booklist_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            // hide/display float action button automatically
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    if (!mActionAddButton.isMenuButtonHidden()) {
                        Log.d(TAG, "Hide FAM 3");
                        mActionAddButton.hideMenuButton(true);
                    }
                } else {
                    if (mActionAddButton.isMenuButtonHidden()) {
                        Log.d(TAG, "Show FAM 3");
                        mActionAddButton.showMenuButton(true);
                    }
                }
            }

        });

        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this, mRecyclerView, new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d(TAG, "Click recyclerView position = " + position);
                if (position != -1) {
                    if (isMultiSelect) {
                        multiSelect(position - 1);
                    } else {
                        if (mActionAddButton.isOpened()) {
                            mActionAddButton.close(true);
                        } else {
                            Log.d(TAG, "Clicked Book's hashcode is " + mBooks.get(position - 1).hashCode());
                            Intent i = new Intent(MainActivity.this, BookDetailActivity.class);
                            i.putExtra(BookDetailActivity.Intent_Book_ToEdit, mBooks.get(position - 1));
                            startActivity(i);
                        }
                    }
                } else {
                    Log.e(TAG, "RecyclerView Position -1");
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!isMultiSelect) {
                    multiSelectList = new ArrayList<>();
                    isMultiSelect = true;
                }
                if (mActionMode == null) {
                    mActionMode = startActionMode(mActionModeCallback);
                }
                multiSelect(position - 1);
            }
        }));

    }

    private class BookHolder extends RecyclerView.ViewHolder {

        private ImageView mCoverImageView;
        private TextView mTitleTextView;
        private TextView mPublisherTextView;
        private TextView mPubtimeTextView;
        private RelativeLayout mRelativeLayout;

        public BookHolder(View itemView) {
            super(itemView);
            mCoverImageView = (ImageView) itemView.findViewById(R.id.list_cover_image_view);
            mTitleTextView = (TextView) itemView.findViewById(R.id.list_title_text_view);
            mPublisherTextView = (TextView) itemView.findViewById(R.id.list_publisher_text_view);
            mPubtimeTextView = (TextView) itemView.findViewById(R.id.list_pubtime_text_view);
            mRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.list_item_relative_layout);
        }

        public void bindBook(Book book) {
            mTitleTextView.setText(book.getTitle());

            StringBuilder authorAndPub = new StringBuilder();
            String authors = book.getFormatAuthor();
            if(authors!=null){
                authorAndPub.append(authors);
            }

            if (book.getPublisher().length() != 0) {
                if (authorAndPub.length() != 0) {
                    authorAndPub.append(" ");
                    authorAndPub.append(",   ");
                }
                authorAndPub.append(book.getPublisher());
            }
            mPublisherTextView.setText(authorAndPub);
            Calendar calendar = book.getPubTime();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            StringBuilder pubDate = new StringBuilder();
            if (year == 9999) {
                pubDate.append(getResources().getString(R.string.pubdate_unset));
            } else {
                pubDate.append(year);
                pubDate.append("-");
                if (month < 9) {
                    pubDate.append("0");
                }
                pubDate.append(month + 1);
            }

            mPubtimeTextView.setText(pubDate);
            if (multiSelectList.contains(book)) {//set select
                mRelativeLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.recycler_item_selected));
                mCoverImageView.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_check_circle));
            } else {//back to normal
                mRelativeLayout.setBackgroundColor(Color.WHITE);
                if (book.isHasCover()) {
                    String path =
                            getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + book.getCoverPhotoFileName();
                    Bitmap src = BitmapFactory.decodeFile(path);
                    mCoverImageView.setImageBitmap(src);
                }
            }

        }

    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.recyclerview_header_text);
        }

        public void setCount(int count) {
            mTextView.setText(String.valueOf(count));
        }
    }


    private class BookAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        // implement header
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;

        BookAdapter(List<Book> books) {
            mBooks = books;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
            if (viewType == TYPE_ITEM) {
                View view = layoutInflater.inflate(R.layout.item_booklist_recyclerview, parent, false);
                return new BookHolder(view);
            } else if (viewType == TYPE_HEADER) {
                View view = layoutInflater.inflate(R.layout.header_booklist_recyclerview, parent, false);
                return new HeaderViewHolder(view);
            }
            throw new RuntimeException("no type  matches the type " + viewType + " + make sure your using types correctly");

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof BookHolder) {
                Book book = mBooks.get(position - 1); // remember to -1 because of the header
                ((BookHolder) holder).bindBook(book);
            } else if (holder instanceof HeaderViewHolder) {
                // set header
                ((HeaderViewHolder) holder).setCount(mBooks.size());
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_HEADER;
            } else {
                return TYPE_ITEM;
            }
        }

        @Override
        public int getItemCount() {
            return mBooks.size() + 1;
        }

    }


    /**
     * setBookShelfSpinner
     *
     * @param selection default selection position
     */
    private void setBookShelfSpinner(int selection) {
        mSpinner = findViewById(R.id.toolbar_spinner);
        if(mSpinner == null){
            // for example, if searchView is expanded, mSpinner is null
            return;
        }
        List<BookShelf> bookShelves = BookShelfLab.get(this).getBookShelves();
        BookShelf allBookShelf = new BookShelf();
        allBookShelf.setTitle(getResources().getString(R.string.spinner_all_bookshelf)); // never save to disk
        int totalBooks = 0;
        for(BookShelf bookShelf : bookShelves){
            totalBooks += bookShelf.getCnt();
        }
        allBookShelf.setCnt(totalBooks);
        bookShelves.add(0, allBookShelf);
        ArrayAdapter<BookShelf> arrayAdapter = new ArrayAdapter<>(
                this, R.layout.spinner_item_white, bookShelves);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_drop_down_white);
        mSpinner.setAdapter(arrayAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateUI(true, null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (selection >= 0 && selection < bookShelves.size()) {
            mSpinner.setSelection(selection);
        }
    }

    /**
     * change Toolbar and status bar color
     *
     * @param mode 0 represents colorPrimary\colorPrimaryDark, 1 represents selected|selectedDark
     */
    private void setToolbarColor(int mode) {
        int colorPrimaryRes, colorPrimaryDarkRes;
        if (mode == 1) {
            colorPrimaryRes = ContextCompat.getColor(this, R.color.selected_primary);
            colorPrimaryDarkRes = ContextCompat.getColor(this, R.color.selected_primary_dark);
        } else {
            colorPrimaryRes = ContextCompat.getColor(this, R.color.colorPrimary);
            colorPrimaryDarkRes = ContextCompat.getColor(this, R.color.colorPrimaryDark);

        }
        mToolbar.setBackgroundColor(colorPrimaryRes);
        getWindow().setStatusBarColor(colorPrimaryDarkRes);
    }

    private void setBooksAndUI(@Nullable String keyword) {
        BookLab bookLab = BookLab.get(this);
        List<Label> labels = LabelLab.get(this).getLabels();
        UUID bookshelfID = null, labelID = null;
        int toolbarMode = 0;
        if (mSpinner != null) {
            BookShelf selectedBookShelf = (BookShelf) mSpinner.getSelectedItem();
            if (selectedBookShelf.getTitle().equals(getString(R.string.spinner_all_bookshelf))) {
                // select "All"
                showBookshelfMenuItem = false;
            } else {
                // select one Bookshelf
                toolbarMode = 1;
                bookshelfID = selectedBookShelf.getId();
                showBookshelfMenuItem = true;
            }
        }

        if (mSlider != null) {
            long drawerSelection = getDrawerSelection();
            if (drawerSelection < 10 || drawerSelection >= 10 + labels.size()) {
                // not select label
                showLabelMenuItem = false;
            } else {
                // select one label
                toolbarMode = 1;
                labelID = labels.get((int) drawerSelection - 10).getId();
                showLabelMenuItem = true;
            }
        }

        if(searchView != null && (!searchView.isIconified() || searchItem.isActionViewExpanded())){
            // in search mode, only support search in specified bookshelf currently
            mBooks = bookLab.searchBook(keyword, bookshelfID);
        }else{
            Log.d(TAG, "setBooksAndUI(), not in search mode");
            invalidateOptionsMenu();
            mBooks = bookLab.getBooks(bookshelfID, labelID);
        }
        BookShelfLab.get(this).calculateBookCnt(bookLab.getBooks(null, labelID));
        setToolbarColor(toolbarMode);
        // invalidateOptionsMenu();// call onPrepareOptionsMenu()

    }

    private void sortBooks() {
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit()
                .putInt(SORT_METHOD, sortMethod).apply();
    }

    /**
     * @param updateBooksList whether to retrieve a new List<Book> mBooks
     */
    private void updateUI(boolean updateBooksList, @Nullable String keyword) {
        if (updateBooksList) {
            setBooksAndUI(keyword);
        }
        sortBooks();
        if (mRecyclerViewAdapter == null) {
            mRecyclerViewAdapter = new BookAdapter(mBooks);
            mRecyclerView.setAdapter(mRecyclerViewAdapter);
        } else {
            mRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(searchView == null){
            Log.d(TAG, "onResume, SearchView is null");
        }else{
            Log.d(TAG, "onResume, SearchView open = " + !searchView.isIconified());
        }

        if (mSlider != null) {
            setDrawer(getDrawerSelection());
        }

        if(searchView != null && !searchView.isIconified()){
            // searchView open
            updateUI(true, searchView.getQuery().toString());
        }else{
            updateUI(true,null);
        }

        if (mSpinner != null) {
            // user may create new bookshelf in edit or creating new book
            setBookShelfSpinner(mSpinner.getSelectedItemPosition());
        }


    }

    private void multiSelect(int position) {
        //Add/Remove items from list
        if (mActionMode != null) {
            int index = multiSelectList.indexOf(mBooks.get(position));
            Log.d(TAG, "Select in List Position " + index);
            if (index == -1) {//not in the list
                multiSelectList.add(mBooks.get(position));
            } else {
                multiSelectList.remove(index);
            }
            if (multiSelectList.size() > 0) {
                String title = getResources().getQuantityString(R.plurals.multi_title, multiSelectList.size(), multiSelectList.size());
                mActionMode.setTitle(title);
            } else {
                mActionMode.finish();

            }
            mRecyclerViewAdapter.notifyDataSetChanged();
        }

    }

    private boolean showFAM = true;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate a menu resource providing contextual menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_multiselect, menu);
            Log.d(TAG, "Hide FAM 4");
            mActionAddButton.setVisibility(View.GONE);
            mActionAddButton.hideMenuButton(true);
            showFAM = true;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_multi_select_select_all:
                    multiSelectList = mBooks;
                    String title = getResources().
                            getQuantityString(R.plurals.multi_title, multiSelectList.size(), multiSelectList.size());
                    mActionMode.setTitle(title);
                    mRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case R.id.menu_multi_select_delete:
                    if (multiSelectList.size() != 0) {
                        final BookLab bookLab = BookLab.get(MainActivity.this);
                        UndoBooks = new ArrayList<>();
                        for (Book book : multiSelectList) {
                            bookLab.deleteBook(book, false);
                            UndoBooks.add(book);
                        }
                        Snackbar snackbar;
                        if (UndoBooks.size() == 1) {
                            snackbar = Snackbar.make(
                                    mCoordinatorLayout,
                                    R.string.book_deleted_snack_bar_0,
                                    Snackbar.LENGTH_SHORT);
                        } else {
                            snackbar = Snackbar.make(
                                    mCoordinatorLayout,
                                    R.string.book_deleted_snack_bar_1,
                                    Snackbar.LENGTH_SHORT);
                        }
                        snackbar.setAction(R.string.book_deleted_snack_bar_undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                for (Book book : UndoBooks) {
                                    bookLab.addBook(book);
                                }
                                UndoBooks = new ArrayList<>();
                                updateUI(true, null);
                                if(mSpinner != null) setBookShelfSpinner(mSpinner.getSelectedItemPosition());
                            }
                        });
                        snackbar.addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                Log.d(TAG, "Show FAM 4");
                                mActionAddButton.setVisibility(View.VISIBLE);
                                mActionAddButton.showMenuButton(true);
                                for (Book book : UndoBooks) {
                                    bookLab.deleteBook(book, true);
                                }

                            }
                        });
                        showFAM = false;
                        // for that the FAM won't move up when a snackbar shows, just hide it currently
                        updateUI(true, null);
                        snackbar.show();
                        mActionMode.finish();
                        if(mSpinner != null) setBookShelfSpinner(mSpinner.getSelectedItemPosition());
                    }
                    break;
                case R.id.menu_multi_select_add_label:
                    final LabelLab labelLab = LabelLab.get(MainActivity.this);
                    final List<Label> labels = labelLab.getLabels();
                    new MaterialDialog.Builder(MainActivity.this)
                            .title(R.string.add_label_dialog_title)
                            .items(labels)
                            .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                                @Override
                                public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                    List<Label> labels = labelLab.getLabels();
                                    // must refresh labels here because if user add label, the list won't update,
                                    // and select the newly add label won't take effect
                                    for (int i = 0; i < which.length; i++) {
                                        for (Label label : labels) {
                                            if (label.getTitle().equals(text[i])) {
                                                // selected label
                                                for (Book book : multiSelectList) {
                                                    book.addLabel(label);
                                                    BookLab.get(MainActivity.this).updateBook(book, false);
                                                }
                                                break;
                                            }
                                        }
                                    }
                                    if (mActionMode != null) {
                                        mActionMode.finish();
                                    }
                                    if (mSlider != null) {
                                        setDrawer(getDrawerSelection());
                                    }
                                    updateUI(true, null);
                                    dialog.dismiss();
                                    return true;
                                }
                            })
                            .neutralText(R.string.label_choice_dialog_neutral)
                            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull final MaterialDialog listDialog, @NonNull DialogAction which) {
                                    // create new label
                                    new MaterialDialog.Builder(MainActivity.this)
                                            .title(R.string.label_add_new_dialog_title)
                                            .inputRange(1, getResources().getInteger(R.integer.label_name_max_length))
                                            .input(
                                                    R.string.label_add_new_dialog_edit_text,
                                                    0,
                                                    new MaterialDialog.InputCallback() {
                                                        @Override
                                                        public void onInput(@NonNull MaterialDialog dialog1, CharSequence input) {
                                                            // nothing to do here
                                                        }
                                                    })
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog inputDialog, @NonNull DialogAction which) {
                                                    Label labelToAdd = new Label();
                                                    labelToAdd.setTitle(inputDialog.getInputEditText().getText().toString());
                                                    labelLab.addLabel(labelToAdd);
                                                    Log.i(TAG, "New label created " + labelToAdd.getTitle());
                                                    listDialog.getItems().add(labelToAdd.getTitle());
                                                    listDialog.notifyItemInserted(listDialog.getItems().size() - 1);
                                                }
                                            })
                                            .negativeText(android.R.string.cancel)
                                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog inputDialog, @NonNull DialogAction which) {
                                                    inputDialog.dismiss();
                                                }
                                            })
                                            .show();
                                }
                            })
                            .positiveText(android.R.string.ok)
                            .autoDismiss(false)
                            .show();
                    break;
                case R.id.menu_multi_select_move_to:
                    final BookShelfLab bookShelfLab = BookShelfLab.get(MainActivity.this);
                    final List<BookShelf> bookShelves = bookShelfLab.getBookShelves();
                    new MaterialDialog.Builder(MainActivity.this)
                            .title(R.string.move_to_dialog_title)
                            .items(bookShelves)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                    List<BookShelf> bookShelves = bookShelfLab.getBookShelves();
                                    for (BookShelf bookShelf : bookShelves) {
                                        if (bookShelf.toString().contentEquals(text)) {
                                            // selected bookshelf
                                            Log.d(TAG, "bookshelf title = " + bookShelf.getTitle());
                                            for (Book book : multiSelectList) {
                                                book.setBookshelfID(bookShelf.getId());
                                            }
                                            BookLab.get(MainActivity.this).updateBooks(multiSelectList);
                                            break;
                                        }
                                    }

                                    if (mActionMode != null) {
                                        mActionMode.finish();
                                    }
                                    updateUI(true, null);
                                    if (mSpinner != null) {
                                        setBookShelfSpinner(mSpinner.getSelectedItemPosition());
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .positiveText(android.R.string.cancel)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .neutralText(R.string.move_to_dialog_neutral)
                            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull final MaterialDialog listdialog, @NonNull DialogAction which) {
                                    // create new bookshelf
                                    new MaterialDialog.Builder(MainActivity.this)
                                            .title(R.string.custom_book_shelf_dialog_title)
                                            .inputRange(1,
                                                    getResources().getInteger(R.integer.bookshelf_name_max_length))
                                            .input(
                                                    R.string.custom_book_shelf_dialog_edit_text,
                                                    0,
                                                    new MaterialDialog.InputCallback() {
                                                        @Override
                                                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                            // nothing to do here
                                                        }
                                                    })
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    BookShelf bookShelfToAdd = new BookShelf();
                                                    bookShelfToAdd.setTitle(dialog.getInputEditText().getText().toString());
                                                    bookShelfLab.addBookShelf(bookShelfToAdd);
                                                    Log.i(TAG, "New bookshelf created " + bookShelfToAdd.getTitle());
                                                    listdialog.getItems().add(bookShelfToAdd.toString());
                                                    listdialog.notifyItemInserted(listdialog.getItems().size() - 1);
                                                }
                                            })
                                            .negativeText(android.R.string.cancel)
                                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    if (mSpinner != null) {
                                                        setBookShelfSpinner(mSpinner.getSelectedItemPosition());
                                                    }
                                                    dialog.dismiss();
                                                }
                                            })
                                            .show();

                                }
                            })
                            .autoDismiss(false)
                            // if autoDismiss = false, the list dialog will dismiss when a new bookshelf is added
                            .show();

                    break;
                case R.id.menu_multi_select_set_reading_status:
                    int initialReadingStatus = multiSelectList.get(0).getReadingStatus();
                    if(initialReadingStatus > 0){
                        initialReadingStatus--;
                    }
                    new MaterialDialog.Builder(MainActivity.this)
                            .title(R.string.set_reading_status_title)
                            .items(R.array.reading_status_array_no_unset)
                            .itemsCallbackSingleChoice(initialReadingStatus, new MaterialDialog.ListCallbackSingleChoice() {
                                @Override
                                public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                    Log.i(TAG,"Set multi reading status = " + which);
                                    for(Book book: multiSelectList){
                                        book.setReadingStatus(which + 1);
                                        BookLab.get(MainActivity.this).updateBook(book, false);
                                        // must call this to update the database
                                    }
                                    updateUI(true, null);
                                    dialog.dismiss();
                                    return true;
                                }
                            })
                            .positiveText(android.R.string.ok)
                            .negativeText(android.R.string.cancel)
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .autoDismiss(false)
                            .show();
                    break;
                default:
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            isMultiSelect = false;
            multiSelectList = new ArrayList<>();
            if (showFAM) {
                Log.d(TAG, "Show FAM 5");
                mActionAddButton.setVisibility(View.VISIBLE);
                mActionAddButton.showMenuButton(true);
            }
            if(searchView != null && !searchView.isIconified()){
                searchView.setIconified(true);
            }
            mRecyclerViewAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (mSlider != null) {
            savedInstanceState.putLong(drawerSelected, getDrawerSelection());
        }
    }


    private void showRatingDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.rating_dialog_title)
                .content(R.string.rating_dialog_content)
                .positiveText(R.string.rating_dialog_positive)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        defaultSharedPreferences.edit().putBoolean("isRated", true).apply();
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse("market://details?id=com.smartjinyu.mybookshelf"));
                        startActivity(i);
                        finish();
                    }
                })
                .negativeText(android.R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        finish();
                    }
                })
                .neutralText(R.string.rating_dialog_neutral)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        defaultSharedPreferences.edit().putBoolean("muteRatings", true).apply();
                        finish();
                    }
                })
                .canceledOnTouchOutside(false)
                .show();

    }
    private void checkTermOfService() {
        boolean isAccepted = defaultSharedPreferences.getBoolean("isAcceptTermOfService", false);
        if (!isAccepted) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.about_preference_term_of_service));

            WebView wv = new WebView(this);
            if (getCurrentLocale().equals(Locale.CHINA)) {
                wv.loadUrl("file:///android_asset/termOfService_zh.html");
            } else {
                wv.loadUrl("file:///android_asset/termOfService_en.html");
            }
            wv.setWebViewClient(new WebViewClient() {
                @SuppressWarnings("deprecation")
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }

                @TargetApi(Build.VERSION_CODES.N)
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    final Uri uri = request.getUrl();
                    view.loadUrl(uri.toString());
                    return true;
                }

            });

            alert.setView(wv);
            alert.setPositiveButton(R.string.accept_term_of_service_dialog_positive, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    defaultSharedPreferences.edit().putBoolean("isAcceptTermOfService", true).apply();
                    dialog.dismiss();
                }
            });
            alert.setNegativeButton(R.string.accept_term_of_service_dialog_negative, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    Toast.makeText(MainActivity.this, R.string.accept_term_of_service_dialog_deny_toast, Toast.LENGTH_LONG)
                            .show();
                    finish();
                }
            });
            alert.show();

        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private Locale getCurrentLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            return getResources().getConfiguration().locale;
        }
    }


}
