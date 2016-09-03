package info.einverne.guesswords.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import info.einverne.guesswords.GameActivity;
import info.einverne.guesswords.R;
import info.einverne.guesswords.data.GroupItem;
import timber.log.Timber;

/**
 * Created by einverne on 9/3/16.
 */

public class GroupRecyclerAdapter extends RecyclerView.Adapter<GroupRecyclerAdapter.ViewHolder> {

    private Context context;
    private ArrayList<GroupItem> groupItems;

    public GroupRecyclerAdapter(Context context, ArrayList<GroupItem> groupItems) {
        this.context = context;
        this.groupItems = groupItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.groupName.setText(groupItems.get(position).groupName);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timber.d("group item Clicked");
                startGame(groupItems.get(position).groupId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupItems.size();
    }

    private void startGame(String groupId) {
        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra(GameActivity.GROUP_ID, groupId);
        context.startActivity(intent);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView groupName;

        public ViewHolder(View itemView) {
            super(itemView);
            groupName = (TextView) itemView.findViewById(R.id.groupName);
        }

    }
}
