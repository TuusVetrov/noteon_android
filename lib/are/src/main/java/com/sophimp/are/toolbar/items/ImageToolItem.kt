package com.sophimp.are.toolbar.items

import com.sophimp.are.R
import com.sophimp.are.style.IStyle
import com.sophimp.are.toolbar.IToolbarItemClickAction

/**
 * @author: sfx
 * @since: 2021/7/21
 */
class ImageToolItem(style: IStyle, itemClickAction: IToolbarItemClickAction? = null) : AbstractItem(style, itemClickAction) {
    override val srcResId: Int
        get() = R.drawable.ic_add_photo_unsel

    /**
     * Open system image chooser page.
     */
    private fun openImageChooser() {
//		ImageSelectDialog dialog = new ImageSelectDialog(mContext, this, REQUEST_CODE);
//		dialog.show();
//        val intent = Intent(style.mEditText.context, ImagesSelectorActivity::class.java)
//        // max number of images to be selected
//        intent.putExtra(SelectorSettings.SELECTOR_MAX_IMAGE_NUMBER, 9)
//        // min size of image which will be shown; to filter tiny images (mainly icons)
//        intent.putExtra(SelectorSettings.SELECTOR_MIN_IMAGE_SIZE, 100000)
//        // show camera or not
//        intent.putExtra(SelectorSettings.SELECTOR_SHOW_CAMERA, true)
//        // pass current selected images as the initial value
//        intent.putStringArrayListExtra(SelectorSettings.SELECTOR_INITIAL_SELECTED_LIST, null)
//        // start the selector
//        (mContext as Activity).startActivityForResult(intent, ImageStyle.REQUEST_CODE)
    }
}