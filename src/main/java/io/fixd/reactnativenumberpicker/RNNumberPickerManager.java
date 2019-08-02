package io.fixd.reactnativenumberpicker;

import android.support.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewProps;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import javax.annotation.Nullable;

public class RNNumberPickerManager extends SimpleViewManager<RNNumberPicker>
        implements RNNumberPicker.OnChangeListener {

    private static final String REACT_CLASS = "RNNumberPicker";

    @NonNull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @NonNull
    @Override
    protected RNNumberPicker createViewInstance(@NonNull ThemedReactContext reactContext) {
        return new RNNumberPicker(reactContext);
    }

    @SuppressWarnings("unused")
    @ReactProp(name = "values")
    public void setValues(RNNumberPicker view, @Nullable ReadableArray items) {
        boolean updateStaged = view.getDisplayedValues() == null;

        if (items != null) {
            String[] displayValues = new String[items.size()];
            for (int i = 0; i < items.size(); i++) {
                displayValues[i] = items.getString(i);
            }

            view.setMinValue(0);
            view.setMaxValue(displayValues.length - 1);
            view.setDisplayedValues(displayValues);
        } else {
            view.setDisplayedValues(null);
        }

        if (updateStaged) {
            view.updateStagedSelection();
        }
    }

    @SuppressWarnings("unused")
    @ReactProp(name = "selected")
    public void setValue(RNNumberPicker view, Integer selected) {
        if (view.getDisplayedValues() == null) {
            view.setStagedSelection(selected);
        } else {
            view.setValue(selected);
        }
    }

    @SuppressWarnings("unused")
    @ReactProp(name = "keyboardInputEnabled")
    public void setKeyboardInputEnabled(RNNumberPicker view, Boolean enabled) {
        view.setKeyboardInputEnabled(enabled);
    }

    @SuppressWarnings("unused")
    @ReactProp(name = "showDivider")
    public void setShowDivider(RNNumberPicker view, Boolean show) {
        if(!show) {
            view.disableDivider();
        }
    }

    @SuppressWarnings("unused")
    @ReactProp(name = ViewProps.COLOR, customType = "Color")
    public void setColor(RNNumberPicker view, Integer color) {
        view.setmTextColor(color);
    }

    @SuppressWarnings("unused")
    @ReactProp(name = ViewProps.FONT_SIZE)
    public void setFontSize(RNNumberPicker view, Integer fontSize) {
        view.setmTextSize(fontSize);
    }

    @SuppressWarnings("unused")
    @ReactProp(name = ViewProps.FONT_FAMILY)
    public void setFontFamily(RNNumberPicker view, String fontFamily) {
        view.setmFontFamily(fontFamily);
    }

    @SuppressWarnings("unused")
    @ReactProp(name = ViewProps.FONT_WEIGHT)
    public void setFontWeight(RNNumberPicker view, String fontWeight) {
        view.setmFontWeight(fontWeight);
    }

    @Override
    protected void addEventEmitters(@NonNull final ThemedReactContext reactContext,
                                    @NonNull final RNNumberPicker picker) {
        picker.setOnChangeListener(this);
    }

    @Override
    public void onValueChange(int value, int viewId, ReactContext reactContext) {
        WritableMap event = Arguments.createMap();
        event.putInt("value", value);
        reactContext.getJSModule(RCTEventEmitter.class)
                .receiveEvent(viewId, "topChange", event);
    }


}
