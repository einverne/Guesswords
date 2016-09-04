package info.einverne.guesswords;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import info.einverne.guesswords.adapter.MyWordRecyclerAdapter;
import info.einverne.guesswords.data.MyWordItem;
import timber.log.Timber;

public class MyWordsActivity extends BaseActivity {

    private RecyclerView myWordsRecyclerView;
    private FirebaseUser mUser;
    private ArrayList<MyWordItem> mWordList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_words);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mUser = mAuth.getCurrentUser();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mAddWordAlert = new AlertDialog.Builder(MyWordsActivity.this);
                View v = LayoutInflater.from(MyWordsActivity.this).inflate(R.layout.dialog_my_word_add, null);
                mAddWordAlert.setView(v);

                final EditText editText = (EditText) v.findViewById(R.id.editText);

                mAddWordAlert.setTitle("Add your own word");
                mAddWordAlert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Timber.d("add");
                        String wordToAdd = editText.getText().toString();
                        database.getReference("users").child(mUser.getUid())
                                .child("words")
                                .child(Long.toString(System.currentTimeMillis()))
                                .setValue(wordToAdd);
                        Toast.makeText(MyWordsActivity.this, "Added", Toast.LENGTH_SHORT).show();
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myWordsRecyclerView = (RecyclerView) findViewById(R.id.myWordsRecyclerView);
        myWordsRecyclerView.setHasFixedSize(false);
        myWordsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mWordList = new ArrayList<>();
        final MyWordRecyclerAdapter adapter = new MyWordRecyclerAdapter(MyWordsActivity.this, database, mUser, mWordList);
        myWordsRecyclerView.setAdapter(adapter);

        database.getReference("users")
                .child(mUser.getUid())
                .child("words").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mWordList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String word = data.getValue(String.class);
                    mWordList.add(new MyWordItem(Long.parseLong(data.getKey()),word));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
