package ins.com.ins_project.materialcamera;

import android.app.Fragment;
import android.support.annotation.NonNull;

import ins.com.ins_project.materialcamera.internal.BaseCaptureActivity;
import ins.com.ins_project.materialcamera.internal.CameraFragment;

public class CaptureActivity extends BaseCaptureActivity {

  @Override
  @NonNull
  public Fragment getFragment() {
    return CameraFragment.newInstance();
  }
}
