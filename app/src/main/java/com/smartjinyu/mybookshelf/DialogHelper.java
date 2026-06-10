package com.smartjinyu.mybookshelf;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.actions.DialogActionExtKt;
import com.afollestad.materialdialogs.input.DialogInputExtKt;
import com.afollestad.materialdialogs.WhichButton;

import java.util.Arrays;
import java.util.List;

/**
 * 封装 material-dialogs 3.x 的冗长调用，提供简洁的静态方法。
 */
public class DialogHelper {

    /** 简单对话框（标题 + 内容 + 确定/取消按钮） */
    public static void show(@NonNull Context context,
                            @StringRes int titleRes, @StringRes int contentRes,
                            @StringRes int positiveRes, @StringRes int negativeRes,
                            @Nullable Runnable onPositive, @Nullable Runnable onNegative) {
        new MaterialDialog(context)
                .title(titleRes, null)
                .message(contentRes, null, null)
                .positiveButton(positiveRes, null, onPositive != null ? d -> {
                    onPositive.run();
                    return kotlin.Unit.INSTANCE;
                } : null)
                .negativeButton(negativeRes, null, onNegative != null ? d -> {
                    onNegative.run();
                    return kotlin.Unit.INSTANCE;
                } : null)
                .show();
    }

    /** 简单对话框（字符串标题/内容） */
    public static void show(@NonNull Context context,
                            @NonNull String title, @NonNull String content,
                            @Nullable String positiveText, @Nullable String negativeText,
                            @Nullable Runnable onPositive, @Nullable Runnable onNegative) {
        new MaterialDialog(context)
                .title(null, title)
                .message(null, content, null)
                .positiveButton(null, positiveText, onPositive != null ? d -> {
                    onPositive.run();
                    return kotlin.Unit.INSTANCE;
                } : null)
                .negativeButton(null, negativeText, onNegative != null ? d -> {
                    onNegative.run();
                    return kotlin.Unit.INSTANCE;
                } : null)
                .show();
    }

    /** 带 neutral 按钮的对话框 */
    public static void showWithNeutral(@NonNull Context context,
                                       @StringRes int titleRes, @StringRes int contentRes,
                                       @StringRes int positiveRes, @StringRes int negativeRes, @StringRes int neutralRes,
                                       @Nullable Runnable onPositive, @Nullable Runnable onNegative, @Nullable Runnable onNeutral) {
        new MaterialDialog(context)
                .title(titleRes, null)
                .message(contentRes, null, null)
                .positiveButton(positiveRes, null, onPositive != null ? d -> {
                    onPositive.run();
                    return kotlin.Unit.INSTANCE;
                } : null)
                .negativeButton(negativeRes, null, onNegative != null ? d -> {
                    onNegative.run();
                    return kotlin.Unit.INSTANCE;
                } : null)
                .neutralButton(neutralRes, null, onNeutral != null ? d -> {
                    onNeutral.run();
                    return kotlin.Unit.INSTANCE;
                } : null)
                .show();
    }

    /** 带 neutral 按钮的对话框（字符串版本） */
    public static void showWithNeutral(@NonNull Context context,
                                       @NonNull String title, @NonNull String content,
                                       @Nullable String positiveText, @Nullable String negativeText, @Nullable String neutralText,
                                       @Nullable Runnable onPositive, @Nullable Runnable onNegative, @Nullable Runnable onNeutral) {
        new MaterialDialog(context)
                .title(null, title)
                .message(null, content, null)
                .positiveButton(null, positiveText, onPositive != null ? d -> {
                    onPositive.run();
                    return kotlin.Unit.INSTANCE;
                } : null)
                .negativeButton(null, negativeText, onNegative != null ? d -> {
                    onNegative.run();
                    return kotlin.Unit.INSTANCE;
                } : null)
                .neutralButton(null, neutralText, onNeutral != null ? d -> {
                    onNeutral.run();
                    return kotlin.Unit.INSTANCE;
                } : null)
                .show();
    }

    /** 输入对话框 */
    public static void showInput(@NonNull Context context,
                                 @StringRes int titleRes,
                                 @StringRes int hintRes, @StringRes int prefillRes,
                                 int inputMaxLength,
                                 @StringRes int positiveRes, @StringRes int negativeRes,
                                 @NonNull InputCallback onPositive,
                                 @Nullable Runnable onNegative) {
        new MaterialDialog(context)
                .title(titleRes, null)
                .input(hintRes, prefillRes, null, 0, inputMaxLength, null, false, (d, input) -> {
                    onPositive.onInput(input.toString());
                    return kotlin.Unit.INSTANCE;
                })
                .positiveButton(positiveRes, null, null)
                .negativeButton(negativeRes, null, onNegative != null ? d -> {
                    onNegative.run();
                    return kotlin.Unit.INSTANCE;
                } : null)
                .show();
    }

    /** 输入对话框（字符串版本） */
    public static void showInput(@NonNull Context context,
                                 @NonNull String title,
                                 @Nullable String hint, @Nullable String prefill,
                                 int inputMaxLength,
                                 @Nullable String positiveText, @Nullable String negativeText,
                                 @NonNull InputCallback onPositive,
                                 @Nullable Runnable onNegative) {
        new MaterialDialog(context)
                .title(null, title)
                .input(null, null, prefill, 0, inputMaxLength, null, false, (d, input) -> {
                    onPositive.onInput(input.toString());
                    return kotlin.Unit.INSTANCE;
                })
                .positiveButton(null, positiveText, null)
                .negativeButton(null, negativeText, onNegative != null ? d -> {
                    onNegative.run();
                    return kotlin.Unit.INSTANCE;
                } : null)
                .show();
    }

    /** 列表选择对话框 */
    public static void showList(@NonNull Context context,
                                @StringRes int titleRes,
                                @NonNull String[] items,
                                @NonNull ListCallback onSelected) {
        List<CharSequence> itemList = Arrays.asList(items);
        new MaterialDialog(context)
                .title(titleRes, null)
                .listItems(null, itemList, null, false, (d, index, text) -> {
                    onSelected.onSelected(index, text.toString());
                    return kotlin.Unit.INSTANCE;
                })
                .show();
    }

    /** 单选对话框 */
    public static void showSingleChoice(@NonNull Context context,
                                        @StringRes int titleRes,
                                        @NonNull String[] items,
                                        int selectedIndex,
                                        @StringRes int positiveRes,
                                        @NonNull ListCallback onSelected) {
        List<CharSequence> itemList = Arrays.asList(items);
        new MaterialDialog(context)
                .title(titleRes, null)
                .listItemsSingleChoice(null, itemList, null, selectedIndex, true, 0, (d, index, text) -> {
                    onSelected.onSelected(index, text.toString());
                    return true;
                })
                .positiveButton(positiveRes, null, null)
                .show();
    }

    /** 多选对话框 */
    public static void showMultiChoice(@NonNull Context context,
                                       @StringRes int titleRes,
                                       @NonNull String[] items,
                                       @Nullable Integer[] selectedIndices,
                                       @StringRes int positiveRes,
                                       @NonNull MultiCallback onSelected) {
        List<CharSequence> itemList = Arrays.asList(items);
        int[] initialSelection = null;
        if (selectedIndices != null) {
            initialSelection = new int[selectedIndices.length];
            for (int i = 0; i < selectedIndices.length; i++) {
                initialSelection[i] = selectedIndices[i];
            }
        }
        new MaterialDialog(context)
                .title(titleRes, null)
                .listItemsMultiChoice(null, itemList, null, initialSelection, true, true, (d, indices, texts) -> {
                    onSelected.onSelected(indices);
                    return true;
                })
                .positiveButton(positiveRes, null, null)
                .show();
    }

    /** 获取对话框按钮（用于动态启用/禁用） */
    public static android.view.View getActionButton(@NonNull MaterialDialog dialog, boolean isPositive) {
        return DialogActionExtKt.getActionButton(dialog,
                isPositive ? WhichButton.Positive : WhichButton.Negative);
    }

    /** 获取输入框文本 */
    public static String getInputText(@NonNull MaterialDialog dialog) {
        return DialogInputExtKt.getInputField(dialog).getText().toString();
    }

    // Callback interfaces
    public interface InputCallback {
        void onInput(@NonNull String text);
    }

    public interface ListCallback {
        void onSelected(int index, @NonNull String text);
    }

    public interface MultiCallback {
        void onSelected(@NonNull int[] indices);
    }
}
