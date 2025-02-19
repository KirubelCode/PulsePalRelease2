// Generated code from Butter Knife. Do not modify!
package com.movesense.showcaseapp.section_01_movesense.tests;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.SwitchCompat;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.movesense.showcaseapp.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class HeartRateTestActivity_ViewBinding implements Unbinder {
  private HeartRateTestActivity target;

  private View view7f0800a4;

  @UiThread
  public HeartRateTestActivity_ViewBinding(HeartRateTestActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public HeartRateTestActivity_ViewBinding(final HeartRateTestActivity target, View source) {
    this.target = target;

    View view;
    target.mConnectedDeviceNameTextView = Utils.findRequiredViewAsType(source, R.id.connected_device_name_textView, "field 'mConnectedDeviceNameTextView'", TextView.class);
    target.mConnectedDeviceSwVersionTextView = Utils.findRequiredViewAsType(source, R.id.connected_device_swVersion_textView, "field 'mConnectedDeviceSwVersionTextView'", TextView.class);
    target.mHeartRateRrValueTextView = Utils.findRequiredViewAsType(source, R.id.heart_rate_rr_value_textView, "field 'mHeartRateRrValueTextView'", TextView.class);
    target.mHeartRateBpmValueTextView = Utils.findRequiredViewAsType(source, R.id.heart_rate_bpm_value_textView, "field 'mHeartRateBpmValueTextView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.heart_rate_switch, "field 'heartRateSwitch' and method 'onCheckedChange'");
    target.heartRateSwitch = Utils.castView(view, R.id.heart_rate_switch, "field 'heartRateSwitch'", SwitchCompat.class);
    view7f0800a4 = view;
    ((CompoundButton) view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton p0, boolean p1) {
        target.onCheckedChange(p0, p1);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    HeartRateTestActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mConnectedDeviceNameTextView = null;
    target.mConnectedDeviceSwVersionTextView = null;
    target.mHeartRateRrValueTextView = null;
    target.mHeartRateBpmValueTextView = null;
    target.heartRateSwitch = null;

    ((CompoundButton) view7f0800a4).setOnCheckedChangeListener(null);
    view7f0800a4 = null;
  }
}
