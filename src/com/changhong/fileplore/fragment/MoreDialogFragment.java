package com.changhong.fileplore.fragment;

import com.changhong.fileplore.R;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

public class MoreDialogFragment extends DialogFragment implements OnClickListener {
	RelativeLayout rl_copy;
	RelativeLayout rl_delete;
	RelativeLayout rl_push;
	RelativeLayout rl_share;
	RelativeLayout rl_detail;
	RelativeLayout rl_qr;

	public interface MoreDialogClickListener {
		void onMoreDialogClick(View v);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		View view = inflater.inflate(R.layout.fragment_moredialog, container);
		findView(view);
		initView();
		return view;
	}

	private void initView() {
		rl_copy.setOnClickListener(this);
		rl_delete.setOnClickListener(this);
		rl_push.setOnClickListener(this);
		rl_share.setOnClickListener(this);
		rl_detail.setOnClickListener(this);
		rl_qr.setOnClickListener(this);

	}

	private void findView(View view) {
		rl_copy = (RelativeLayout) view.findViewById(R.id.rl_moreoption_copy);
		rl_delete = (RelativeLayout) view.findViewById(R.id.rl_moreoption_delete);
		rl_push = (RelativeLayout) view.findViewById(R.id.rl_moreoption_push);
		rl_share = (RelativeLayout) view.findViewById(R.id.rl_moreoption_share);
		rl_detail = (RelativeLayout) view.findViewById(R.id.rl_moreoption_detail);
		rl_qr = (RelativeLayout) view.findViewById(R.id.rl_moreoption_qr);

	}

	@Override
	public void onClick(View v) {
		if (getActivity() instanceof MoreDialogClickListener) {
			((MoreDialogClickListener) getActivity()).onMoreDialogClick(v);
		}

	}
}
