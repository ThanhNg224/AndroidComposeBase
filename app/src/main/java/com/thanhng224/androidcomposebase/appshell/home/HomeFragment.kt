package com.thanhng224.androidcomposebase.appshell.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thanhng224.androidcomposebase.databinding.FragmentAppshellHomeBinding
import com.thanhng224.androidcomposebase.core.ui.base.BaseFragment

class HomeFragment : BaseFragment<FragmentAppshellHomeBinding>() {
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentAppshellHomeBinding = FragmentAppshellHomeBinding.inflate(inflater, container, false)

    override fun onBindingReady(
        view: View,
        savedInstanceState: Bundle?,
    ) = Unit
}
