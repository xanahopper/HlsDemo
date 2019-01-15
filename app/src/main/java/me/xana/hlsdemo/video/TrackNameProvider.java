package me.xana.hlsdemo.video;

import com.google.android.exoplayer2.Format;

public interface TrackNameProvider {

  /** Returns a user readable track name for the given {@link Format}. */
  String getTrackName(Format format);
}