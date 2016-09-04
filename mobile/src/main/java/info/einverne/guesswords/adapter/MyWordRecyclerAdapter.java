package info.einverne.guesswords.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import info.einverne.guesswords.R;
import info.einverne.guesswords.data.MyWordItem;
import timber.log.Timber;

/**
 * Created by einverne on 9/3/16.
 */

public class MyWordRecyclerAdapter extends RecyclerView.Adapter<MyWordRecyclerAdapter.ViewHolder> {

    private Context context;
    private FirebaseDatabase database;
    private FirebaseUser user;
    ArrayList<MyWordItem> mWordsList;

    public MyWordRecyclerAdapter(Context context, FirebaseDatabase database, FirebaseUser user, ArrayList<MyWordItem> mWordsList) {
        this.context = context;
        this.database = database;
        this.user = user;
        this.mWordsList = mWordsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_word_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mWord.setText(mWordsList.get(position).mWord);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timber.d("Clicked " + mWordsList.get(position).mAddTime);
                AlertDialog.Builder mAddWordAlert = new AlertDialog.Builder(context);
                View v = LayoutInflater.from(context).inflate(R.layout.dialog_my_word_add, null);
                mAddWordAlert.setView(v);

                final EditText editText = (EditText) v.findViewById(R.id.editText);
                editText.setText(mWordsList.get(position).mWord);

                mAddWordAlert.setTitle("Update your own word");
                mAddWordAlert.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Timber.d("add");
                        String wordToAdd = editText.getText().toString();
                        database.getReference("users").child(user.getUid())
                                .child("words")
                                .child(Long.toString(mWordsList.get(position).mAddTime))
                                .setValue(wordToAdd);
                        Toast.makeText(context, "Update", Toast.LENGTH_SHORT).show();
                    }
                });
                mAddWordAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Timber.d("cancel");
                    }
                });

                mAddWordAlert.show();
            }
        });
        holder.popMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.pop_up_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.my_word_update:
                                Timber.d("update");
                                AlertDialog.Builder mAddWordAlert = new AlertDialog.Builder(context);
                                View v = LayoutInflater.from(context).inflate(R.layout.dialog_my_word_add, null);
                                mAddWordAlert.setView(v);

                                final EditText editText = (EditText) v.findViewById(R.id.editText);
                                editText.setText(mWordsList.get(position).mWord);

                                mAddWordAlert.setTitle("Update your own word");
                                mAddWordAlert.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Timber.d("add");
                                        String wordToAdd = editText.getText().toString();
                                        database.getReference("users").child(user.getUid())
                                                .child("words")
                                                .child(Long.toString(mWordsList.get(position).mAddTime))
                                                .setValue(wordToAdd);
                                        Toast.makeText(context, "Update", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                mAddWordAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Timber.d("cancel");
                                    }
                                });

                                mAddWordAlert.show();
                                break;
                            case R.id.my_word_delete:
                                Timber.d("delete");
                                database.getReference("users").child(user.getUid())
                                        .child("words")
                                        .child(Long.toString(mWordsList.get(position).mAddTime))
                                        .removeValue();

                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mWordsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView mWord;
        ImageView popMenu;

        public ViewHolder(View itemView) {
            super(itemView);
            mWord = (TextView) itemView.findViewById(R.id.tvWord);
            popMenu = (ImageView) itemView.findViewById(R.id.popMenu);
        }


        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.add("menu");
            contextMenu.add("menu1");

        }
    }
}
