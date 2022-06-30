package com.androidninja.myninjanotes.adapters;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.androidninja.myninjanotes.R;
import com.androidninja.myninjanotes.entities.Note;
import com.androidninja.myninjanotes.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesVewHolder>{

    private List<Note> notes;
    private NotesListener notesListener;

    private Timer timer;
    private List<Note> notesSource;



    public NotesAdapter(List<Note> notes,NotesListener notesListener)

    {
        this.notes = notes;
        this.notesListener = notesListener;
        notesSource = notes;
    }

    @NonNull
    @Override
    public NotesVewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        return new NotesVewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_note,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull  NotesVewHolder holder, int position) {
            holder.setNote(notes.get(position));
            holder.layoutNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notesListener.onNoteClicked(notes.get(position),position);
                }
            });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NotesVewHolder extends RecyclerView.ViewHolder{

        TextView textTitle,textSubTitle,textDateTime;
        LinearLayout layoutNote;

        ImageView imageNote;

        public NotesVewHolder(@NonNull View itemView) {
            super(itemView);

            textTitle = itemView.findViewById(R.id.textTitle);
            textSubTitle = itemView.findViewById(R.id.textSubTitle);
            textDateTime = itemView.findViewById(R.id.textDateTime);

            layoutNote = itemView.findViewById(R.id.layoutNote);

            imageNote = itemView.findViewById(R.id.imageNote);
        }


        void setNote(Note note)
        {
            textTitle.setText(note.getTitle());
            if(!note.getSubTitle().trim().isEmpty())
            {
                textSubTitle.setVisibility(View.GONE);
            }
            else
            {
                textSubTitle.setText(note.getSubTitle());
            }
            textDateTime.setText(note.getDateTime());

            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if(note.getColor() != null)
            {
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            }
            else
            {
            gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            if(note.getImagePath() != null)
            {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
            }
            else
            {
                imageNote.setVisibility(View.GONE);
            }
        }


    }

    public void searchNotes(final String searchNotes)
    {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
         if(searchNotes.trim().isEmpty())
         {
             notes = notesSource;
         }
         else
         {
             ArrayList<Note> temp = new ArrayList<>();
             for (Note note:notesSource)
             {
                 if(note.getTitle().toLowerCase().contains(searchNotes.toLowerCase())
                  || note.getSubTitle().toLowerCase().contains(searchNotes.toLowerCase())
                 || note.getNoteText().toLowerCase().contains(searchNotes.toLowerCase())){
                     temp.add(note);
                 }
             }
             notes = temp;
         }

         new Handler(Looper.getMainLooper()).post(new Runnable(){

             @Override
             public void run() {
                        notifyDataSetChanged();
             }
         });
            }
        },500);

    }

    public void cancelTimer()
    {
        if(timer != null)
        {
            timer.cancel();
        }
    }
}
