// Generated code from Butter Knife. Do not modify!
package com.movesense.showcaseapp.section_03_dfu;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.movesense.showcaseapp.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class DfuActivity2_ViewBinding implements Unbinder {


  private View view7f0900a8;

  private View view7f0900ac;

  private View view7f0900b1;

  @UiThread
  public DfuActivity2_ViewBinding(DfuActivity2 target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public DfuActivity2_ViewBinding(final DfuActivity2 target, View source) {
    this.target = target;

    View view;
    target.mDfuSelectDeviceTextView = Utils.findRequiredViewAsType(source, R.id.dfu_selectDevice_textView, "field 'mDfuSelectDeviceTextView'", TextView.class);
    target.mDfuSelectedDeviceNameTextView = Utils.findRequiredViewAsType(source, R.id.dfu_selectedDevice_nameTextView, "field 'mDfuSelectedDeviceNameTextView'", TextView.class);
    target.mDfuSelectedDeviceSerialTextView = Utils.findRequiredViewAsType(source, R.id.dfu_selectedDevice_serialTextView, "field 'mDfuSelectedDeviceSerialTextView'", TextView.class);
    target.mDfuSelectedDeviceInfoLayout = Utils.findRequiredViewAsType(source, R.id.dfu_selectedDevice_infoLayout, "field 'mDfuSelectedDeviceInfoLayout'", LinearLayout.class);
    view = Utils.findRequiredView(source, R.id.dfu_selectedDevice_containerLl, "field 'mDfuSelectedDeviceContainer' and method 'onViewClicked'");
    target.mDfuSelectedDeviceContainer = Utils.castView(view, R.id.dfu_selectedDevice_containerLl, "field 'mDfuSelectedDeviceContainer'", LinearLayout.class);
    view7f0900a8 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.mDfuSelectedFileTextView = Utils.findRequiredViewAsType(source, R.id.dfu_selectedFile_textView, "field 'mDfuSelectedFileTextView'", TextView.class);
    target.mDfuSelectedFileFileNameTextView = Utils.findRequiredViewAsType(source, R.id.dfu_selectedFile_fileNameTextView, "field 'mDfuSelectedFileFileNameTextView'", TextView.class);
    target.mDfuSelectedFileFileSizeTextView = Utils.findRequiredViewAsType(source, R.id.dfu_selectedFile_fileSizeTextView, "field 'mDfuSelectedFileFileSizeTextView'", TextView.class);
    target.mDfuSelectedFileInfoLayout = Utils.findRequiredViewAsType(source, R.id.dfu_selectedFile_infoLayout, "field 'mDfuSelectedFileInfoLayout'", LinearLayout.class);
    view = Utils.findRequiredView(source, R.id.dfu_selectedFile_containerLl, "field 'mDfuSelectedFileContainer' and method 'onViewClicked'");
    target.mDfuSelectedFileContainer = Utils.castView(view, R.id.dfu_selectedFile_containerLl, "field 'mDfuSelectedFileContainer'", LinearLayout.class);
    view7f0900ac = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.dfu_startUpload_btn, "field 'mDfuStartUploadBtn' and method 'onViewClicked'");
    target.mDfuStartUploadBtn = Utils.castView(view, R.id.dfu_startUpload_btn, "field 'mDfuStartUploadBtn'", TextView.class);
    view7f0900b1 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.mDfuStatusTv = Utils.findRequiredViewAsType(source, R.id.dfu_status_Tv, "field 'mDfuStatusTv'", TextView.class);
    target.mDfuPercentProgressTv = Utils.findRequiredViewAsType(source, R.id.dfu_percentProgress_Tv, "field 'mDfuPercentProgressTv'", TextView.class);
    target.mDfuDfuSwVersionTv = Utils.findRequiredViewAsType(source, R.id.dfu_dfu_sw_version_tv, "field 'mDfuDfuSwVersionTv'", TextView.class);
    target.mDfuMovesenseSwVersionTv = Utils.findRequiredViewAsType(source, R.id.dfu_movesense_sw_version_tv, "field 'mDfuMovesenseSwVersionTv'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    DfuActivity2 target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mDfuSelectDeviceTextView = null;
    target.mDfuSelectedDeviceNameTextView = null;
    target.mDfuSelectedDeviceSerialTextView = null;
    target.mDfuSelectedDeviceInfoLayout = null;
    target.mDfuSelectedDeviceContainer = null;
    target.mDfuSelectedFileTextView = null;
    target.mDfuSelectedFileFileNameTextView = null;
    target.mDfuSelectedFileFileSizeTextView = null;
    target.mDfuSelectedFileInfoLayout = null;
    target.mDfuSelectedFileContainer = null;
    target.mDfuStartUploadBtn = null;
    target.mDfuStatusTv = null;
    target.mDfuPercentProgressTv = null;
    target.mDfuDfuSwVersionTv = null;
    target.mDfuMovesenseSwVersionTv = null;

    view7f0900a8.setOnClickListener(null);
    view7f0900a8 = null;
    view7f0900ac.setOnClickListener(null);
    view7f0900ac = null;
    view7f0900b1.setOnClickListener(null);
    view7f0900b1 = null;
  }
}
