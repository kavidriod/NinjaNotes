package com.androidninja.myninjanotes.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.androidninja.myninjanotes.R;
import com.androidninja.myninjanotes.adapters.NotesAdapter;
import com.androidninja.myninjanotes.database.NotesDatabase;
import com.androidninja.myninjanotes.entities.Note;
import com.androidninja.myninjanotes.listeners.NotesListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NotesListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTE = 3;

    String LOG_TAG = MainActivity.class.getSimpleName();

    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;
    private EditText inputSearch;

    private int noteClickedPosition = -1;

    ImageView imageAddVoiceNote;
    private static final int REQUEST_CODE_SPEECH_INPUT = 4;
    private static final String TEXT_SPEECH_INPUT = "TEXT_SPEECH_INPUT";

    //Add adView
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initMobileAds();

        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNotes(null);

            }
        });

        notesRecyclerView  = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        );

        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList,this);
        notesRecyclerView.setAdapter(notesAdapter);

        getNotes(REQUEST_CODE_SHOW_NOTE,false);

        inputSearch = findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                  if(noteList.size() > 0)
                  {
                      notesAdapter.searchNotes(editable.toString());
                  }
            }
        });


        imageAddVoiceNote = findViewById(R.id.imageAddVoiceNote);
        imageAddVoiceNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVoiceNotes();
            }
        });
    }

    private void createNotes(String notes) {
        Intent intent = new Intent(MainActivity.this,CreateNoteActivity.class);
        intent.putExtra("TEXT_SPEECH_INPUT",notes);
        startActivityForResult(intent,REQUEST_CODE_ADD_NOTE);
    }

    private void showVoiceNotes() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Add Voice Notes!");

        PackageManager manager = getApplicationContext().getPackageManager();
        List<ResolveInfo> infos = manager.queryIntentActivities(intent, 0);
        if (infos.size() > 0) {
            //Then there is application can handle your intent
            startActivityForResult(intent,REQUEST_CODE_SPEECH_INPUT);
        }else{
            //No Application can handle your intent
            Toast.makeText(getApplicationContext(),
                    R.string.speech_recognition_is_not_supported,Toast.LENGTH_LONG).show();
        }
    }


    private void initMobileAds() {
        adView = findViewById(R.id.adView);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.i("Ad","onInitializationComplete");
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.i("Ad","onAdLoaded");
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.i("Ad","onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.i("Ad","onAdFailedToLoad");
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.i("Ad","onAdClicked");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                Log.i("Ad","onAdOpened");
            }
        });
    }

    private void getNotes(final int requestCode,final boolean isNoteDeleted)
    {
        class GetNotesTask extends AsyncTask<Void,Void, List<Note>>
        {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase
                        .getInstance(getApplicationContext())
                        .noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if(requestCode == REQUEST_CODE_SHOW_NOTE)
                {
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                }
                else if(requestCode == REQUEST_CODE_ADD_NOTE)
                {
                    noteList.add(0,notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                }
                else if(requestCode == REQUEST_CODE_UPDATE_NOTE)
                {
                    noteList.remove(noteClickedPosition);

                    if (isNoteDeleted)
                    {
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                    }
                    else
                    {
                        noteList.add(noteClickedPosition,notes.get(noteClickedPosition));
                        notesAdapter.notifyItemChanged(noteClickedPosition);

                    }
                }
            }
        }
        new GetNotesTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK)
        {
            getNotes(REQUEST_CODE_ADD_NOTE,false);
        }
        else if(requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK)
        {
            if(data != null)
            {
                boolean isNoteDeleted = data.getBooleanExtra("isNoteDeleted",false);
                getNotes(REQUEST_CODE_UPDATE_NOTE,isNoteDeleted);
            }
        }
        else if(requestCode == REQUEST_CODE_SPEECH_INPUT)
        {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
              /*  tv_Speech_to_text.setText(
                        Objects.requireNonNull(result).get(0));*/

                //Launch Note Creation Page with texttospeech
                String notes = Objects.requireNonNull(result).get(0);
                Log.d("Debug","notes "+notes);

                createNotes(notes);
            }
        }
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Log.d(LOG_TAG,"noteClickedPosition "+noteClickedPosition);
        Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate",true);
        intent.putExtra("note",note);
        startActivityForResult(intent,REQUEST_CODE_UPDATE_NOTE);
    }
}