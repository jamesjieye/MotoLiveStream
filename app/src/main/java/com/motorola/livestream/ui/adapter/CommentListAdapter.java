package com.motorola.livestream.ui.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.motorola.livestream.R;
import com.motorola.livestream.model.fb.Comment;
import com.motorola.livestream.model.fb.User;
import com.motorola.livestream.util.CircleTransform;
import com.motorola.livestream.util.ExecutorUtil;
import com.motorola.livestream.util.FbUtil;

import java.util.List;

public class CommentListAdapter extends BaseRecyclerViewAdapter<Comment> {

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            CommentListAdapter.this.notifyDataSetChanged();
        }
    };

    public CommentListAdapter(RecyclerView recyclerView) {
        super(recyclerView, R.layout.live_comment_item, null);
    }

    @Override
    protected void bindData(BaseRecyclerViewHolder holder, int position, Comment item) {
        User from = item.getFrom();
        updateUserPhoto(from, holder.getImageView(R.id.viewer_img));
        holder.getTextView(R.id.viewer_name).setText(
                (from != null) ? from.getName() : null);
        holder.getTextView(R.id.viewer_comment).setText(item.getMessage());
    }

    public void setCommentData(List<Comment> commentList) {
        clearData();
        addData(commentList);
        if (getItemCount() > 0) {
            mRecyclerView.scrollToPosition(getItemCount() - 1);
        }
        notifyDataSetChanged();
    }

    private void updateUserPhoto(User currentUser, ImageView imgView) {
        Context context = imgView.getContext();
        if (context == null || currentUser == null) {
            imgView.setImageDrawable(null);
            return;
        }

        String userPhotoUrl = currentUser.getUserPhotoUrl();
        if (TextUtils.isEmpty(userPhotoUrl)) {
            RequestOptions options =
                    new RequestOptions().transform(new CircleTransform());
            Glide.with(mContext)
                    .load(R.drawable.ic_user_photo_default)
                    .apply(options)
                    .into(imgView);
            // Cause maybe there will be a huge instant request, so we have to control them in a deque
            // avoid Facebook to process the requests simultaneously
            ExecutorUtil.executeAsync(new GetUserPhotoTask(mHandler, currentUser));
        } else {
            RequestOptions options =
                    new RequestOptions().placeholder(R.drawable.ic_user_photo_default)
                            .transform(new CircleTransform());
            Glide.with(mContext)
                    .load(userPhotoUrl)
                    .apply(options)
                    .into(imgView);
        }
    }

    private static class GetUserPhotoTask implements Runnable {

        private final Handler mHandler;
        private final User mUser;

        public GetUserPhotoTask(Handler handler, User user) {
            mHandler = handler;
            mUser = user;
        }

        @Override
        public void run() {
            String photoUrl = User.getCachedUserPhoto(mUser.getId());
            if (TextUtils.isEmpty(photoUrl)) {
                FbUtil.getUserPhoto(new FbUtil.OnDataRetrievedListener<User>() {
                    @Override
                    public void onSuccess(User data) {
                        mHandler.sendEmptyMessage(0);
                    }

                    @Override
                    public void onError(Exception exp) {
                        exp.printStackTrace();
                    }
                }, mUser, false);
            } else {
                mUser.setUserPhotoUrl(photoUrl);
                mHandler.sendEmptyMessage(0);
            }
        }
    }
}
