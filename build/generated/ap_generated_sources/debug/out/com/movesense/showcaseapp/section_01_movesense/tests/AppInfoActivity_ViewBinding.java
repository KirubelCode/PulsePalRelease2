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

public class AppInfoActivity_ViewBinding implements Unbinder {
  private AppInfoActivity target;

  private View view7f090060;

  @UiThread
  public AppInfoActivity_ViewBinding(AppInfoActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public AppInfoActivity_ViewBinding(final AppInfoActivity target, View source) {
    this.target = target;

    View view;
    target.mSensorListAppInfoNameTv = Utils.findRequiredViewAsType(source, R.id.sensorList_appInfo_name_tv, "field 'mSensorListAppInfoNameTv'", TextView.class);
    target.mSensorListAppInfoVersionTv = Utils.findRequiredViewAsType(source, R.id.sensorList_appInfo_version_tv, "field 'mSensorListAppInfoVersionTv'", TextView.class);
    target.mSensorListAppInfoCompanyTv = Utils.findRequiredViewAsType(source, R.id.sensorList_appInfo_company_tv, "field 'mSensorListAppInfoCompanyTv'", TextView.class);
    view = Utils.findRequiredView(source, R.id.buttonGet, "field 'mButtonGet' and method 'onViewClicked'");
    target.mButtonGet = Utils.castView(view, R.id.buttonGet, "field 'mButtonGet'", Button.class);
    view7f090060 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    AppInfoActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mSensorListAppInfoNameTv = null;
    target.mSensorListAppInfoVersionTv = null;
    target.mSensorListAppInfoCompanyTv = null;
    target.mButtonGet = null;

    view7f090060.setOnClickListener(null);
    view7f090060 = null;
  }
}
