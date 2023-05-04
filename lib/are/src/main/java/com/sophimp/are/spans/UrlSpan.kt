package com.sophimp.are.spans

import android.text.style.URLSpan


class UrlSpan(url: String?) : URLSpan(url), IClickableSpan, ISpan {
}