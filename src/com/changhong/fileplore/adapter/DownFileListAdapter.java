package com.changhong.fileplore.adapter;

import java.io.File;
import java.util.ArrayList;

import com.changhong.alljoyn.simpleservice.FC_GetShareFile;
import com.changhong.fileplore.R;
import com.changhong.fileplore.data.DownData;
import com.changhong.fileplore.service.DownLoadService;
import com.changhong.fileplore.utils.Utils;
import com.chobit.corestorage.CoreApp;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DownFileListAdapter extends BaseAdapter {
	String download_Path;
	String appname;
	private LayoutInflater inflater;
	ArrayList<DownData> downList;
	ArrayList<DownData> alreadydownList;
	ArrayList<DownData> allList;
	Context context;
	DownLoadService downLoadService;

	public DownFileListAdapter(ArrayList<DownData> downList, ArrayList<DownData> alreadydownList, Context context, DownLoadService downLoadService) {
		allList = new ArrayList<DownData>();
		inflater = LayoutInflater.from(context);
		if (downList == null) {
			downList = new ArrayList<DownData>();
		} else
			this.downList = downList;
		this.alreadydownList = alreadydownList;
		this.context = context;
		 download_Path = Environment.getExternalStorageDirectory().getAbsolutePath();
		 appname = FC_GetShareFile.getApplicationName(context);
		this.downLoadService = downLoadService;
		
		allList.addAll(downList);
		allList.addAll(alreadydownList);
	}

	@Override
	public int getCount() {

		return allList.size();
	}

	@Override
	public Object getItem(int position) {
		return allList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return allList.get(position).getTotalPart();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listitem_downlist, null);
			viewHolder = new ViewHolder();
			viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			viewHolder.tv_process = (TextView) convertView.findViewById(R.id.tv_process);
			viewHolder.pb = (ProgressBar) convertView.findViewById(R.id.download_bar);
			viewHolder.btn = (Button) convertView.findViewById(R.id.btn_download);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final DownData tmpdata = allList.get(position);
		viewHolder.tv_name.setText(tmpdata.getName());
		int max = (int) (tmpdata.getTotalPart() / 100);
		if(max ==0)
			max =1;
		viewHolder.pb.setMax(max);
		long part = tmpdata.getCurPart();
		viewHolder.pb.setProgress((int) (part / 100));
		int p = (int) (part / max);
		viewHolder.tv_process.setText(p + "%");
		if (tmpdata.isDone()) {
			viewHolder.btn.setText("打开");
			viewHolder.btn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					context.startActivity(
							Utils.openFile(new File(download_Path + "/" + appname + "/download/" + tmpdata.getName())));
				}
			});
		} else {
			if (!tmpdata.isCancel()) {
				viewHolder.btn.setText("取消");
				viewHolder.btn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						downLoadService.stopDownload(tmpdata.getUri());
						
						
					}
				});
			} else {
				viewHolder.btn.setText("下载");
				viewHolder.btn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						downLoadService.addDownloadFile(tmpdata.getUri());

					}
				});
			}
		}
		return convertView;
	}

	class ViewHolder {
		public TextView tv_process;
		public TextView tv_name;
		public ProgressBar pb;
		public Button btn;
	}

	public void update(ArrayList<DownData> downList2, DownLoadService downLoadService2) {
		downList = downList2;
		downLoadService=downLoadService2;
		allList.clear();
		
		allList.addAll(downList);
		allList.addAll(alreadydownList);
		notifyDataSetChanged();

	}
}
