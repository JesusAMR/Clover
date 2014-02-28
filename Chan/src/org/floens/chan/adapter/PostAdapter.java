package org.floens.chan.adapter;

import java.util.ArrayList;
import java.util.List;

import org.floens.chan.R;
import org.floens.chan.manager.ThreadManager;
import org.floens.chan.model.Post;
import org.floens.chan.utils.ViewUtils;
import org.floens.chan.view.PostView;
import org.floens.chan.view.ThreadWatchCounterView;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class PostAdapter extends BaseAdapter {
    private final Context context;
    private final ThreadManager threadManager;
    private final ListView listView;
    private boolean endOfLine;
    private int count = 0;
    private final List<Post> postList = new ArrayList<Post>();
    
    public PostAdapter(Context activity, ThreadManager threadManager, ListView listView) {
        this.context = activity;
        this.threadManager = threadManager;
        this.listView = listView;
    }

    @Override
    public int getCount() {
        if (threadManager.getLoadable().isBoardMode() || threadManager.getLoadable().isThreadMode()) {
            return count + 1;
        } else {
            return count;
        }
    }

    @Override
    public Post getItem(int position) {
        return postList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= getCount() - 1 && !endOfLine && threadManager.getLoadable().isBoardMode()) {
            // Try to load more posts
            threadManager.loadMore();
        }
        
        if (position >= count) {
            return createThreadEndView();
        } else {
            PostView postView = null;
            
            if (position >= 0 && position < postList.size()) {
                if (convertView != null && convertView instanceof PostView) {
                    postView = (PostView) convertView;
                } else {
                    postView = new PostView(context);
                }
                
                postView.setPost(postList.get(position), threadManager);
            } else {
                Log.e("Chan", "PostAdapter: Invalid index: " + position + ", size: " + postList.size() + ", count: " + count);
            }
            
            return postView;
        }
    }
    
    private View createThreadEndView() {
        if (threadManager.getWatchLogic() != null) {
            ThreadWatchCounterView view = new ThreadWatchCounterView(context);
            ViewUtils.setPressedDrawable(view);
            view.init(threadManager, listView, this);
            int padding = context.getResources().getDimensionPixelSize(R.dimen.general_padding);
            view.setPadding(padding, padding, padding, padding);
            view.setGravity(Gravity.CENTER);
            return view;
        } else {
            if (endOfLine) {
                TextView textView = new TextView(context);
                textView.setText(context.getString(R.string.end_of_line));
                int padding = context.getResources().getDimensionPixelSize(R.dimen.general_padding);
                textView.setPadding(padding, padding, padding, padding);
                return textView;
            } else {
                return new ProgressBar(context);
            }
        }
    }
    
    public void addToList(List<Post> list) {
        List<Post> newPosts = new ArrayList<Post>();
        
        for (Post newPost : list) {
            boolean have = false;
            for (Post havePost : postList) {
                if (havePost.no == newPost.no) {
                    have = true;
                    break;
                }
            }
            
            if (!have) {
                newPosts.add(newPost);
            }
        }
        
        postList.addAll(newPosts);
        count += newPosts.size();
        
        notifyDataSetChanged();
    }
    
    public List<Post> getList() {
        return postList;
    }
    
    public void setEndOfLine(boolean endOfLine) {
        this.endOfLine = endOfLine;
        
        notifyDataSetChanged();
    }
    
    public void scrollToPost(Post post) {
        for (int i = 0; i < postList.size(); i++) {
            if (postList.get(i) == post) {
                listView.smoothScrollToPosition(i);
                
                break;
            }
        }
    }
}




