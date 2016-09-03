package info.einverne.guesswords.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import info.einverne.guesswords.R;
import timber.log.Timber;

/**
 * Created by einverne on 9/3/16.
 */

public class MyWordRecyclerAdapter extends RecyclerView.Adapter<MyWordRecyclerAdapter.ViewHolder> {

    ArrayList<String> mWordsList;

    public MyWordRecyclerAdapter(ArrayList<String> mWordsList) {
        this.mWordsList = mWordsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_word_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mWord.setText(mWordsList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timber.d("Clicked");
            }
        });
    }

    @Override
    public int getItemCount() {
        return mWordsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mWord;

        public ViewHolder(View itemView) {
            super(itemView);
            mWord = (TextView) itemView.findViewById(R.id.tvWord);
        }
    }
}
