// Generated code from Butter Knife. Do not modify!
package com.movesense.showcaseapp.section_01_movesense.tests;

import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.SwitchCompat;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.github.mikephil.charting.charts.LineChart;
import com.movesense.showcaseapp.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MagneticFieldTestActivity_ViewBinding implements Unbinder {
  private MagneticFieldTestActivity target;

  private View view7f08014d;

  private View view7f080140;

  @UiThread
  public MagneticFieldTestActivity_ViewBinding(MagneticFieldTestActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public MagneticFieldTestActivity_ViewBinding(final MagneticFieldTestActivity target,
      View source) {
    this.target = target;

    View view;
    view = Utils.findRequiredView(source, R.id.switchSubscription, "field 'switchSubscription' and method 'onCheckedChanged'");
    target.switchSubscription = Utils.castView(view, R.id.switchSubscription, "field 'switchSubscription'", SwitchCompat.class);
    view7f08014d = view;
    ((CompoundButton) view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton p0, boolean p1) {
        target.onCheckedChanged(p0, p1);
      }
    });
    view = Utils.findRequiredView(source, R.id.spinner, "field 'spinner' and method 'onItemSelected'");
    target.spinner = Utils.castView(view, R.id.spinner, "field 'spinner'", Spinner.class);
    view7f080140 = view;
    ((AdapterView<?>) view).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> p0, View p1, int p2, long p3) {
        target.onItemSelected(p0, p1, p2, p3);
      }

      @Override
      public void onNothingSelected(AdapterView<?> p0) {
      }
    });
    target.xAxisTextView = Utils.findRequiredViewAsType(source, R.id.x_axis_textView, "field 'xAxisTextView'", TextView.class);
    target.yAxisTextView = Utils.findRequiredViewAsType(source, R.id.y_axis_textView, "field 'yAxisTextView'", TextView.class);
    target.zAxisTextView = Utils.findRequiredViewAsType(source, R.id.z_axis_textView, "field 'zAxisTextView'", TextView.class);
    target.mChart = Utils.findRequiredViewAsType(source, R.id.magneticField_lineChart, "field 'mChart'", LineChart.class);
    target.mConnectedDeviceNameTextView = Utils.findRequiredViewAsType(source, R.id.connected_device_name_textView, "field 'mConnectedDeviceNameTextView'", TextView.class);
    target.mConnectedDeviceSwVersionTextView = Utils.findRequiredViewAsType(source, R.id.connected_device_swVersion_textView, "field 'mConnectedDeviceSwVersionTextView'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    MagneticFieldTestActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.switchSubscription = null;
    target.spinner = null;
    target.xAxisTextView = null;
    target.yAxisTextView = null;
    target.zAxisTextView = null;
    target.mChart = null;
    target.mConnectedDeviceNameTextView = null;
    target.mConnectedDeviceSwVersionTextView = null;

    ((CompoundButton) view7f08014d).setOnCheckedChangeListener(null);
    view7f08014d = null;
    ((AdapterView<?>) view7f080140).setOnItemSelectedListener(null);
    view7f080140 = null;
  }
}
