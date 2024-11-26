package com.telemechanic.consu

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.telemechanic.consu.viewmodel.JoinMeetingModel
import com.telemechanic.consu.viewmodel.MeetingModel

class ViewModelFragment : Fragment() {
    val meetingSessionModel: JoinMeetingModel by lazy {
        ViewModelProvider(this)[JoinMeetingModel::class.java]
    }
    val meetingModel: MeetingModel by lazy {
        ViewModelProvider(this)[MeetingModel::class.java]
    }
}