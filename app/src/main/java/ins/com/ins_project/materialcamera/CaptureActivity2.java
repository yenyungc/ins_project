package ins.com.ins_project.materialcamera;

import android.app.Fragment;
import android.support.annotation.NonNull;

import ins.com.ins_project.materialcamera.internal.BaseCaptureActivity;
import ins.com.ins_project.materialcamera.internal.Camera2Fragment;


public class CaptureActivity2 extends BaseCaptureActivity {

  @Override
  @NonNull
  public Fragment getFragment() {
    return Camera2Fragment.newInstance();
  }
}
