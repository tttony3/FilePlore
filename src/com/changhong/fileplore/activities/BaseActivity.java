package com.changhong.fileplore.activities;

import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

public class BaseActivity  extends Activity {  
  
    protected  ImageLoader imageLoader = ImageLoader.getInstance();  
  
    @Override  
    public boolean onCreateOptionsMenu(Menu menu) {  
        // 加载菜单  
   //     getMenuInflater().inflate(R.menu.main_menu, menu);  
        return true;  
    }  
  
    @Override  
    public boolean onOptionsItemSelected(MenuItem item) {  
        switch (item.getItemId()) {  
//            case R.id.item_clear_memory_cache:  
//                imageLoader.clearMemoryCache();     // 清除内存缓存  
//                return true;  
//            case R.id.item_clear_disc_cache:  
//                imageLoader.clearDiscCache();       // 清除SD卡中的缓存  
//                return true;  
            default:  
                return false;  
        }  
    }  
}  
