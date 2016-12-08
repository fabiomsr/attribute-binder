package org.fabiomsr.yeko.yekotest;

import android.content.Context;
import android.widget.RelativeLayout;

import org.fabiomsr.yeko.R;
import org.fabiomsr.yeko.annotation.CustomView;
import org.fabiomsr.yeko.annotation.SaveStated;

/**
 * Created by Fabiomsr on 25/7/16.
 */
@CustomView(parent = RelativeLayout.class, rClass = R.class, styleableFile = "app/src/main/res/values/MyView.xml")
public class MyView extends YekoMyView {

  private int zero;

  @SaveStated
  private int one;

  @SaveStated
  private int two;


  public MyView(Context context) {
    super(context);
    setter().amount(12)
            .baseTextColor(12)
          .apply();

  }
}
