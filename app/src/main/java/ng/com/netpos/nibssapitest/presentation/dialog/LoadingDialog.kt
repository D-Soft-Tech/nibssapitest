package ng.com.netpos.nibssapitest.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import ng.com.netpos.nibssapitest.R
import ng.com.netpos.nibssapitest.databinding.LayoutLoadingDialogBinding

class LoadingDialog(var loadingMessage: String = "Processing...") : DialogFragment() {
    private lateinit var binding: LayoutLoadingDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.layout_loading_dialog, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loadingMessage.text = loadingMessage
        dialog?.window?.apply {
            setBackgroundDrawableResource(R.drawable.curve_bg)
            isCancelable = false
        }
    }

    fun setMessage(newMessage: String) {
        loadingMessage = newMessage
        binding.loadingMessage.text = loadingMessage
    }
}
