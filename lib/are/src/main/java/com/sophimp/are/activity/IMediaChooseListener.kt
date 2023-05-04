package com.sophimp.are.activity

import com.sophimp.are.models.MediaInfo

interface IMediaChooseListener {
    fun onMediaChoose(mediaInfos: List<MediaInfo>)
}