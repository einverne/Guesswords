package info.einverne.guesswords;

import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import info.einverne.guesswords.data.SingleData;

public class HistoryDataActivity extends AppCompatActivity {

    public static final String DATA_SET = "DATA_SET";
    private RecyclerView recyclerViewHistoryData;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private ArrayList<SingleData> dataset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_data);

        Bundle bundle = getIntent().getExtras();
        dataset = bundle.getParcelableArrayList(DATA_SET);

        recyclerViewHistoryData = (RecyclerView) findViewById(R.id.recyclerViewHistoryData);
        recyclerViewHistoryData.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerViewHistoryData.setLayoutManager(layoutManager);

        adapter = new HistoryDataAdapter(dataset);
        recyclerViewHistoryData.setAdapter(adapter);
    }

    private class HistoryDataAdapter extends RecyclerView.Adapter<HistoryDataAdapter.ViewHolder> {
        ArrayList<SingleData> dataSet;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.historydata_list_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textView.setText(dataSet.get(position).word);
            if (dataSet.get(position).isRight) {
                holder.imageView.setImageResource(R.drawable.right);
            } else {
                holder.imageView.setImageResource(R.drawable.wrong);
            }
        }

        @Override
        public int getItemCount() {
            return dataSet.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView textView;
            public ImageView imageView;

            public ViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.historyDataWord);
                imageView = (ImageView) itemView.findViewById(R.id.historyRightOrWrong);
            }
        }
        public HistoryDataAdapter(ArrayList<SingleData> dataset) {
            dataSet = dataset;
        }
    }
}
