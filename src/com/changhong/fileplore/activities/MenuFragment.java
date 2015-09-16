package com.changhong.fileplore.activities;

import com.changhong.fileplore.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MenuFragment extends Fragment {
	View view;
	ImageView tv;
	RelativeLayout rl_menu_localfile;
	RelativeLayout rl_menu_netfile;
	RelativeLayout rl_menu_sambafile;
	RelativeLayout rl_menu_music;
	RelativeLayout rl_menu_movie;
	RelativeLayout rl_menu_photo;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.layout_menu, null);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//rl_menu_localfile = (RelativeLayout) view.findViewById(R.id.rl_menu_localfile);
		rl_menu_netfile = (RelativeLayout) view.findViewById(R.id.rl_menu_netfile);
		rl_menu_sambafile = (RelativeLayout) view.findViewById(R.id.rl_menu_sambafile);
		rl_menu_music = (RelativeLayout) view.findViewById(R.id.rl_menu_music);
		rl_menu_movie = (RelativeLayout) view.findViewById(R.id.rl_menu_movie);
		rl_menu_photo = (RelativeLayout) view.findViewById(R.id.rl_menu_photo);
		//rl_menu_localfile.setOnClickListener(menuClick);
		rl_menu_netfile.setOnClickListener(menuClick);
		rl_menu_sambafile.setOnClickListener(menuClick);
		rl_menu_music.setOnClickListener(menuClick);
		rl_menu_movie.setOnClickListener(menuClick);
		rl_menu_photo.setOnClickListener(menuClick);

	}

	OnClickListener menuClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			switch (v.getId()) {
		//	case R.id.rl_menu_localfile:

			//	break;
			case R.id.rl_menu_netfile:
				intent.setClass(getActivity(), ShowNetDevActivity.class);
				startActivity(intent);
				break;
			case R.id.rl_menu_sambafile:

				LayoutInflater inflater = getActivity().getLayoutInflater();
				final View layout = inflater.inflate(R.layout.samba_option,
						(ViewGroup) view.findViewById(R.id.samba_op));

				new AlertDialog.Builder(getActivity()).setTitle("samba设置").setView(layout)
						.setNegativeButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						EditText ip = (EditText) layout.findViewById(R.id.ip);
						EditText user = (EditText) layout.findViewById(R.id.user);
						EditText password = (EditText) layout.findViewById(R.id.password);
						EditText dir = (EditText) layout.findViewById(R.id.dir);
						if (!ip.getText().toString().isEmpty() && !user.getText().toString().isEmpty()
								&& !password.getText().toString().isEmpty()) {
							Intent intent = new Intent();
							intent.putExtra("ip", ip.getText().toString());
							intent.putExtra("user", user.getText().toString());
							intent.putExtra("password", password.getText().toString());
							intent.putExtra("dir", dir.getText().toString());
							intent.setClass(getActivity(), SambaActivity.class);
							startActivity(intent);

						}

					}
				}).setPositiveButton("取消", null).show();

				break;
			case R.id.rl_menu_music:
				Log.e("123", "123");
				intent.setClass(getActivity(), ClassifyListActivity.class);
				intent.setFlags(R.id.img_music);
				startActivity(intent);
				break;
			case R.id.rl_menu_movie:
				intent.setClass(getActivity(), ClassifyGridActivity.class);
				intent.setFlags(R.id.img_movie);
				startActivity(intent);
				break;
			case R.id.rl_menu_photo:
				intent.setClass(getActivity(), ClassifyGridActivity.class);
				intent.setFlags(R.id.img_photo);
				startActivity(intent);
				break;
			default:
				break;
			}

		}
	};
}
