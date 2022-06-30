package com.androidninja.myninjanotes.listeners;

import com.androidninja.myninjanotes.entities.Note;

public interface NotesListener {

    void onNoteClicked(Note note, int position);

}
