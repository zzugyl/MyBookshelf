package com.smartjinyu.mybookshelf;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.actions.DialogActionExtKt;
import com.afollestad.materialdialogs.callbacks.DialogCallbackExtKt;
import com.afollestad.materialdialogs.input.DialogInputExtKt;
import com.afollestad.materialdialogs.list.DialogListExtKt;
import com.afollestad.materialdialogs.list.DialogMultiChoiceExtKt;
import com.afollestad.materialdialogs.list.DialogSingleChoiceExtKt;
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
        new MaterialDialog(context, com.afollestad.materialdialogs.ModalDialog.INSTANCE)
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
        new MaterialDialog(context, com.afollestad.materialdialogs.ModalDialog.INSTANCE)
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
        new MaterialDialog(context, com.afollestad.materialdialogs.ModalDialog.INSTANCE)
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

    /** 输入对话框（资源 ID 版本） */
    public static MaterialDialog showInput(@NonNull Context context,
                                           @StringRes int titleRes,
                                           @StringRes int hintRes, @Nullable String prefill,
                                           int inputMaxLength,
                                           @StringRes int positiveRes, @StringRes int negativeRes,
                                           @NonNull InputCallback onPositive,
                                           @Nullable Runnable onNegative) {
        MaterialDialog dialog = new MaterialDialog(context, com.afollestad.materialdialogs.ModalDialog.INSTANCE);
        dialog.title(titleRes, null);
        DialogInputExtKt.input(dialog, null, hintRes, prefill, null, 0, inputMaxLength, true, true, (d, input) -> {
            onPositive.onInput(d, input.toString());
            return kotlin.Unit.INSTANCE;
        });
        dialog.positiveButton(positiveRes, null, null);
        dialog.negativeButton(negativeRes, null, onNegative != null ? d -> {
            onNegative.run();
            return kotlin.Unit.INSTANCE;
        } : null);
        dialog.show();
        return dialog;
    }

    /** 输入对话框（字符串版本） */
    public static MaterialDialog showInput(@NonNull Context context,
                                           @NonNull String title,
                                           @Nullable String hint, @Nullable String prefill,
                                           int inputMaxLength,
                                           @Nullable String positiveText, @Nullable String negativeText,
                                           @NonNull InputCallback onPositive,
                                           @Nullable Runnable onNegative) {
        MaterialDialog dialog = new MaterialDialog(context, com.afollestad.materialdialogs.ModalDialog.INSTANCE);
        dialog.title(null, title);
        DialogInputExtKt.input(dialog, hint, null, prefill, null, 0, inputMaxLength, true, true, (d, input) -> {
            onPositive.onInput(d, input.toString());
            return kotlin.Unit.INSTANCE;
        });
        dialog.positiveButton(null, positiveText, null);
        dialog.negativeButton(null, negativeText, onNegative != null ? d -> {
            onNegative.run();
            return kotlin.Unit.INSTANCE;
        } : null);
        dialog.show();
        return dialog;
    }

    /** 输入对话框（带验证，不自动关闭） */
    public static MaterialDialog showInputNoAutoDismiss(@NonNull Context context,
                                                        @NonNull String title,
                                                        @Nullable String hint, @Nullable String prefill,
                                                        int inputType, int inputMaxLength,
                                                        @Nullable String positiveText, @Nullable String negativeText,
                                                        @NonNull InputCallback onPositive,
                                                        @Nullable Runnable onNegative) {
        MaterialDialog dialog = new MaterialDialog(context, com.afollestad.materialdialogs.ModalDialog.INSTANCE);
        dialog.title(null, title);
        DialogInputExtKt.input(dialog, hint, null, prefill, null, inputType, inputMaxLength, false, true, (d, input) -> {
            onPositive.onInput(d, input.toString());
            return kotlin.Unit.INSTANCE;
        });
        dialog.positiveButton(null, positiveText, null);
        dialog.negativeButton(null, negativeText, onNegative != null ? d -> {
            onNegative.run();
            return kotlin.Unit.INSTANCE;
        } : null);
        dialog.noAutoDismiss();
        dialog.show();
        return dialog;
    }

    /** 列表选择对话框 */
    public static void showList(@NonNull Context context,
                                @StringRes int titleRes,
                                @NonNull CharSequence[] items,
                                @NonNull ListCallback onSelected) {
        List<CharSequence> itemList = Arrays.asList(items);
        MaterialDialog dialog = new MaterialDialog(context, com.afollestad.materialdialogs.ModalDialog.INSTANCE);
        dialog.title(titleRes, null);
        DialogListExtKt.listItems(dialog, null, itemList, null, false, (d, index, text) -> {
            onSelected.onSelected(index, text.toString());
            return kotlin.Unit.INSTANCE;
        });
        dialog.show();
    }

    /** 单选对话框 */
    public static void showSingleChoice(@NonNull Context context,
                                        @StringRes int titleRes,
                                        @NonNull CharSequence[] items,
                                        int selectedIndex,
                                        @StringRes int positiveRes,
                                        @NonNull ListCallback onSelected) {
        List<CharSequence> itemList = Arrays.asList(items);
        MaterialDialog dialog = new MaterialDialog(context, com.afollestad.materialdialogs.ModalDialog.INSTANCE);
        dialog.title(titleRes, null);
        DialogSingleChoiceExtKt.listItemsSingleChoice(dialog, null, itemList, null, selectedIndex, true, 0, 0, (d, index, text) -> {
            onSelected.onSelected(index, text.toString());
            return kotlin.Unit.INSTANCE;
        });
        dialog.positiveButton(positiveRes, null, null);
        dialog.show();
    }

    /** 多选对话框 */
    public static MaterialDialog showMultiChoice(@NonNull Context context,
                                                 @StringRes int titleRes,
                                                 @NonNull CharSequence[] items,
                                                 @Nullable int[] initialSelection,
                                                 @StringRes int positiveRes,
                                                 @NonNull MultiCallback onSelected) {
        List<CharSequence> itemList = Arrays.asList(items);
        MaterialDialog dialog = new MaterialDialog(context, com.afollestad.materialdialogs.ModalDialog.INSTANCE);
        dialog.title(titleRes, null);
        DialogMultiChoiceExtKt.listItemsMultiChoice(dialog, null, itemList, null, initialSelection, true, true, (d, indices, texts) -> {
            onSelected.onSelected(d, indices);
            return kotlin.Unit.INSTANCE;
        });
        dialog.positiveButton(positiveRes, null, null);
        dialog.show();
        return dialog;
    }

    /** 带 neutral 按钮的多选对话框 */
    public static MaterialDialog showMultiChoiceWithNeutral(@NonNull Context context,
                                                            @StringRes int titleRes,
                                                            @NonNull CharSequence[] items,
                                                            @Nullable int[] initialSelection,
                                                            @StringRes int positiveRes,
                                                            @StringRes int neutralRes,
                                                            @NonNull MultiCallback onSelected,
                                                            @Nullable Runnable onNeutral) {
        List<CharSequence> itemList = Arrays.asList(items);
        MaterialDialog dialog = new MaterialDialog(context, com.afollestad.materialdialogs.ModalDialog.INSTANCE);
        dialog.title(titleRes, null);
        DialogMultiChoiceExtKt.listItemsMultiChoice(dialog, null, itemList, null, initialSelection, true, true, (d, indices, texts) -> {
            onSelected.onSelected(d, indices);
            return kotlin.Unit.INSTANCE;
        });
        dialog.positiveButton(positiveRes, null, null);
        dialog.neutralButton(neutralRes, null, onNeutral != null ? d -> {
            onNeutral.run();
            return kotlin.Unit.INSTANCE;
        } : null);
        dialog.noAutoDismiss();
        dialog.show();
        return dialog;
    }

    /** 获取对话框按钮（用于动态启用/禁用） */
    public static View getActionButton(@NonNull MaterialDialog dialog, boolean isPositive) {
        return DialogActionExtKt.getActionButton(dialog,
                isPositive ? WhichButton.POSITIVE : WhichButton.NEGATIVE);
    }

    /** 获取输入框文本 */
    public static String getInputText(@NonNull MaterialDialog dialog) {
        return DialogInputExtKt.getInputField(dialog).getText().toString();
    }

    /** 添加 dismiss 回调 */
    public static MaterialDialog onDismiss(@NonNull MaterialDialog dialog, @NonNull Runnable onDismiss) {
        return DialogCallbackExtKt.onDismiss(dialog, d -> {
            onDismiss.run();
            return kotlin.Unit.INSTANCE;
        });
    }

    // Callback interfaces
    public interface InputCallback {
        void onInput(@NonNull MaterialDialog dialog, @NonNull String text);
    }

    public interface ListCallback {
        void onSelected(int index, @NonNull String text);
    }

    public interface MultiCallback {
        void onSelected(@NonNull MaterialDialog dialog, @NonNull int[] indices);
    }

    public interface MultiCallbackValidate {
        boolean onSelection(@NonNull MaterialDialog dialog, @NonNull int[] indices);
    }
}
