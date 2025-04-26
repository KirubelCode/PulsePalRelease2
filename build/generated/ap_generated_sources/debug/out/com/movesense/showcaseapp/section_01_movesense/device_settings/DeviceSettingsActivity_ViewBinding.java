// Generated code from Butter Knife. Do not modify!
package com.movesense.showcaseapp.section_01_movesense.device_settings;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.movesense.showcaseapp.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class DeviceSettingsActivity_ViewBinding implements Unbinder {
  private DeviceSettingsActivity target;

  private View view7f0900a2;

  private View view7f09009b;

  private View view7f09009d;

  private View view7f0900a0;

  private View view7f09009a;

  @UiThread
  public DeviceSettingsActivity_ViewBinding(DeviceSettingsActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public DeviceSettingsActivity_ViewBinding(final DeviceSettingsActivity target, View source) {
    this.target = target;

    View view;
    target.mDeviceSettingsUartStatusTv = Utils.findRequiredViewAsType(source, R.id.device_settings_uart_status_tv, "field 'mDeviceSettingsUartStatusTv'", TextView.class);
    view = Utils.findRequiredView(source, R.id.device_settings_uart_switch, "method 'onUartCheckedChange'");
    view7f0900a2 = view;
    ((CompoundButton) view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton p0, boolean p1) {
        target.onUartCheckedChange(p0, p1);
      }
    });
    view = Utils.findRequiredView(source, R.id.device_settings_powerOffAfterReset_switch, "method 'onPowerOffCheckedChange'");
    view7f09009b = view;
    ((CompoundButton) view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton p0, boolean p1) {
        target.onPowerOffCheckedChange(p0, p1);
      }
    });
    view = Utils.findRequiredView(source, R.id.device_settings_uart_get_btn, "method 'onViewClicked'");
    view7f09009d = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.device_settings_uart_set_btn, "method 'onViewClicked'");
    view7f0900a0 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.device_settings_powerOffAfterReset_set_btn, "method 'onViewClicked'");
    view7f09009a = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    DeviceSettingsActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mDeviceSettingsUartStatusTv = null;

    ((CompoundButton) view7f0900a2).setOnCheckedChangeListener(null);
    view7f0900a2 = null;
    ((CompoundButton) view7f09009b).setOnCheckedChangeListener(null);
    view7f09009b = null;
    view7f09009d.setOnClickListener(null);
    view7f09009d = null;
    view7f0900a0.setOnClickListener(null);
    view7f0900a0 = null;
    view7f09009a.setOnClickListener(null);
    view7f09009a = null;
  }
}
