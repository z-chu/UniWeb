package com.radiuswallet.uniweb.utils;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;

public class ToolbarUtils {

    private ToolbarUtils() {
        // Private constructor to prevent unwanted construction.
    }

    @Nullable
    public static TextView getTitleTextView(@NonNull Toolbar toolbar) {
        return getTextView(toolbar, toolbar.getTitle());
    }

    @Nullable
    public static TextView getSubtitleTextView(@NonNull Toolbar toolbar) {
        return getTextView(toolbar, toolbar.getSubtitle());
    }

    @Nullable
    private static TextView getTextView(@NonNull Toolbar toolbar, CharSequence text) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View child = toolbar.getChildAt(i);
            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                if (TextUtils.equals(textView.getText(), text)) {
                    return textView;
                }
            }
        }
        return null;
    }

    @Nullable
    public static View getSecondaryActionMenuItemView(@NonNull Toolbar toolbar) {
        ActionMenuView actionMenuView = getActionMenuView(toolbar);
        if (actionMenuView != null) {
            // Only return the first child of the ActionMenuView if there is more than one child
            if (actionMenuView.getChildCount() > 1) {
                return actionMenuView.getChildAt(0);
            }
        }
        return null;
    }

    @Nullable
    public static ActionMenuView getActionMenuView(@NonNull Toolbar toolbar) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View child = toolbar.getChildAt(i);
            if (child instanceof ActionMenuView) {
                return (ActionMenuView) child;
            }
        }
        return null;
    }

    @Nullable
    public static ImageButton getNavigationIconButton(@NonNull Toolbar toolbar) {
        Drawable navigationIcon = toolbar.getNavigationIcon();
        if (navigationIcon == null) {
            return null;
        }
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View child = toolbar.getChildAt(i);
            if (child instanceof ImageButton) {
                ImageButton imageButton = (ImageButton) child;
                if (imageButton.getDrawable() == navigationIcon) {
                    return imageButton;
                }
            }
        }
        return null;
    }


}
