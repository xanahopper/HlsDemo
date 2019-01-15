package me.xana.hlsdemo.video;

import android.content.res.Resources;
import android.text.TextUtils;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import me.xana.hlsdemo.R;

import java.util.Locale;

public class DefaultTrackNameProvider implements TrackNameProvider {

  private final Resources resources;

  /** @param resources Resources from which to obtain strings. */
  public DefaultTrackNameProvider(Resources resources) {
    this.resources = Assertions.checkNotNull(resources);
  }

  @Override
  public String getTrackName(Format format) {
    String trackName;
    int trackType = inferPrimaryTrackType(format);
    if (trackType == C.TRACK_TYPE_VIDEO) {
      trackName = joinWithSeparator(buildResolutionString(format), buildBitrateString(format));
    } else if (trackType == C.TRACK_TYPE_AUDIO) {
      trackName =
          joinWithSeparator(
              buildLanguageString(format),
              buildAudioChannelString(format),
              buildBitrateString(format));
    } else {
      trackName = buildLanguageString(format);
    }
    return trackName.length() == 0 ? resources.getString(R.string.exo_track_unknown) : trackName;
  }

  private String buildResolutionString(Format format) {
    int width = format.width;
    int height = format.height;
    return width == Format.NO_VALUE || height == Format.NO_VALUE
        ? ""
        : resources.getString(R.string.exo_track_resolution, width, height);
  }

  private String buildBitrateString(Format format) {
    int bitrate = format.bitrate;
    return bitrate == Format.NO_VALUE
        ? ""
        : resources.getString(R.string.exo_track_bitrate, bitrate / 1000000f);
  }

  private String buildAudioChannelString(Format format) {
    int channelCount = format.channelCount;
    if (channelCount == Format.NO_VALUE || channelCount < 1) {
      return "";
    }
    switch (channelCount) {
      case 1:
        return resources.getString(R.string.exo_track_mono);
      case 2:
        return resources.getString(R.string.exo_track_stereo);
      case 6:
      case 7:
        return resources.getString(R.string.exo_track_surround_5_point_1);
      case 8:
        return resources.getString(R.string.exo_track_surround_7_point_1);
      default:
        return resources.getString(R.string.exo_track_surround);
    }
  }

  private String buildLanguageString(Format format) {
    String language = format.language;
    return TextUtils.isEmpty(language) || C.LANGUAGE_UNDETERMINED.equals(language)
        ? ""
        : buildLanguageString(language);
  }

  private String buildLanguageString(String language) {
    Locale locale = Util.SDK_INT >= 21 ? Locale.forLanguageTag(language) : new Locale(language);
    return locale.getDisplayLanguage();
  }

  private String joinWithSeparator(String... items) {
    String itemList = "";
    for (String item : items) {
      if (item.length() > 0) {
        if (TextUtils.isEmpty(itemList)) {
          itemList = item;
        } else {
          itemList = resources.getString(R.string.exo_item_list, itemList, item);
        }
      }
    }
    return itemList;
  }

  private static int inferPrimaryTrackType(Format format) {
    int trackType = MimeTypes.getTrackType(format.sampleMimeType);
    if (trackType != C.TRACK_TYPE_UNKNOWN) {
      return trackType;
    }
    if (MimeTypes.getVideoMediaMimeType(format.codecs) != null) {
      return C.TRACK_TYPE_VIDEO;
    }
    if (MimeTypes.getAudioMediaMimeType(format.codecs) != null) {
      return C.TRACK_TYPE_AUDIO;
    }
    if (format.width != Format.NO_VALUE || format.height != Format.NO_VALUE) {
      return C.TRACK_TYPE_VIDEO;
    }
    if (format.channelCount != Format.NO_VALUE || format.sampleRate != Format.NO_VALUE) {
      return C.TRACK_TYPE_AUDIO;
    }
    return C.TRACK_TYPE_UNKNOWN;
  }
}