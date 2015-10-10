package com.changhong.fileplore.activities;

import java.io.File;
import java.util.List;

import com.changhong.alljoyn.simpleservice.FC_GetShareFile;
import com.changhong.fileplore.R;
import com.changhong.fileplore.base.BaseActivity;
import com.changhong.fileplore.utils.Utils;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ShowSharefileActivity extends BaseActivity {
	List<String> shareList;
	ListView lv_sharelist;
	TextView tv_listnum;
	SharefileAdapter mSharefileAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_file);
		findView();
		shareList = FC_GetShareFile.GetShareList();
		initView();
	}

	private void initView() {
		tv_listnum.setText(shareList.size() + "项");
		mSharefileAdapter = new SharefileAdapter(shareList);
		lv_sharelist.setAdapter(mSharefileAdapter);
		lv_sharelist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final String filepath =(String)parent.getItemAtPosition(position);
				AlertDialog.Builder dialog = new AlertDialog.Builder(ShowSharefileActivity.this);
				dialog.setTitle("");
				String[] dataArray = new String[] { "取消共享" };
				dialog.setItems(dataArray, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						switch (which) {
						case 0:
							Toast.makeText(ShowSharefileActivity.this, FC_GetShareFile.DeleteShareFile(filepath), Toast.LENGTH_SHORT).show();
							shareList = FC_GetShareFile.GetShareList();
							mSharefileAdapter.updateList(shareList);
							tv_listnum.setText(shareList.size() + "项");
							break;
						}
					}
				});
				dialog.create().show();
			
				
				
			}
		});
	}

	@Override
	protected void findView() {
		lv_sharelist = findView(R.id.lv_sharefile);
		tv_listnum = findView(R.id.tv_sharefilenum);

	}

	class SharefileAdapter extends BaseAdapter {

		private List<String> shareList;

		public SharefileAdapter(List<String> shareList) {
			this.shareList = shareList;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return shareList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return shareList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				convertView = LayoutInflater.from(ShowSharefileActivity.this).inflate(R.layout.listitem_sharefile,
						null);
				viewHolder = new ViewHolder();
				viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_share_filename);
				viewHolder.tv_url = (TextView) convertView.findViewById(R.id.tv_share_fileurl);
				viewHolder.iv = (ImageView) convertView.findViewById(R.id.im_sharefile);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			String loc = shareList.get(position);
			File file = new File(loc);
			if (file.exists()) {
				if (file.isDirectory()) {
					viewHolder.iv.setBackgroundResource(R.drawable.file_icon_folder);
				} else {
					String type = Utils.getMIMEType(loc);
					if (type == "video") {
						viewHolder.iv.setBackgroundResource(R.drawable.file_icon_movie);
					} else if (type == "audio") {
						viewHolder.iv.setBackgroundResource(R.drawable.file_icon_music);
					} else if (type == "image") {
						viewHolder.iv.setBackgroundResource(R.drawable.file_icon_photo);
					} else if (type == "*") {
						viewHolder.iv.setBackgroundResource(R.drawable.file_icon_unknown);
					}
				}
			}

			viewHolder.tv_title.setText(loc.subSequence(loc.lastIndexOf("/") + 1, loc.length()));
			viewHolder.tv_url.setText(loc);

			return convertView;
		}
		public void updateList(List<String> shareList){
				this.shareList=shareList;
				notifyDataSetChanged();
		}
		class ViewHolder {
			public TextView tv_title;
			public ImageView iv;
			public TextView tv_url;

		}
	}
}
