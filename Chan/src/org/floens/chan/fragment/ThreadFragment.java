package org.floens.chan.fragment;

import java.util.List;

import org.floens.chan.R;
import org.floens.chan.activity.BaseActivity;
import org.floens.chan.adapter.PostAdapter;
import org.floens.chan.imageview.activity.ImageViewActivity;
import org.floens.chan.manager.ThreadManager;
import org.floens.chan.manager.ThreadManager.ThreadListener;
import org.floens.chan.model.Loadable;
import org.floens.chan.model.Post;
import org.floens.chan.net.EndOfLineException;
import org.floens.chan.utils.LoadView;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

import com.android.volley.VolleyError;

public class ThreadFragment extends Fragment implements ThreadListener {
    private BaseActivity baseActivity;
    private ThreadManager threadManager;
    private PostAdapter postAdapter;
    private boolean shown = false;
    private Loadable loadable;
    private LoadView container;
    private ListView listView;
    
    public static ThreadFragment newInstance(BaseActivity activity) {
        ThreadFragment fragment = new ThreadFragment();
        fragment.baseActivity = activity;
        fragment.threadManager = new ThreadManager(activity, fragment);
        
        return fragment;
    }
    
    public ThreadManager getThreadManager() {
        return threadManager;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (threadManager != null) {
            stopLoading();
            
            threadManager.onDestroy();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        if (threadManager != null) {
            threadManager.onResume();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        if (threadManager != null) {
            threadManager.onPause();
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        container = new LoadView(inflater.getContext());
        return container;
    }
    
    public void stopLoading() {
        if (threadManager != null) {
            threadManager.stop();            
        }
        
        shown = false;
        postAdapter = null;
        
        if (container != null) {
            container.setView(null);
        }
        
        if (listView != null) {
            listView.setOnScrollListener(null);
        }
    }
    
    public void startLoading(Loadable loadable) {
        stopLoading();
        
        this.loadable = loadable;
        
        threadManager.startLoading(loadable);
    }
    
    public void reload() {
        stopLoading();
        
        threadManager.reload();
    }
    
    @Override
    public void onThreadLoaded(List<Post> posts) {
        if (!shown) {
            shown = true;
            
            listView = new ListView(baseActivity);
            
            postAdapter = new PostAdapter(baseActivity, threadManager, listView);
            
            listView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            listView.setAdapter(postAdapter);
            listView.setSelectionFromTop(loadable.listViewIndex, loadable.listViewTop);
            
            if (threadManager.getLoadable().isThreadMode()) {
                listView.setOnScrollListener(new OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {}
                    
                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        if (loadable != null) {
                            loadable.listViewIndex = view.getFirstVisiblePosition();
                            View v = view.getChildAt(0);
                            loadable.listViewTop = (v == null) ? 0 : v.getTop();
                        }
                    }
                });
            }
            
            if (container != null) {
                container.setView(listView);
            }
        }
        
        postAdapter.addToList(posts);
    }
    
    @Override
    public void onThreadLoadError(VolleyError error) {
        if (error instanceof EndOfLineException) {
            postAdapter.setEndOfLine(true);
        } else {
            if (container != null) {
                container.setView(threadManager.getTextViewError(error));
            }
        }
    }
    
    @Override
    public void onPostClicked(Post post) {
        baseActivity.onOPClicked(post);
    }
    
    @Override
    public void onThumbnailClicked(Post source) {
        if (isDetached() || postAdapter == null) return;
        
        ImageViewActivity.setAdapter(postAdapter, source.no);
        
        Intent intent = new Intent(baseActivity, ImageViewActivity.class);
        baseActivity.startActivity(intent);
        baseActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}




