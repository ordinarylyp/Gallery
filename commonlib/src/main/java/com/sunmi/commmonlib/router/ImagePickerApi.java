package com.sunmi.commmonlib.router;

import android.content.Context;

import com.xiaojinzi.component.anno.ParameterAnno;
import com.xiaojinzi.component.anno.router.HostAnno;
import com.xiaojinzi.component.anno.router.NavigateAnno;
import com.xiaojinzi.component.anno.router.PathAnno;
import com.xiaojinzi.component.anno.router.RequestCodeAnno;
import com.xiaojinzi.component.anno.router.RouterApiAnno;
import com.xiaojinzi.component.impl.BiCallback;

/**
 * Description:
 *
 * @author linyuanpeng on 2019-09-16.
 */
@RouterApiAnno()
@HostAnno(ModuleConfig.ImagePicker.NAME)
public interface ImagePickerApi {

    @PathAnno(ModuleConfig.ImagePicker.IMAGEOICKER)
    @NavigateAnno(forResultCode = true)
    @RequestCodeAnno()
    void goToImagePicker(Context context, BiCallback<Integer> callback);

    @PathAnno(ModuleConfig.ImagePicker.IMAGEPRE)
    @NavigateAnno(forResultCode = true)
    @RequestCodeAnno(0x01)
    void goToImagePre(Context context,@ParameterAnno("imagePosition") int position,BiCallback<Integer> callback);
}
