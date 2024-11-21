package com.devesh.trivio_app

import android.content.Context
import android.media.MediaPlayer

// MusicManager handles the playback of background music in the app
object MusicManager {
    private var mediaPlayer: MediaPlayer? = null

    // Starts the background music. If music isn't already playing, it creates a new MediaPlayer instance.
    fun startMusic(context: Context, resourceId: Int) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, resourceId).apply {
                isLooping = true
                start()
            }
        } else if (!mediaPlayer!!.isPlaying) {
            mediaPlayer?.start()
        }
    }

    // Stops the currently playing music and releases the MediaPlayer resources
    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // Resumes the music if it was previously paused or stopped
    fun resumeMusic() {
        mediaPlayer?.start()
    }
}
