// Generated code from Butter Knife. Do not modify!
package com.movesense.showcaseapp.section_01_movesense.tests;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.movesense.showcaseapp.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class BatteryActivity_ViewBinding implements Unbinder {
  private BatteryActivity target;

  private View view7f09024a;

  @UiThread
  public BatteryActivity_ViewBinding(BatteryActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public BatteryActivity_ViewBinding(final BatteryActivity target, View source) {
    this.target = target;

    View view;
    target.mConnectedDeviceNameTextView = Utils.findRequiredViewAsType(source, R.id.connected_device_name_textView, "field 'mConnectedDeviceNameTextView'", TextView.class);
    target.mConnectedDeviceSwVersionTextView = Utils.findRequiredViewAsType(source, R.id.connected_device_swVersion_textView, "field 'mConnectedDeviceSwVersionTextView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.temperature_get_button, "field 'mTemperatureGetButton' and method 'onViewClicked'");
    target.mTemperatureGetButton = Utils.castView(view, R.id.temperature_get_button, "field 'mTemperatureGetButton'", Button.class);
    view7f09024a = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked();
      }
    });
    target.mValueTextView = Utils.findRequiredViewAsType(source, R.id.value_textView, "field 'mValueTextView'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    BatteryActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mConnectedDeviceNameTextView = null;
    target.mConnectedDeviceSwVersionTextView = null;
    target.mTemperatureGetButton = null;
    target.mValueTextView = null;

    view7f09024a.setOnClickListener(null);
    view7f09024a = null;
  }
}
