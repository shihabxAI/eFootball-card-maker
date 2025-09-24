# Data classes/models should not be obfuscated, as their field names might be
# used by serialization libraries (like Gson) or reflection.
-keep class com.example.efootballcardmaker3.Country { *; }
-keep class com.example.efootballcardmaker3.SettingItem { *; }
-keep class com.example.efootballcardmaker3.WhatsNewItem { *; }
-keep class com.example.efootballcardmaker3.EditorUiState { *; }

# Keep custom views that are used in XML layouts.
-keep class com.example.efootballcardmaker3.EraseableImageView { *; }

# Keep setters in ViewModels, as they might be used by data binding or reflection.
-keepclassmembers public class * extends androidx.lifecycle.ViewModel {
   public void set*(***);
}

# Keep Parcelable classes and their creator fields.
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Preserve annotations, which might be needed by certain libraries.
-keepattributes *Annotation*

# Keep callback classes used in listeners.
-keepclassmembers class * {
    void *(**On*Listener);
}